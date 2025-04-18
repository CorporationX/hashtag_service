package school.faang.hashtagservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.event.HashtagEvent;
import school.faang.hashtagservice.service.HashtagService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagDeletingEventListener implements EventListener {

    private final ObjectMapper objectMapper;
    private final HashtagService hashtagService;

    @Override
    public void receiveEvent(HashtagEvent event) {
        hashtagService.unlinkHashtagOnPost(event);
    }
}
