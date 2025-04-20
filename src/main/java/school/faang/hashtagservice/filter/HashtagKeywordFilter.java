package school.faang.hashtagservice.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.model.Hashtag;

@Component
public class HashtagKeywordFilter implements HashtagFilter {

    @Override
    public boolean isApplicable(HashtagFilterDto filter) {
        return filter.keyword() != null && !filter.keyword().isBlank();
    }

    @Override
    public Specification<Hashtag> apply(HashtagFilterDto filter) {
        String pattern = "%" + filter.keyword().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_") + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("name")), pattern, '\\');
    }
}
