package faang.school.hashtagservice.controller;

import faang.school.hashtagservice.annotation.ValidHashtag;
import faang.school.hashtagservice.model.Hashtag;
import faang.school.hashtagservice.dto.hashtag.HashtagRequest;
import faang.school.hashtagservice.dto.hashtag.HashtagResponse;
import faang.school.hashtagservice.dto.post.PostResponse;
import faang.school.hashtagservice.service.hashtag.HashtagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hashtag")
@RequiredArgsConstructor
public class HashtagController {
    public final HashtagService hashtagService;

    @PostMapping
    public void save(@RequestParam @ValidHashtag String hashtagName) {
        hashtagService.save(hashtagName);
    }

    @PostMapping("/list")
    public void saveHashtags(@RequestBody @Valid HashtagRequest request) {
        hashtagService.saveAllHashtags(request.getHashtagNames());
    }

    @GetMapping("/allByNames")
    public HashtagResponse getHashtagsByNames(@RequestBody @ValidHashtag HashtagRequest request) {
        return HashtagResponse.builder()
                .hashtags(hashtagService.getHashtagsByNames(request.getHashtagNames()))
                .build();
    }

    @GetMapping("/name")
    public Hashtag getHashtagByName(@RequestParam @ValidHashtag String hashtagName) {
        return hashtagService.getHashtagByName(hashtagName);
    }

    @PostMapping("/post")
    public PostResponse findPostsByHashtag(@RequestBody @ValidHashtag String hashtagName) {
        return PostResponse.builder().posts(hashtagService.findPostsByHashtag(hashtagName)).build();
    }
}
