package school.faang.hashtagservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.event.HashtagEvent;
import school.faang.hashtagservice.filter.HashtagFilter;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final UserServiceClient userClient;
    private final PostServiceClient postClient;
    private final UserContext userContext;
    private final HashtagMapper hashtagMapper;
    private final List<HashtagFilter> filters;

    public void addHashtags(HashtagStringsDto hashtagDto) {

    }

    public List<HashtagResponseDto> getHashtagsByIds(List<Long> hashtagIds) {
        return null;
    }

    public List<HashtagResponseDto> getHashtagsByFilters(HashtagFilterDto filter) {
        return null;
    }

    public List<Long> getHashtagsIdsByPostId(Long postId) {
        return null;
    }

    public List<Long> getHashtagsIdsByPostIds(List<Long> postIds) {
        return null;
    }

    public void linkHashtagOnPost(HashtagEvent event) {

    }

    public void unlinkHashtagOnPost(HashtagEvent event) {

    }

    @Async("unusedHashtagCleaner")
    public void clearUnusedHashtags() {

    }
}
