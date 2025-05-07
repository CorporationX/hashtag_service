package school.faang.hashtagservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.kafka.topics.hashtag-removing")
public record HashtagRemovingTopicProperties(
        String name,
        int partitions,
        int replicas
) {
}
