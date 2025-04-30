package school.faang.hashtagservice.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class HashtagRemovingTopicProperties {

    @Value("${spring.data.kafka.topics.hashtag-removing.name}")
    private String name;

    @Value("${spring.data.kafka.topics.hashtag-removing.partitions}")
    private int partitions;

    @Value("${spring.data.kafka.topics.hashtag-removing.replicas}")
    private int replicas;
}
