package school.faang.hashtagservice.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import school.faang.hashtagservice.model.Hashtag;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ElasticsearchHashtagRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final ElasticsearchClient elasticClient;

    public void save(Hashtag hashtag) throws IOException {
        IndexRequest<Hashtag> request = IndexRequest.of(builder -> builder
                .index("hashtags")
                .id(hashtag.getId().toString())
                .document(hashtag)
        );
        elasticClient.index(request);
    }

    public List<Hashtag> findAllByKeyword(String keyword) throws IOException {
        SearchResponse<Hashtag> response = elasticClient.search(search -> search
                        .index("hashtags")
                        .query(query -> query
                                .match(match -> match
                                        .field("name")
                                        .query(keyword)
                                        .fuzziness("1"))), Hashtag.class);

        return extractHashtags(response);
    }

    public List<Hashtag> findAllByDateTo(LocalDateTime toDate) throws IOException {
        SearchResponse<Hashtag> response = elasticClient.search(search -> search
                        .index("hashtags")
                        .query(query -> query
                                .range(range -> range
                                        .field("createdAt")
                                        .lte(JsonData.of(formatDate(toDate))))), Hashtag.class);

        return extractHashtags(response);
    }

    public List<Hashtag> findAllByDateFrom(LocalDateTime fromDate) throws IOException {
        SearchResponse<Hashtag> response = elasticClient.search(search -> search
                        .index("hashtags")
                        .query(query -> query
                                .range(range -> range
                                        .field("createdAt")
                                        .gte(JsonData.of(formatDate(fromDate))))), Hashtag.class);

        return extractHashtags(response);
    }

    private List<Hashtag> extractHashtags(SearchResponse<Hashtag> response) {
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    private String formatDate(LocalDateTime date) {
        return date.format(FORMATTER);
    }
}
