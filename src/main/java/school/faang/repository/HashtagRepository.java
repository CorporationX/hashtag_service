package school.faang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.faang.model.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    boolean existsByContent(String content);
}
