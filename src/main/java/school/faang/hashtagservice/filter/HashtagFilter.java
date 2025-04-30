package school.faang.hashtagservice.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import school.faang.hashtagservice.dto.HashtagFilterDto;

public interface HashtagFilter {

    boolean isApplicable(HashtagFilterDto filter);

    void apply(BoolQuery.Builder boolQuery, HashtagFilterDto filter);
}
