package school.faang.hashtagservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.service.HashtagService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagCacheCleaner {

    private final HashtagService hashtagService;

    @Scheduled(cron = "${cron.hashtag-cache-clean}")
    public void cleanHashtagsCache() {
        log.debug("Cleaning hashtags cache start on {}", LocalDateTime.now());
    }
}
