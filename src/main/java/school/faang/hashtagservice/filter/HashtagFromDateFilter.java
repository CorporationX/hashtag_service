package school.faang.hashtagservice.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.model.Hashtag;

@Component
public class HashtagFromDateFilter implements HashtagFilter {

    @Override
    public boolean isApplicable(HashtagFilterDto filter) {
        return filter.fromDate() != null;
    }

    @Override
    public Specification<Hashtag> apply(HashtagFilterDto filter) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("createdAt"), filter.fromDate());
    }
}
