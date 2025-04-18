package school.faang.hashtagservice.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CleanerUnusedHashtagPoolConfig {

    @Value("${thread-pool.cleaner-hashtag-pool.size}")
    private int poolSize;

    @Value("${thread-pool.cleaner-hashtag-pool.shutdown-timeout-seconds}")
    private int shutdownTimeoutSeconds;

    @Bean(name = "unusedHashtagCleaner")
    public ThreadPoolTaskExecutor createUnusedHashtagCleanerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setThreadNamePrefix("CleanerUnusedHashtagPool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(shutdownTimeoutSeconds);
        executor.initialize();
        return executor;
    }
}
