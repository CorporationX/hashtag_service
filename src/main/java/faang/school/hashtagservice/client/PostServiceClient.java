package faang.school.hashtagservice.client;

import faang.school.hashtagservice.annotation.ValidHashtag;
import faang.school.hashtagservice.dto.post.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "post-service", url = "${post-service.host}:${post-service.port}")
public interface PostServiceClient {

    @GetMapping("/post/hashtag/cache")
    List<PostDto> findPostsByHashtag(@RequestParam @ValidHashtag String hashtagName,
                                     @RequestParam int page,
                                     @RequestParam int size);

    @GetMapping("/post/list/ids")
    List<PostDto> getPostsByIds(@RequestParam List<Long> postIds);
}
