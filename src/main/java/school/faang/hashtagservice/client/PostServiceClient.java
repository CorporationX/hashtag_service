package school.faang.hashtagservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.faang.hashtagservice.dto.client.PostResponseDto;

import java.util.List;

@FeignClient(name = "post-service", url = "${post-service.host}:${post-service.port}")
public interface PostServiceClient {

    @GetMapping("/posts")
    List<PostResponseDto> getPostsByIds(@RequestParam List<Long> postIds);
}
