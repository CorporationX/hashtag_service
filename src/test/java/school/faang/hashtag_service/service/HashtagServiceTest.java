package school.faang.hashtag_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import school.faang.hashtag_service.dto.PostEvent;
import school.faang.hashtag_service.jpa.HashtagJpaRepository;
import school.faang.hashtag_service.model.Hashtag;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    private static final Long POST_ID = 1L;

    @Mock
    private HashtagJpaRepository hashtagJpaRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private HashtagService hashtagService;

    @Test
    public void whenParsePostAndCreateHashtags() {
        Cache cache = Mockito.mock(Cache.class);

        PostEvent post = PostEvent.builder()
                .postId(POST_ID)
                .content("#Java oop cross platform #language with wide #possibilities")
                .build();

        Hashtag hashtagJava = Hashtag.builder().name("Java").postId(POST_ID).build();
        Hashtag hashtagLanguage = Hashtag.builder().name("language").postId(POST_ID).build();
        Hashtag hashtagPossibilities = Hashtag.builder().name("possibilities").postId(POST_ID).build();

        when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        hashtagService.parsePostAndCreateHashtags(post);

        verify(cache).evict(hashtagJava.getName());
        verify(cache).evict(hashtagLanguage.getName());
        verify(cache).evict(hashtagPossibilities.getName());
        verify(hashtagJpaRepository).save(hashtagJava);
        verify(hashtagJpaRepository).save(hashtagLanguage);
        verify(hashtagJpaRepository).save(hashtagPossibilities);
    }
}