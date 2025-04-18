package school.faang.hashtagservice.dto.error;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String message
) {
}
