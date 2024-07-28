package faang.school.hashtagservice.repository;

import faang.school.hashtagservice.model.hashtag.Hashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Hashtag findByName(String name);

    List<Hashtag> findByNameIn(List<String> hashtagNames);

    boolean existsByName(String name);

    @Query("SELECT h FROM Hashtag h JOIN h.posts p GROUP BY h.id ORDER BY COUNT(p.id) DESC")
    List<Hashtag> findPopularHashtags(Pageable pageable);
}
