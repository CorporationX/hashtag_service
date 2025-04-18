package school.faang.hashtagservice.dto.event;

import lombok.Builder;

@Builder
public record HashtagEvent(
        String hashtagName,
        Long postId
) {
}
