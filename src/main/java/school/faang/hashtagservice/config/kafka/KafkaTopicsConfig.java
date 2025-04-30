package school.faang.hashtagservice.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import school.faang.hashtagservice.config.properties.HashtagAchievementTopicProperties;
import school.faang.hashtagservice.config.properties.HashtagAddingTopicProperties;
import school.faang.hashtagservice.config.properties.HashtagAnalyticsTopicProperties;
import school.faang.hashtagservice.config.properties.HashtagNotificationTopicProperties;
import school.faang.hashtagservice.config.properties.HashtagRemovingTopicProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {

    private final HashtagAddingTopicProperties hashtagAddingTopic;
    private final HashtagRemovingTopicProperties hashtagRemovingTopic;
    private final HashtagAnalyticsTopicProperties hashtagAnalyticsTopic;
    private final HashtagNotificationTopicProperties hashtagNotificationTopic;
    private final HashtagAchievementTopicProperties hashtagAchievementTopic;

    @Bean
    public NewTopic hashtagAddingTopic() {
        return createTopic(hashtagAddingTopic.getName(),
                hashtagAddingTopic.getPartitions(),
                hashtagAddingTopic.getReplicas());
    }

    @Bean
    public NewTopic hashtagRemovingTopic() {
        return createTopic(hashtagRemovingTopic.getName(),
                hashtagRemovingTopic.getPartitions(),
                hashtagRemovingTopic.getReplicas());
    }

    @Bean
    public NewTopic hashtagAnalyticsTopic() {
        return createTopic(hashtagAnalyticsTopic.getName(),
                hashtagAnalyticsTopic.getPartitions(),
                hashtagAnalyticsTopic.getReplicas());
    }

    @Bean
    public NewTopic hashtagNotificationTopic() {
        return createTopic(hashtagNotificationTopic.getName(),
                hashtagNotificationTopic.getPartitions(),
                hashtagNotificationTopic.getReplicas());
    }

    @Bean
    public NewTopic hashtagAchievementTopic() {
        return createTopic(hashtagAchievementTopic.getName(),
                hashtagAchievementTopic.getPartitions(),
                hashtagAchievementTopic.getReplicas());
    }

    private NewTopic createTopic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
