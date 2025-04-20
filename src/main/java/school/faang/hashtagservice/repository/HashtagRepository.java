package school.faang.hashtagservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import school.faang.hashtagservice.model.Hashtag;

import java.time.LocalDateTime;
import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long>, JpaSpecificationExecutor<Hashtag> {

    boolean existsByName(String name);

    Hashtag findByName(String name);

    List<Hashtag> findAllByIdIn(List<Long> hashtagIds);

    List<Hashtag> findAllByPostsWithHashtagId(Long postId);

    List<Hashtag> findAllByPostsWithHashtagIdIn(List<Long> postIds);

    List<Hashtag> findAllByPostsWithHashtagEmptyAndCreatedAtBefore(LocalDateTime dateTime);
}
