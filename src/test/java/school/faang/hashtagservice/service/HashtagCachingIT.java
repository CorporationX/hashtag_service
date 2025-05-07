package school.faang.hashtagservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.repository.HashtagRepository;
import school.faang.hashtagservice.util.PostgresAndRedisContainersConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class HashtagCachingIT extends PostgresAndRedisContainersConfig {

    private final Hashtag hashtag = createHashtag();

    @Autowired
    private HashtagCachingHelper cachingHelper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        hashtagRepository.save(hashtag);
    }

    @AfterEach
    void tearDown() {
        hashtagRepository.deleteAll();
    }

    @Test
    void testPositiveHashtagPutCache() {
        HashtagSmartDto expected = cachingHelper.cacheHashtag(hashtag);

        Object cached = redisTemplate.opsForValue().get("hashtag::" + hashtag.getId());
        assertNotNull(cached);
        HashtagSmartDto actual = objectMapper.convertValue(cached, HashtagSmartDto.class);
        assertEquals(expected, actual);
    }

    private Hashtag createHashtag() {
        return Hashtag.builder()
                .name("name")
                .userId(100L)
                .build();
    }
}
