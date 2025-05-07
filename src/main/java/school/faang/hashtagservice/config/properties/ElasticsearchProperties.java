package school.faang.hashtagservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.elasticsearch")
public record ElasticsearchProperties(
        String host,
        int port,
        String protocol
) {
}
