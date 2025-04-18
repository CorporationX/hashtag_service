package school.faang.hashtagservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HashtagCacheService {

    @Async("hashtagCacheExecutor")
    public void clearHashtagsCache() {

    }
}
