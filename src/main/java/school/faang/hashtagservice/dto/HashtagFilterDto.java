package school.faang.hashtagservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HashtagFilterDto(
        String keyword,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {
}
