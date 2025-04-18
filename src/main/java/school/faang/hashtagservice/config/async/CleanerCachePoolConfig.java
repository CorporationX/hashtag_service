package school.faang.hashtagservice.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CleanerCachePoolConfig {

    @Value("${thread-pool.cleaner-cache-pool.size}")
    private int poolSize;

    @Value("${thread-pool.cleaner-cache-pool.shutdown-timeout-seconds}")
    private int shutdownTimeoutSeconds;

    @Bean(name = "hashtagCacheCleaner")
    public ThreadPoolTaskExecutor createHashtagCacheCleanerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setThreadNamePrefix("CleanerHashtagCachePool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(shutdownTimeoutSeconds);
        executor.initialize();
        return executor;
    }
}
