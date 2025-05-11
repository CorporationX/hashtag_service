package school.faang.hashtagservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record HashtagStringsDto(

        @NotEmpty(message = "Must be minimum 1 hashtag name")
        List<
                @NotBlank(message = "Name cannot be blank")
                @Size(min = 2, max = 100, message = "Name size must be between {min} and {max} symbols")
                String
        > hashtagNames
) {
}
