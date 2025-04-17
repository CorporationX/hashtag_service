package school.faang.hashtagservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url = "${user-service.host}:${user-service.port}")
public interface UserServiceClient {

}
