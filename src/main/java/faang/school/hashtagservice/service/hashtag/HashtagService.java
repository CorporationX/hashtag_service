package faang.school.hashtagservice.service.hashtag;

import faang.school.hashtagservice.client.PostServiceClient;
import faang.school.hashtagservice.dto.post.PostDto;
import faang.school.hashtagservice.model.hashtag.Hashtag;
import faang.school.hashtagservice.repository.HashtagRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final PostServiceClient postServiceClient;


    @Transactional
    public void save(String hashtagName) {
        if (!hashtagRepository.existsByName(hashtagName)) {
            Hashtag hashtag = Hashtag.builder()
                    .name(hashtagName)
                    .build();
            hashtagRepository.save(hashtag);
        }
    }

    @Transactional
    public void saveAllHashtags(List<String> hashtagNames) {
        hashtagNames.forEach(hashtagName -> {
            if (!hashtagRepository.existsByName(hashtagName)) {
                Hashtag hashtag = Hashtag.builder()
                        .name(hashtagName)
                        .build();
                hashtagRepository.save(hashtag);
            }
        });
    }

    public List<Hashtag> getHashtagsByNames(List<String> hashtagNames) {
        return hashtagRepository.findByNameIn(hashtagNames);
    }

    public Hashtag getHashtagByName(String hashtagName) {
        return hashtagRepository.findByName(hashtagName);
    }

    public List<PostDto> findPostsByHashtag(String hashtagName) {
        List<Long> cachedPostIds = (List<Long>) redisTemplate.opsForValue().get("hashtag:" + hashtagName);

        if (cachedPostIds != null) {
            List<PostDto> postDtos = new ArrayList<>();

            for (Long postId : cachedPostIds) {
                PostDto cachedPost = (PostDto) redisTemplate.opsForValue().get("post:" + postId);
                if (cachedPost != null) {
                    postDtos.add(cachedPost);
                } else {
                    postDtos.add(getPostById(postId));
                }
            }
            return postDtos;
        }
        return new ArrayList<>();
    }

    @Retryable(retryFor = FeignException.class, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    private PostDto getPostById(Long postId) {
        PostDto postDto = postServiceClient.getPostById(postId);
        log.info("PostDto with ID {} was successfully received", postId);
        return postDto;
    }

    @Recover
    public void recover(FeignException e, Long id) {
        throw new EntityNotFoundException("PostDto with ID " + id + " not found");
    }
}