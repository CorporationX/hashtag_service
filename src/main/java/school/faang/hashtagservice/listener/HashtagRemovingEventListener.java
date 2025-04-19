package school.faang.hashtagservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.exception.JsonDeserializationException;
import school.faang.hashtagservice.service.HashtagService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagRemovingEventListener {

    private final HashtagService hashtagService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.hashtag-removing.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void receive(String message) {
        try {
            log.debug("Received new post id: {}", message);
            Long postId = objectMapper.readValue(message, Long.class);
            hashtagService.unlinkHashtagOnPost(postId);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Deserialization json %s to event object error", message);
        }
    }
}
