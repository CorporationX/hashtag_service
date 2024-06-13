package school.faang.hashtag_service.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import school.faang.hashtag_service.model.Hashtag;

public interface HashtagJpaRepository extends JpaRepository<Hashtag, Long> {
}
