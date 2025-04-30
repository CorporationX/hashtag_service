package school.faang.hashtagservice.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.HashtagFilterDto;

@Component
@RequiredArgsConstructor
public class HashtagKeywordFilter implements HashtagFilter {

    @Override
    public boolean isApplicable(HashtagFilterDto filter) {
        return filter.keyword() != null && !filter.keyword().isBlank();
    }

    @Override
    public void apply(BoolQuery.Builder boolQuery, HashtagFilterDto filter) {
        boolQuery.must(must -> must
                .wildcard(wildcard -> wildcard
                        .field("name")
                        .value("*" + filter.keyword() + "*")
                )
        );
    }
}
