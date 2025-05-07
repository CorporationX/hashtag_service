package school.faang.hashtagservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.service.HashtagService;
import school.faang.hashtagservice.util.PostgresContainerConfig;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "${spring.data.kafka.topics.hashtag-adding.name}",
                "${spring.data.kafka.topics.hashtag-removing.name}",
        },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9094",
                "port=9094",
                "auto.create.topics.enable=true"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HashtagEventListenerIT extends PostgresContainerConfig {

    private final Long id = 1L;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private HashtagService hashtagService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.data.kafka.topics.hashtag-adding.name}")
    private String hashtagAddingTopic;

    @Value("${spring.data.kafka.topics.hashtag-removing.name}")
    private String hashtagRemovingTopic;

    @Test
    void testPositiveHashtagAddingEventListening() throws JsonProcessingException {
        HashtagAddingEvent event = createAddingEvent();
        String message = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(hashtagAddingTopic, message);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> verify(hashtagService, times(1))
                        .linkHashtagOnPost(any(HashtagAddingEvent.class)));
    }

    @Test
    void testPositiveHashtagRemovingEventListening() throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(id);

        kafkaTemplate.send(hashtagRemovingTopic, message);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> verify(hashtagService, times(1))
                        .unlinkHashtagOnPost(any(Long.class)));
    }

    private HashtagAddingEvent createAddingEvent() {
        return HashtagAddingEvent.builder()
                .hashtagName("hashtag")
                .postId(id)
                .authorId(id)
                .build();
    }
}
