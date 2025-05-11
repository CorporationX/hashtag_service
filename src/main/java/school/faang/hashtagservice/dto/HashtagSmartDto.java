package school.faang.hashtagservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record HashtagSmartDto(
        Long id,
        String name,
        LocalDateTime createdAt,
        List<Long> postIds,
        Long userId
) {
}
