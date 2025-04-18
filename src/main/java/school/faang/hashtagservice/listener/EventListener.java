package school.faang.hashtagservice.listener;

import school.faang.hashtagservice.dto.event.HashtagEvent;

public interface EventListener {

    void receiveEvent(HashtagEvent event);
}
