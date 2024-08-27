package faang.school.hashtagservice.service.cashe;


import faang.school.hashtagservice.client.PostServiceClient;
import faang.school.hashtagservice.dto.post.PostDto;
import faang.school.hashtagservice.model.Hashtag;
import faang.school.hashtagservice.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final PostServiceClient postServiceClient;
    private final HashtagRepository hashtagRepository;

    @Value("${spring.data.redis.cache.expiration:3600}")
    private long cacheExpiration;

    @Value("${spring.data.cache.size.hashtag}")
    private int cacheHashtagSize;

    @Value("${spring.data.cache.size.post}")
    private int cachePostSize;

    @Value("${spring.data.cache.hashtag-cache-key}")
    private String hashtagCacheKey;

    @Value("${spring.data.cache.post-cache-key}")
    private String postCacheKey;

    public void cachePostsByHashtag(String hashtag, List<Long> postIds) {
        redisTemplate.opsForValue().set(hashtagCacheKey + ":" + hashtag, (Serializable) postIds, cacheExpiration,
                TimeUnit.SECONDS);
    }

    public void cachePost(PostDto postDto) {
        redisTemplate.opsForValue().set(postCacheKey + ":" + postDto.getId(), postDto, cacheExpiration,
                TimeUnit.SECONDS);
    }

    @Transactional
    public void initializeCache() {
        List<Hashtag> popularHashtags = hashtagRepository.findPopularHashtags(PageRequest.of(0, cacheHashtagSize));
        popularHashtags.forEach(hashtag -> {
            List<PostDto> posts = postServiceClient.findPostsByHashtag(hashtag.getName(), 0, cachePostSize);

            List<Long> postIds = new ArrayList<>();
            posts.forEach(post -> {
                cachePost(post);
                postIds.add(post.getId());
            });

            cachePostsByHashtag(hashtag.getName(), postIds);
        });
    }

    public void clearCache() {
        log.info("Clearing all keys from Redis");
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}