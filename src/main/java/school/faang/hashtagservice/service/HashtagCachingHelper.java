package school.faang.hashtagservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.mapper.HashtagSmartDataMapper;
import school.faang.hashtagservice.model.Hashtag;

@Service
@CacheConfig(cacheNames = "hashtag")
@RequiredArgsConstructor
public class HashtagCachingHelper {

    private final HashtagSmartDataMapper smartHashtagMapper;

    @CachePut(key = "#hashtag.id")
    public HashtagSmartDto cacheHashtag(Hashtag hashtag) {
        return smartHashtagMapper.toSmartDto(hashtag);
    }
}
