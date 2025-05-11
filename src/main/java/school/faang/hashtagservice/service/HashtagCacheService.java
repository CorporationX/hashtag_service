package school.faang.hashtagservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.util.List;

@Service
@Slf4j
@CacheConfig(cacheNames = "hashtag")
@RequiredArgsConstructor
public class HashtagCacheService {

    private final HashtagRepository hashtagRepository;
    private final HashtagCachingHelper cachingHelper;

    @Value("${cache-config.top-hashtags-limit}")
    private int topHashtagsLimit;

    @Async("hashtagCacheExecutor")
    @CacheEvict(allEntries = true)
    public void recalculateHashtagsCache() {
        Pageable page = PageRequest.of(0, topHashtagsLimit);
        List<Long> ids = hashtagRepository.findTopPopularHashtagIds(page);

        if (ids.isEmpty()) {
            log.info("No hashtags found");
            return;
        }
        List<Hashtag> hashtags = hashtagRepository.findWithPostsByIds(ids);
        hashtags.forEach(cachingHelper::cacheHashtag);
    }
}
