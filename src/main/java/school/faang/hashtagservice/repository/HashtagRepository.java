package school.faang.hashtagservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.faang.hashtagservice.model.Hashtag;

import java.time.LocalDateTime;
import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    boolean existsByName(String name);

    Hashtag findByName(String name);

    @Query("SELECT h FROM Hashtag h WHERE SIZE(h.postsWithHashtag) = 0 AND h.createdAt < :dateTime")
    List<Hashtag> findAllByPostsWithHashtagEmptyAndCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT h.id FROM Hashtag h LEFT JOIN h.postsWithHashtag p " +
            "GROUP BY h.id ORDER BY COUNT(p) DESC, h.name ASC")
    List<Long> findTopPopularHashtagIds(Pageable pageable);

    @Query("SELECT DISTINCT h FROM Hashtag h LEFT JOIN FETCH h.postsWithHashtag WHERE h.id IN :ids")
    List<Hashtag> findWithPostsByIds(@Param("ids") List<Long> ids);
}
