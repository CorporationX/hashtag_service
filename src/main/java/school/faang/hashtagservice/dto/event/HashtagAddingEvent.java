package school.faang.hashtagservice.dto.event;

import lombok.Builder;

@Builder
public record HashtagAddingEvent(
        String hashtagName,
        Long postId
) {
}
