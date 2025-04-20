package school.faang.hashtagservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.dto.client.UserDto;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.exception.PostServiceConnectionException;
import school.faang.hashtagservice.exception.UserNotFoundException;
import school.faang.hashtagservice.exception.UserServiceConnectionException;
import school.faang.hashtagservice.filter.HashtagFilter;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final UserServiceClient userClient;
    private final PostServiceClient postClient;
    private final UserContext userContext;
    private final HashtagMapper hashtagMapper;
    private final List<HashtagFilter> filters;

    @Value("${cleaner-config.days}")
    private int days;

    public void addHashtags(HashtagStringsDto hashtagDto) {
        checkUserExists();
        hashtagDto.hashtagNames().stream()
                .map(this::createHashtag)
                .filter(hashtag -> isHashtagNotExists(hashtag.getName()))
                .forEach(hashtagRepository::save);
    }

    public List<HashtagResponseDto> getHashtagsByIds(List<Long> hashtagIds) {
        return hashtagMapper.toDtoList(hashtagRepository.findAllByIdIn(hashtagIds));
    }

    public List<HashtagResponseDto> getHashtagsByFilters(HashtagFilterDto filter) {
        Specification<Hashtag> specifications = filters.stream()
                .filter(hashtagFilter -> hashtagFilter.isApplicable(filter))
                .map(hashtagFilter -> hashtagFilter.apply(filter))
                .reduce(Specification::and)
                .orElse(null);

        List<Hashtag> hashtags = specifications != null ? hashtagRepository.findAll(specifications)
                : hashtagRepository.findAll();

        return hashtagMapper.toDtoList(hashtags.stream()
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

    public void linkHashtagOnPost(HashtagAddingEvent event) {
        if (isHashtagNotExists(event.hashtagName())) {
            hashtagRepository.save(createHashtag(event.hashtagName()));
        }
        Hashtag hashtag = hashtagRepository.findByName(event.hashtagName());
        List<PostHashtag> posts = hashtag.getPostsWithHashtag();
        posts.add(createPostHashtag(event.postId(), hashtag));
        hashtag.setPostsWithHashtag(posts);
        hashtagRepository.save(hashtag);
    }

    public void unlinkHashtagOnPost(Long postId) {
        List<Hashtag> hashtags = hashtagRepository.findAllByPostsWithHashtagId(postId);
        hashtags.stream()
                .peek(hashtag -> {
                    List<PostHashtag> posts = hashtag.getPostsWithHashtag();
                    posts.removeIf(post -> post.getPostId().equals(postId));
                    hashtag.setPostsWithHashtag(posts);
                })
                .forEach(hashtagRepository::save);
    }

    public List<PostResponseDto> getPostsByHashtagIds(List<Long> hashtagIds) {
        List<Hashtag> hashtags = hashtagRepository.findAllByIdIn(hashtagIds);
        List<Long> postIds = hashtags.stream()
                .flatMap(hashtag -> hashtag.getPostsWithHashtag().stream()
                        .map(PostHashtag::getPostId))
                .distinct()
                .toList();
        return getPostsOnPostClient(postIds);
    }

    @Async("unusedHashtagCleaner")
    public void clearUnusedHashtags() {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
        List<Hashtag> hashtags = hashtagRepository.findAllByPostsWithHashtagEmptyAndCreatedAtBefore(dateTime);
        hashtags.removeIf(hashtag -> hashtag.getPostsWithHashtag().isEmpty());
        hashtagRepository.deleteAll(hashtags);
    }

    private void checkUserExists() {
        try {
            Long userId = userContext.getUserId();
            UserDto userDto = userClient.getUser(userId);
            if (userDto == null) {
                throw new UserNotFoundException("User with id %d not found", userId);
            }
        } catch (FeignException e) {
            throw new UserServiceConnectionException("User server returned an error: " + e.getMessage());
        }
    }

    private List<PostResponseDto> getPostsOnPostClient(List<Long> postIds) {
        try {
            return postClient.getPostsByIds(postIds);
        } catch (FeignException e) {
            throw new PostServiceConnectionException("Post server returned an error: " + e.getMessage());
        }
    }

    private boolean isHashtagNotExists(String hashtagName) {
        return !hashtagRepository.existsByName(hashtagName);
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
}
