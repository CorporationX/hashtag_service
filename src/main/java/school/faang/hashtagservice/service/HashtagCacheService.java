package school.faang.hashtagservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HashtagCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashtagRepository hashtagRepository;

    @Value("${cache-config.top-hashtags-key}")
    private String topHashtagsKey;

    @Value("${cache-config.top-hashtags-limit}")
    private int topHashtagsLimit;

    @Async("hashtagCacheExecutor")
    @CacheEvict(value = "${cache-config.top-hashtags-key}", allEntries = true)
    public void recalculateHashtagsCache() {
        List<Hashtag> popularHashtags = hashtagRepository.findTopPopularHashtags(topHashtagsLimit);
        redisTemplate.opsForValue().set(topHashtagsKey, popularHashtags);
    }

    @Cacheable(value = "${cache-config.top-hashtags-key}", unless = "#result == null || #result.isEmpty()")
    public List<Hashtag> getPopularHashtags() {
        return hashtagRepository.findTopPopularHashtags(topHashtagsLimit);
    }


}
