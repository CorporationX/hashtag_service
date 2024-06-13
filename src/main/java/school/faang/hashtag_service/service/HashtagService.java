package school.faang.hashtag_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.hashtag_service.dto.PostEvent;
import school.faang.hashtag_service.jpa.HashtagJpaRepository;
import school.faang.hashtag_service.model.Hashtag;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagJpaRepository hashtagJpaRepository;
    private final CacheManager cacheManager;

    @Transactional
    public void parsePostAndCreateHashtags(PostEvent postEvent) {
        log.info("Parse post content with ID: {}", postEvent.getPostId());
        List<String> tags = getHashtags(postEvent.getContent());
        tags.stream()
                .map(tag -> Hashtag.builder()
                        .postId(postEvent.getPostId())
                        .name(tag)
                        .build())
                .forEach(this::create);
    }

    private void create(Hashtag hashtag) {
        log.info("Save hashtag = {}", hashtag);
        Objects.requireNonNull(cacheManager.getCache("posts")).evict(hashtag.getName());
        hashtagJpaRepository.save(hashtag);
    }

    private List<String> getHashtags(String content) {
        return Arrays.stream(content.split("[.,!;: ]+"))
                .filter(word -> word.startsWith("#"))
                .map(word -> word.substring(1))
                .toList();
    }
}
