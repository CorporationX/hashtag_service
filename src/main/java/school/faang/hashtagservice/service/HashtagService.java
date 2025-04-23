package school.faang.hashtagservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.exception.ElasticsearchConnectionException;
import school.faang.hashtagservice.exception.PostNotFoundException;
import school.faang.hashtagservice.exception.PostServiceConnectionException;
import school.faang.hashtagservice.exception.UserNotFoundException;
import school.faang.hashtagservice.exception.UserServiceConnectionException;
import school.faang.hashtagservice.filter.HashtagFilter;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.publisher.HashtagRemovingEventPublisher;
import school.faang.hashtagservice.publisher.HashtagRequestEventPublisher;
import school.faang.hashtagservice.repository.ElasticsearchHashtagRepository;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashtagService {

    private static final int RETRY_DELAY = 500;
    private static final int RETRY_MULTIPLIER = 3;
    private static final String ELASTIC_ERROR_MESSAGE = "Elastic search connection failed. %s";

    private final HashtagRepository hashtagRepository;
    private final ElasticsearchHashtagRepository elasticRepository;
    private final UserServiceClient userClient;
    private final PostServiceClient postClient;
    private final UserContext userContext;
    private final HashtagMapper hashtagMapper;
    private final List<HashtagFilter> filters;
    private final HashtagRequestEventPublisher hashtagRequestPublisher;
    private final HashtagRemovingEventPublisher hashtagRemovingPublisher;
    private final HashtagCacheService cacheService;
    private final Executor unusedHashtagCleaner;

    @Value("${cleaner-config.days}")
    private int days;

    @Transactional
    public void addHashtags(HashtagStringsDto hashtagDto) {
        checkUserExists();
        hashtagDto.hashtagNames().stream()
                .map(this::createHashtag)
                .filter(hashtag -> isHashtagNotExists(hashtag.getName()))
                .forEach(hashtag -> {
                    saveHashtag(hashtag);
                    log.info("Hashtag {} added successfully", hashtag.getName());
                });
    }

    public List<HashtagResponseDto> getHashtagsByIds(List<Long> hashtagIds) {
        List<Hashtag> cachedHashtags = findHashtagsOnCache(hashtagIds);

        if (isHashtagListsSizeEquals(cachedHashtags, hashtagIds)) {
            publishEventOnRequestListener(cachedHashtags);
            return hashtagMapper.toDtoList(cachedHashtags);
        }

        List<Hashtag> hashtags = findMissingHashtags(hashtagIds, cachedHashtags);

        return hashtagMapper.toDtoList(publishEventOnRequestListener(hashtags));
    }

    @Cacheable(
            value = "${cache-config.hashtag-filters-key}",
            key = "#filterDto.toString()",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<HashtagResponseDto> getHashtagsByFilters(HashtagFilterDto filterDto) {
        List<Hashtag> result = new ArrayList<>();

        for (HashtagFilter filter : filters) {
            if (filter.isApplicable(filterDto)) {
                try {
                    result = mergeResults(result, filter.apply(filterDto));
                } catch (IOException e) {
                    throw new ElasticsearchConnectionException(ELASTIC_ERROR_MESSAGE, e.getMessage());
                }
            }
        }

        return hashtagMapper.toDtoList(result.stream()
                .sorted(Comparator.comparing(Hashtag::getCreatedAt).reversed())
                .toList());
    }

    public List<Long> getHashtagsIdsByPostId(Long postId) {
        return hashtagRepository.findAllByPostsWithHashtagId(postId).stream()
                .map(Hashtag::getId)
                .toList();
    }

    public Map<Long, List<Long>> getHashtagsIdsByPostIds(List<Long> postIds) {
        return hashtagRepository.findAllByPostsWithHashtagIdIn(postIds).stream()
                .flatMap(hashtag -> hashtag.getPostsWithHashtag().stream()
                        .map(post -> Pair.of(post.getId(), hashtag.getId())))
                .collect(Collectors.groupingBy(
                        Pair::getFirst,
                        Collectors.mapping(Pair::getSecond, Collectors.toList())));
    }

    @Transactional
    public void linkHashtagOnPost(HashtagAddingEvent event) {
        if (isHashtagNotExists(event.hashtagName())) {
            saveHashtag(createHashtag(event.hashtagName()));
        }
        Hashtag hashtag = hashtagRepository.findByName(event.hashtagName());
        List<PostHashtag> posts = hashtag.getPostsWithHashtag();
        posts.add(createPostHashtag(event.postId(), hashtag));
        hashtag.setPostsWithHashtag(posts);
        saveHashtag(hashtag);
        log.info("Hashtag {} linked to post with id {}", hashtag.getName(), event.postId());
    }

    @Transactional
    public void unlinkHashtagOnPost(Long postId) {
        List<Hashtag> hashtags = hashtagRepository.findAllByPostsWithHashtagId(postId);
        hashtags.stream()
                .peek(hashtag -> {
                    List<PostHashtag> posts = hashtag.getPostsWithHashtag();
                    posts.removeIf(post -> post.getPostId().equals(postId));
                    hashtag.setPostsWithHashtag(posts);
                })
                .forEach(hashtag -> {
                    saveHashtag(hashtag);
                    log.info("Hashtag {} unlink to post with id {}", hashtag.getName(), postId);
                });
    }

    public List<PostResponseDto> getPostsByHashtagIds(List<Long> hashtagIds) {
        List<Hashtag> cachedHashtags = findHashtagsOnCache(hashtagIds);

        if (isHashtagListsSizeEquals(cachedHashtags, hashtagIds)) {
            publishEventOnRequestListener(cachedHashtags);
            List<Long> postIds = findPostIdsByHashtags(cachedHashtags);

            return getPostsOnPostClient(postIds);
        }

        List<Hashtag> hashtags = findMissingHashtags(hashtagIds, cachedHashtags);

        List<Long> postIds = findPostIdsByHashtags(hashtags);
        return getPostsOnPostClient(postIds);
    }

    @Async("unusedHashtagCleaner")
    public void clearUnusedHashtags() {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
        List<Hashtag> hashtags = hashtagRepository.findAllByPostsWithHashtagEmptyAndCreatedAtBefore(dateTime);

        hashtags.removeIf(hashtag -> !hashtag.getPostsWithHashtag().isEmpty());
        hashtagRepository.deleteAll(hashtags);

        List<CompletableFuture<Void>> futures = hashtags.stream()
                .map(hashtag -> CompletableFuture.runAsync(() -> {
                    hashtagRemovingPublisher.publish(createHashtagRemovingEvent(hashtag.getUserId(), hashtag.getName()));
                    log.debug("Hashtag {} removed from DB and sent to notification listener", hashtag.getName());
                }, unusedHashtagCleaner))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Transactional
    public void saveHashtag(Hashtag hashtag) {
        hashtagRepository.save(hashtag);
        log.debug("Hashtag {} saved to Postgres", hashtag.getName());
        try {
            elasticRepository.save(hashtag);
        } catch (IOException e) {
            throw new ElasticsearchConnectionException(ELASTIC_ERROR_MESSAGE, e.getMessage());
        }
        log.debug("Hashtag {} saved to Elasticsearch", hashtag.getName());
    }

    @Retryable(
            retryFor = UserServiceConnectionException.class,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    private void checkUserExists() {
        try {
            Long userId = userContext.getUserId();
            userClient.getUser(userId);
        } catch (FeignException e) {
            if (e.status() < 500) {
                throw new UserNotFoundException("User with id %d not found", userContext.getUserId());
            } else {
                throw new UserServiceConnectionException("User server returned an error: " + e.getMessage());
            }
        }
    }

    @Retryable(
            retryFor = PostServiceConnectionException.class,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    private List<PostResponseDto> getPostsOnPostClient(List<Long> postIds) {
        try {
            return postClient.getPostsByIds(postIds);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new PostNotFoundException("Posts not found");
            } else {
                throw new PostServiceConnectionException("Post server returned an error: " + e.getMessage());
            }
        }
    }

    private boolean isHashtagNotExists(String hashtagName) {
        return !hashtagRepository.existsByName(hashtagName);
    }

    private List<Hashtag> publishEventOnRequestListener(List<Hashtag> hashtags) {
        return hashtags.stream()
                .peek(hashtag -> {
                    hashtagRequestPublisher.publish(createHashtagRequestEvent(hashtag.getUserId(), hashtag.getId()));
                    log.debug("Hashtag {} published on listener", hashtag.getName());
                })
                .toList();
    }

    private Hashtag createHashtag(String name) {
        return Hashtag.builder()
                .name(name)
                .postsWithHashtag(new ArrayList<>())
                .userId(userContext.getUserId())
                .build();
    }

    private PostHashtag createPostHashtag(Long postId, Hashtag hashtag) {
        return PostHashtag.builder()
                .postId(postId)
                .hashtag(hashtag)
                .build();
    }

    private HashtagRequestEvent createHashtagRequestEvent(Long userId, Long hashtagId) {
        return HashtagRequestEvent.builder()
                .userId(userId)
                .hashtagId(hashtagId)
                .receivedAt(LocalDateTime.now())
                .build();
    }

    private HashtagRemovingEvent createHashtagRemovingEvent(Long userId, String hashtagName) {
        return HashtagRemovingEvent.builder()
                .userId(userId)
                .hashtagName(hashtagName)
                .removedAt(LocalDateTime.now())
                .build();
    }

    private List<Hashtag> findHashtagsOnCache(List<Long> hashtagIds) {
        return cacheService.getPopularHashtags().stream()
                .filter(hashtag -> hashtagIds.contains(hashtag.getId()))
                .toList();
    }

    private List<Hashtag> findMissingHashtags(List<Long> hashtagIds, List<Hashtag> cachedHashtags) {
        List<Long> missingIds = hashtagIds.stream()
                .filter(id -> cachedHashtags.stream().noneMatch(hashtag -> hashtag.getId().equals(id)))
                .toList();
        List<Hashtag> hashtagsOnDatabase = hashtagRepository.findAllByIdIn(missingIds);
        List<Hashtag> hashtags = new ArrayList<>(cachedHashtags);
        hashtags.addAll(hashtagsOnDatabase);
        return publishEventOnRequestListener(hashtags);
    }

    private List<Long> findPostIdsByHashtags(List<Hashtag> hashtags) {
        return hashtags.stream()
                .flatMap(hashtag -> hashtag.getPostsWithHashtag().stream()
                        .map(PostHashtag::getPostId))
                .distinct()
                .toList();
    }

    private boolean isHashtagListsSizeEquals(List<Hashtag> cachedHashtags, List<Long> hashtagsIds) {
        return cachedHashtags.size() == hashtagsIds.size();
    }

    private List<Hashtag> mergeResults(List<Hashtag> existingResult, List<Hashtag> newResult) {
        if (existingResult.isEmpty()) {
            return newResult;
        }
        return existingResult.stream()
                .filter(newResult::contains)
                .toList();
    }
}
