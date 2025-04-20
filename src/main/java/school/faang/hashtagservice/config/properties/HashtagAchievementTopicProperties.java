package school.faang.hashtagservice.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class HashtagAchievementTopicProperties {

    @Value("${spring.kafka.topics.hashtag-achievement.name}")
    private String name;

    @Value("${spring.kafka.topics.hashtag-achievement.partitions}")
    private int partitions;

    @Value("${spring.kafka.topics.hashtag-achievement.replicas}")
    private int replicas;
}
