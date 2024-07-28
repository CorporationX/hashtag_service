package faang.school.hashtagservice.mapper;

import faang.school.hashtagservice.dto.post.PostDto;
import faang.school.hashtagservice.model.hashtag.Hashtag;
import faang.school.hashtagservice.model.post.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    @Mapping(source = "hashtags", target = "hashtagNames", qualifiedByName = "hashtagToHashtagName")
    PostDto toDto(Post post);

    @Mapping(source = "hashtags", target = "hashtagNames", qualifiedByName = "hashtagToHashtagName")
    List<PostDto> toDto(List<Post> posts);

    @Named("hashtagToHashtagName")
    default String hashtagToHashtagName(Hashtag hashtag) {
        return hashtag.getName();
    }
}
