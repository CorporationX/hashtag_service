package school.faang.hashtagservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "post-service", url = "${post-service.host}:${post-service.port}")
public interface PostServiceClient {

}
