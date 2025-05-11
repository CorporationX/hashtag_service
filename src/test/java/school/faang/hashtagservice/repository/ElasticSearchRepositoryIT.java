package school.faang.hashtagservice.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.util.PostgresAndElasticContainersConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ElasticSearchRepositoryIT extends PostgresAndElasticContainersConfig {

    private final Long id = 13L;
    private final String name = "name";
    private final LocalDateTime date = LocalDateTime.now();
    private final Long userId = 100L;

    @Autowired
    private ElasticSearchHashtagRepository elasticRepository;

    @Autowired
    private ElasticsearchClient elasticClient;

    @Test
    void testPositiveSaveOnElasticsearchDatabase() throws IOException {
        HashtagSmartDto hashtag = createSmartHashtag();
        elasticRepository.save(hashtag);

        var response = elasticClient.get(
                builder -> builder.index("hashtags").id("13"), HashtagSmartDto.class);

        assertTrue(response.found());
        assertEquals(hashtag.name(), response.source() != null ? response.source().name() : null);
    }

    @Test
    void testPositiveFindHashtagsByFilters() throws InterruptedException, IOException {
        Hashtag hashtag = createHashtag();
        HashtagFilterDto filterDto = createFilter();
        IndexRequest<Hashtag> request = IndexRequest.of(builder -> builder
                .index("hashtags")
                .id("13")
                .document(hashtag)
        );

        elasticClient.index(request);
        Thread.sleep(1000);

        List<Hashtag> result = elasticRepository.findHashtagsByFilters(filterDto);

        assertFalse(result.isEmpty());
        assertEquals(name, result.get(0).getName());
    }

    private HashtagSmartDto createSmartHashtag() {
        return HashtagSmartDto.builder()
                .id(id)
                .name(name)
                .createdAt(date)
                .postIds(List.of(1L, 2L))
                .userId(userId)
                .build();
    }

    private Hashtag createHashtag() {
        return Hashtag.builder()
                .id(id)
                .name(name)
                .createdAt(date)
                .postsWithHashtag(new ArrayList<>())
                .userId(userId)
                .build();
    }

    private HashtagFilterDto createFilter() {
        return HashtagFilterDto.builder()
                .keyword("n")
                .fromDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .toDate(LocalDateTime.of(2050, 1, 1, 0, 0))
                .build();
    }
}
