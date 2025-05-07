package school.faang.hashtagservice.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.util.PostgresContainerConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PostgresJpaRepositoryIT extends PostgresContainerConfig {

    private final List<Long> postIds = List.of(1L, 2L, 3L);
    private final List<Hashtag> hashtags = List.of(
            createHashtag("name 1"), createHashtag("name 2"), createHashtag("name 3")
    );
    private final List<PostHashtag> postHashtags = List.of(
            createPostHashtag(postIds.get(0), hashtags.get(0)), createPostHashtag(postIds.get(0), hashtags.get(1)),
            createPostHashtag(postIds.get(0), hashtags.get(2)), createPostHashtag(postIds.get(1), hashtags.get(0)),
            createPostHashtag(postIds.get(1), hashtags.get(1)), createPostHashtag(postIds.get(2), hashtags.get(0))
    );
    private final LocalDateTime date = LocalDateTime.now().plusDays(1);

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @BeforeEach
    void setUp() {
        hashtagRepository.deleteAll();
        hashtagRepository.saveAll(hashtags);
        postHashtagRepository.saveAll(postHashtags);
    }

    @AfterEach
    void tearDown() {
        hashtags.forEach(hashtag -> hashtag.setPostsWithHashtag(null));
        hashtagRepository.saveAll(hashtags);
        postHashtagRepository.deleteAll(postHashtags);
    }

    @Test
    void testPositiveFindAllByPostsWithHashtagEmpty() {
        List<Hashtag> hashtagsWithoutPosts = List.of(
                createHashtag("name 4"), createHashtag("name 5")
        );
        hashtagRepository.saveAll(hashtagsWithoutPosts);

        List<Hashtag> result = hashtagRepository.findAllByPostsWithHashtagEmptyAndCreatedAtBefore(date);

        assertEquals(2, result.size());
        assertEquals(hashtagsWithoutPosts.get(0).getName(), result.get(0).getName());
        assertEquals(hashtagsWithoutPosts.get(0).getPostsWithHashtag().size(),
                result.get(0).getPostsWithHashtag().size());
        assertEquals(hashtagsWithoutPosts.get(1).getName(), result.get(1).getName());
        assertEquals(hashtagsWithoutPosts.get(1).getPostsWithHashtag().size(),
                result.get(1).getPostsWithHashtag().size());
    }

    @Test
    void testPositiveFindTopPopularHashtagIds() {
        List<Long> result = hashtagRepository.findTopPopularHashtagIds(Pageable.ofSize(2));

        Hashtag firstHashtag = hashtagRepository.findById(result.get(0)).orElseThrow();
        Hashtag secondHashtag = hashtagRepository.findById(result.get(1)).orElseThrow();

        assertEquals(2, result.size());
        assertEquals(firstHashtag.getName(), hashtags.get(0).getName());
        assertEquals(secondHashtag.getName(), hashtags.get(1).getName());
    }

    @Test
    void testPositiveFindWithPostsByIds() {
        List<Long> hashtagIds = new ArrayList<>();
        hashtagIds.add(hashtagRepository.findByName(hashtags.get(0).getName()).getId());
        hashtagIds.add(hashtagRepository.findByName(hashtags.get(1).getName()).getId());
        hashtagIds.add(hashtagRepository.findByName(hashtags.get(2).getName()).getId());

        List<Hashtag> result = hashtagRepository.findWithPostsByIds(hashtagIds);

        assertEquals(result.size(), hashtags.size());
    }

    @Test
    void testPositiveDeleteByPostIdWhenIdNotFound() {
        postHashtagRepository.deleteById(0L);

        List<PostHashtag> result = postHashtagRepository.findAll();
        assertEquals(postHashtags.size(), result.size());
    }

    @Test
    void testPositiveDeleteByPostId() {
        postHashtagRepository.deleteByPostId(postIds.get(0));

        List<PostHashtag> result = postHashtagRepository.findAll();
        assertEquals(3, result.size());
    }

    private Hashtag createHashtag(String name) {
        return Hashtag.builder()
                .name(name)
                .userId(100L)
                .postsWithHashtag(new ArrayList<>())
                .build();
    }

    private PostHashtag createPostHashtag(Long postId, Hashtag hashtag) {
        return PostHashtag.builder()
                .postId(postId)
                .hashtag(hashtag)
                .build();
    }
}
