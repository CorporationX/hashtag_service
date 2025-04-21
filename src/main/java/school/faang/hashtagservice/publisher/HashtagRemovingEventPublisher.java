package school.faang.hashtagservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.exception.JsonSerializationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagRemovingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.hashtag-notification.name}")
    private String topic;

    public void publish(HashtagRemovingEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException("Serialization object %s in json error", event.toString());
        }
    }
}
