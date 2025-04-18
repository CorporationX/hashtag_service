package school.faang.hashtagservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HashtagResponseDto(
        String name,
        LocalDateTime createdAt
) {
}
