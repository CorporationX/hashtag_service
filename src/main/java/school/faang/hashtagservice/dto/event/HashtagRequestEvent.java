package school.faang.hashtagservice.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HashtagRequestEvent(
        Long userId,
        Long hashtagId,
        LocalDateTime receivedAt
) {
}
