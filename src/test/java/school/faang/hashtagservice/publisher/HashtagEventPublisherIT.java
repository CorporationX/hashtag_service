package school.faang.hashtagservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.util.PostgresContainerConfig;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "${spring.data.kafka.topics.hashtag-analytics.name}",
                "${spring.data.kafka.topics.hashtag-achievement.name}",
                "${spring.data.kafka.topics.hashtag-notification.name}",
        },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093",
                "auto.create.topics.enable=true"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HashtagEventPublisherIT extends PostgresContainerConfig {

    private final Long id = 1L;
    private final LocalDateTime date = LocalDateTime.now();

    @Autowired
    private HashtagRemovingEventPublisher hashtagRemovingEventPublisher;

    @Autowired
    private HashtagRequestEventPublisher hashtagRequestEventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private KafkaTemplate<String, String> kafkaTemplateSpy;

    @Value("${spring.data.kafka.topics.hashtag-notification.name}")
    private String hashtagNotificationTopic;

    @Value("${spring.data.kafka.topics.hashtag-analytics.name}")
    private String hashtagAnalyticsTopic;

    @Value("${spring.data.kafka.topics.hashtag-achievement.name}")
    private String hashtagAchievementTopic;

    @Test
    void testPositiveHashtagRemovingEventPublishing() throws JsonProcessingException {
        HashtagRemovingEvent event = createRemovingEvent();
        String expectedJson = objectMapper.writeValueAsString(event);

        hashtagRemovingEventPublisher.publish(event);

        verify(kafkaTemplateSpy, timeout(5000).times(1))
                .send(eq(hashtagNotificationTopic), eq(expectedJson));
    }

    @Test
    void testPositiveHashtagRequestEventPublishing() throws JsonProcessingException {
        HashtagRequestEvent event = createRequestEvent();
        String expectedJson = objectMapper.writeValueAsString(event);

        hashtagRequestEventPublisher.publish(event);

        verify(kafkaTemplateSpy, timeout(5000).times(1))
                .send(eq(hashtagAnalyticsTopic), eq(expectedJson));
        verify(kafkaTemplateSpy, timeout(5000).times(1))
                .send(eq(hashtagAchievementTopic), eq(expectedJson));
    }

    private HashtagRemovingEvent createRemovingEvent() {
        return HashtagRemovingEvent.builder()
                .hashtagName("hashtag")
                .removedAt(date)
                .userId(id)
                .build();
    }

    private HashtagRequestEvent createRequestEvent() {
        return HashtagRequestEvent.builder()
                .userId(id)
                .hashtagId(id)
                .receivedAt(date)
                .build();
    }
}
