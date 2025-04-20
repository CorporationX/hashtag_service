package school.faang.hashtagservice.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HashtagRemovingEvent(
        Long userId,
        String hashtagName,
        LocalDateTime removedAt
) {
}
