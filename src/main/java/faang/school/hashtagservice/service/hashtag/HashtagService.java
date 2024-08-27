package faang.school.hashtagservice.service.hashtag;

import faang.school.hashtagservice.client.PostServiceClient;
import faang.school.hashtagservice.dto.post.PostDto;
import faang.school.hashtagservice.model.Hashtag;
import faang.school.hashtagservice.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Value("${spring.data.cache.hashtag-cache-key}")
    private String hashtagKey;


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

    @Transactional(readOnly = true)
    public List<Hashtag> getHashtagsByNames(List<String> hashtagNames) {
        return hashtagRepository.findByNameIn(hashtagNames);
    }

    @Transactional(readOnly = true)
    public Hashtag getHashtagByName(String hashtagName) {
        return hashtagRepository.findByName(hashtagName);
    }

    public List<PostDto> findPostsByHashtag(String hashtagName) {
        List<Long> cachedPostIds = (List<Long>) redisTemplate.opsForValue().get(hashtagKey + ":" + hashtagName);

        if (cachedPostIds != null) {
            return postServiceClient.getPostsByIds(cachedPostIds);
        }

        return new ArrayList<>();
    }
}