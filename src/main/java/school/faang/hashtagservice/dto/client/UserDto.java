package school.faang.hashtagservice.dto.client;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String username
) {
}
