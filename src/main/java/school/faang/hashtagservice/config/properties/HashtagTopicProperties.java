package school.faang.hashtagservice.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class HashtagTopicProperties {

    @Value("${spring.kafka.topics.hashtag.name}")
    private String name;

    @Value("${spring.kafka.topics.hashtag.partitions}")
    private int partitions;

    @Value("${spring.kafka.topics.hashtag.replicas}")
    private int replicas;
}
