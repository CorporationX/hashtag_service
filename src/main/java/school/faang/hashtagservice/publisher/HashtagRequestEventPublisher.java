package school.faang.hashtagservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.exception.JsonSerializationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagRequestEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.data.kafka.topics.hashtag-analytics.name}")
    private String analyticsTopic;

    @Value("${spring.data.kafka.topics.hashtag-achievement.name}")
    private String achievementTopic;

    public void publish(HashtagRequestEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(analyticsTopic, message);
            kafkaTemplate.send(achievementTopic, message);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException("Serialization object %s in json error", event.toString());
        }
    }
}
