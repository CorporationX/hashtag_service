package faang.school.hashtagservice.client;

import faang.school.hashtagservice.dto.post.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "${post-service.host}:${post-service.port}")
public interface PostServiceClient {

    @GetMapping("/post/{postId}")
    PostDto getPostById(@PathVariable Long postId);
}
