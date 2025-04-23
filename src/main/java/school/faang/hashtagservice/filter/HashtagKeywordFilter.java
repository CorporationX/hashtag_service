package school.faang.hashtagservice.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.repository.ElasticsearchHashtagRepository;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HashtagKeywordFilter implements HashtagFilter {

    private final ElasticsearchHashtagRepository elasticRepository;

    @Override
    public boolean isApplicable(HashtagFilterDto filter) {
        return filter.keyword() != null && !filter.keyword().isBlank();
    }

    @Override
    public List<Hashtag> apply(HashtagFilterDto filter) throws IOException {
        return elasticRepository.findAllByKeyword(filter.keyword());
    }
}
