package school.faang.hashtagservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.exception.JsonDeserializationException;
import school.faang.hashtagservice.service.HashtagService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagAddingEventListener {

    private final HashtagService hashtagService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.data.kafka.topics.hashtag-adding.name}",
            groupId = "${spring.data.kafka.consumer.group-id}"
    )
    public void receive(String message) {
        try {
            log.debug("Received new hashtag event: {}", message);
            HashtagAddingEvent event = objectMapper.readValue(message, HashtagAddingEvent.class);
            hashtagService.linkHashtagOnPost(event);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Deserialization json %s to event object error", message);
        }
    }
}
