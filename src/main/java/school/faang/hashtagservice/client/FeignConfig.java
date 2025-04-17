package school.faang.hashtagservice.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.faang.hashtagservice.config.context.UserContext;

@Configuration
public class FeignConfig {

    @Bean
    public FeignUserInterceptor feignUserInterceptor(UserContext userContext) {
        return new FeignUserInterceptor(userContext);
    }
}
