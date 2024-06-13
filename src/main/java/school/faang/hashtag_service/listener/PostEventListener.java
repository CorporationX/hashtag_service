package school.faang.hashtag_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import school.faang.hashtag_service.dto.PostEvent;
import school.faang.hashtag_service.service.HashtagService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostEventListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final HashtagService hashtagService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            log.info("Received message from channel {}: {}", channel, body);
            PostEvent postEvent = objectMapper.readValue(body, PostEvent.class);
            hashtagService.parsePostAndCreateHashtags(postEvent);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
