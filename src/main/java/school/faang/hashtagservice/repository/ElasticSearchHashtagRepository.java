package school.faang.hashtagservice.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.exception.ElasticsearchConnectionException;
import school.faang.hashtagservice.filter.HashtagFilter;
import school.faang.hashtagservice.model.Hashtag;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static school.faang.hashtagservice.constants.HashtagConstants.ELASTIC_ERROR_MESSAGE;

@Repository
@RequiredArgsConstructor
public class ElasticSearchHashtagRepository {

    private final ElasticsearchClient elasticClient;
    private final List<HashtagFilter> filters;

    public void save(HashtagSmartDto hashtag) throws IOException {
        IndexRequest<HashtagSmartDto> request = IndexRequest.of(builder -> builder
                .index("hashtags")
                .id(hashtag.id().toString())
                .document(hashtag)
        );
        elasticClient.index(request);
    }

    public List<Hashtag> findHashtagsByFilters(HashtagFilterDto filter) {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            for (HashtagFilter filterComponent : filters) {
                if (filterComponent.isApplicable(filter)) {
                    filterComponent.apply(boolQuery, filter);
                }
            }

            SearchResponse<Hashtag> response = elasticClient.search(search -> search
                            .index("hashtags")
                            .query(query -> query.bool(boolQuery.build()))
                    , Hashtag.class);

            return extractHashtags(response);
        } catch (IOException e) {
            throw new ElasticsearchConnectionException(ELASTIC_ERROR_MESSAGE, e.getMessage());
        }
    }

    private List<Hashtag> extractHashtags(SearchResponse<Hashtag> response) {
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}
