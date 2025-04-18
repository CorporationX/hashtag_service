package school.faang.hashtagservice.filter;

import org.springframework.data.jpa.domain.Specification;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.model.Hashtag;

public interface HashtagFilter {

    boolean isApplicable(HashtagFilterDto filter);

    Specification<Hashtag> apply(HashtagFilterDto filter);
}
