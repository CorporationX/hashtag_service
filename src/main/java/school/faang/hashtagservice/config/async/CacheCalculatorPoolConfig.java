package school.faang.hashtagservice.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CacheCalculatorPoolConfig {

    @Value("${thread-pool.cache-calculator-pool.size}")
    private int poolSize;

    @Value("${thread-pool.cache-calculator-pool.shutdown-timeout-seconds}")
    private int shutdownTimeoutSeconds;

    @Bean(name = "hashtagCacheExecutor")
    public ThreadPoolTaskExecutor createHashtagCacheRecalculateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setThreadNamePrefix("HashtagCacheRecalculatePool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(shutdownTimeoutSeconds);
        executor.initialize();
        return executor;
    }
}
