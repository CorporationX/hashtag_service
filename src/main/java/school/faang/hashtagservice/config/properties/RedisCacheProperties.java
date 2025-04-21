package school.faang.hashtagservice.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RedisCacheProperties {

    @Value("${cache-config.top-hashtags-minutes}")
    private int topHashtagMinutes;

    @Value("${cache-config.top-hashtags-key}")
    private String topHashtagsKey;

    @Value("${cache-config.hashtag-filters-minutes}")
    private int filtersMinutes;

    @Value("${cache-config.hashtag-filters-key}")
    private String filtersKey;
}
