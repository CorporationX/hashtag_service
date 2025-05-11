package school.faang.hashtagservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
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
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.dto.client.UserDto;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.exception.ElasticsearchConnectionException;
import school.faang.hashtagservice.exception.PostNotFoundException;
import school.faang.hashtagservice.exception.PostServiceConnectionException;
import school.faang.hashtagservice.exception.UserNotFoundException;
import school.faang.hashtagservice.exception.UserServiceConnectionException;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.mapper.HashtagSmartDataMapper;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.publisher.HashtagRemovingEventPublisher;
import school.faang.hashtagservice.publisher.HashtagRequestEventPublisher;
import school.faang.hashtagservice.repository.ElasticSearchHashtagRepository;
import school.faang.hashtagservice.repository.HashtagRepository;
import school.faang.hashtagservice.repository.PostHashtagRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static school.faang.hashtagservice.constants.HashtagConstants.ELASTIC_ERROR_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "hashtag")
public class HashtagService {

    private static final int RETRY_DELAY = 500;
    private static final int RETRY_MULTIPLIER = 3;

    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final ElasticSearchHashtagRepository elasticRepository;
    private final UserServiceClient userClient;
    private final PostServiceClient postClient;
    private final UserContext userContext;
    private final HashtagMapper hashtagMapper;
    private final HashtagSmartDataMapper smartHashtagMapper;
    private final HashtagRequestEventPublisher hashtagRequestPublisher;
    private final HashtagRemovingEventPublisher hashtagRemovingPublisher;
    private final Executor unusedHashtagCleaner;

    @Value("${cleaner-config.days}")
    private int days;

    @Transactional
    public void addHashtags(HashtagStringsDto hashtagDto) {
        checkUserExists();
        hashtagDto.hashtagNames().stream()
                .map(hashtag -> createHashtag(hashtag, userContext.getUserId()))
                .filter(hashtag -> isHashtagNotExists(hashtag.getName()))
                .forEach(this::saveHashtag);
    }

    public List<HashtagResponseDto> getHashtagsByIds(List<Long> hashtagIds) {
        List<Hashtag> hashtags = hashtagIds.stream()
                .map(this::getHashtagById)
                .filter(Objects::nonNull)
                .toList();

        return hashtagMapper.toDtoList(publishEventOnRequestListener(hashtags));
    }

    public List<HashtagResponseDto> getHashtagsByFilters(HashtagFilterDto filterDto) {
        List<Hashtag> result = elasticRepository.findHashtagsByFilters(filterDto);

        return hashtagMapper.toDtoList(result.stream()
                .sorted(Comparator.comparing(Hashtag::getCreatedAt).reversed())
                .toList());
    }

    public List<Long> getHashtagsIdsByPostId(Long postId) {
        return postHashtagRepository.findAllByPostId(postId).stream()
                .map(PostHashtag::getId)
                .toList();
    }

    public Map<Long, List<Long>> getHashtagsIdsByPostIds(List<Long> postIds) {
        return postHashtagRepository.findAllByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(PostHashtag::getPostId,
                        Collectors.mapping(sequence -> sequence.getHashtag().getId(), Collectors.toList())
                ));
    }

    @Transactional
    public void linkHashtagOnPost(HashtagAddingEvent event) {
        if (isHashtagNotExists(event.hashtagName())) {
            saveHashtag(createHashtag(event.hashtagName(), event.authorId()));
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
        postHashtagRepository.deleteByPostId(postId);
        log.info("All hashtags unlinked from post {}", postId);
    }

    public List<PostResponseDto> getPostsByHashtagIds(List<Long> hashtagIds) {
        List<Hashtag> hashtags = hashtagIds.stream()
                .map(this::getHashtagById)
                .filter(Objects::nonNull)
                .toList();

        List<Long> postIds = findPostIdsByHashtags(hashtags);
        return getPostsOnPostClient(postIds);
    }

    @Async("unusedHashtagCleaner")
    public void clearUnusedHashtags() {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
        List<Hashtag> hashtags = new ArrayList<>(
                hashtagRepository.findAllByPostsWithHashtagEmptyAndCreatedAtBefore(dateTime));

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
        HashtagSmartDto hashtagSmartDto = smartHashtagMapper.toSmartDto(hashtag);
        try {
            elasticRepository.save(hashtagSmartDto);
        } catch (IOException e) {
            throw new ElasticsearchConnectionException(ELASTIC_ERROR_MESSAGE, e.getMessage());
        }
        log.debug("Hashtag {} saved to Elasticsearch", hashtag.getName());
        log.info("Hashtag {} added successfully", hashtag.getName());
    }

    @Cacheable(key = "#id", unless = "#result == null")
    public Hashtag getHashtagById(Long id) {
        return hashtagRepository.findById(id).orElse(null);
    }

    @Retryable(
            retryFor = UserServiceConnectionException.class,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    private void checkUserExists() {
        try {
            Long userId = userContext.getUserId();
            UserDto user = userClient.getUser(userId);
            if (user == null) {
                throw new UserNotFoundException("User with id %d not found", userId);
            }
        } catch (FeignException e) {
            throw new UserServiceConnectionException("User server returned an error: " + e.getMessage());
        }
    }

    @Retryable(
            retryFor = PostServiceConnectionException.class,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    private List<PostResponseDto> getPostsOnPostClient(List<Long> postIds) {
        try {
            List<PostResponseDto> postListDto = postClient.getPostsByIds(postIds);
            if (postListDto.isEmpty()) {
                throw new PostNotFoundException("Posts not found");
            }
            return postListDto;
        } catch (FeignException e) {
            throw new PostServiceConnectionException("Post server returned an error: " + e.getMessage());
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

    private Hashtag createHashtag(String name, Long authorId) {
        return Hashtag.builder()
                .name(name)
                .postsWithHashtag(new ArrayList<>())
                .userId(authorId)
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

    private List<Long> findPostIdsByHashtags(List<Hashtag> hashtags) {
        return hashtags.stream()
                .flatMap(hashtag -> hashtag.getPostsWithHashtag().stream()
                        .map(PostHashtag::getPostId))
                .distinct()
                .toList();
    }
}
