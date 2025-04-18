package school.faang.hashtagservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.service.HashtagCacheService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagCacheCleaner {

    private final HashtagCacheService hashtagCacheService;

    @Scheduled(cron = "${cron.hashtag-cache-clean}")
    public void clearHashtagsCache() {
        log.debug("Clearing hashtags cache start on {}", LocalDateTime.now());
        hashtagCacheService.clearHashtagsCache();
        log.debug("Clearing hashtags cache end on {}", LocalDateTime.now());
    }
}
