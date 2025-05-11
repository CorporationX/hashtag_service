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
        return createTopic(hashtagAddingTopic.name(),
                hashtagAddingTopic.partitions(),
                hashtagAddingTopic.replicas());
    }

    @Bean
    public NewTopic hashtagRemovingTopic() {
        return createTopic(hashtagRemovingTopic.name(),
                hashtagRemovingTopic.partitions(),
                hashtagRemovingTopic.replicas());
    }

    @Bean
    public NewTopic hashtagAnalyticsTopic() {
        return createTopic(hashtagAnalyticsTopic.name(),
                hashtagAnalyticsTopic.partitions(),
                hashtagAnalyticsTopic.replicas());
    }

    @Bean
    public NewTopic hashtagNotificationTopic() {
        return createTopic(hashtagNotificationTopic.name(),
                hashtagNotificationTopic.partitions(),
                hashtagNotificationTopic.replicas());
    }

    @Bean
    public NewTopic hashtagAchievementTopic() {
        return createTopic(hashtagAchievementTopic.name(),
                hashtagAchievementTopic.partitions(),
                hashtagAchievementTopic.replicas());
    }

    private NewTopic createTopic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
