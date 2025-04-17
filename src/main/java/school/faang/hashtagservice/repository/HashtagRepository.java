package school.faang.hashtagservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.faang.hashtagservice.model.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
}
