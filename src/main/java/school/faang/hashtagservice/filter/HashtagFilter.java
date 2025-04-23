package school.faang.hashtagservice.filter;

import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.model.Hashtag;

import java.io.IOException;
import java.util.List;

public interface HashtagFilter {

    boolean isApplicable(HashtagFilterDto filter);

    List<Hashtag> apply(HashtagFilterDto filter) throws IOException;
}
