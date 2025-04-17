package school.faang.hashtagservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.hashtagservice.service.HashtagService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;
}
