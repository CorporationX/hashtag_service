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
public class HashtagUnusedCleaner {

    private final HashtagService hashtagService;

    @Scheduled(cron = "${cron.unused-hashtag-clean}")
    public void cleanUnusedHashtags() {
        log.debug("Cleaning unused hashtags start on {}", LocalDateTime.now());
    }
}
