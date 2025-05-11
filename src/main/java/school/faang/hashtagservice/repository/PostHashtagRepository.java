package school.faang.hashtagservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.faang.hashtagservice.model.PostHashtag;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    List<PostHashtag> findAllByPostId(Long postId);

    List<PostHashtag> findAllByPostIdIn(List<Long> postIds);

    @Modifying
    @Query("delete from PostHashtag ph where ph.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
