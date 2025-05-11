package school.faang.hashtagservice.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.HashtagFilterDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class HashtagToDateFilter implements HashtagFilter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public boolean isApplicable(HashtagFilterDto filter) {
        return filter.toDate() != null;
    }

    @Override
    public void apply(BoolQuery.Builder boolQuery, HashtagFilterDto filter) {
        boolQuery.filter(builder -> builder
                .range(range -> range
                        .field("createdAt")
                        .lte(JsonData.of(formatDate(filter.toDate())))
                )
        );
    }

    private String formatDate(LocalDateTime date) {
        return date.format(FORMATTER);
    }
}
