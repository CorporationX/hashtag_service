package school.faang.hashtagservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HashtagSmartDataMapper {


    @Mapping(target = "postIds", source = "postsWithHashtag", qualifiedByName = "mapByIds")
    HashtagSmartDto toSmartDto(Hashtag hashtag);

    @Named("mapByIds")
    default List<Long> mapByIds(List<PostHashtag> posts) {
        return posts != null
                ? posts.stream()
                .map(PostHashtag::getId)
                .toList()
                : null;
    }
}
