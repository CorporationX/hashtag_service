package school.faang.hashtagservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.repository.HashtagRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final UserServiceClient userClient;
    private final PostServiceClient postClient;
    private final UserContext userContext;


}
