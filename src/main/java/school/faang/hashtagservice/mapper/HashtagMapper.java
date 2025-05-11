package school.faang.hashtagservice.mapper;

import org.mapstruct.Mapper;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.model.Hashtag;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HashtagMapper {

    HashtagResponseDto toDto(Hashtag entity);

    List<HashtagResponseDto> toDtoList(List<Hashtag> entityList);
}
