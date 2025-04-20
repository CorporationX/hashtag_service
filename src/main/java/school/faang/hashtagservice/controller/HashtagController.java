package school.faang.hashtagservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.service.HashtagService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;

    @PostMapping
    public ResponseEntity<String> addHashtags(@Valid @RequestBody HashtagStringsDto hashtagDto) {
        hashtagService.addHashtags(hashtagDto);
        return ResponseEntity.ok().body("Hashtags added successfully");
    }

    @GetMapping("/all")
    public List<HashtagResponseDto> getHashtagsByIds(@RequestParam List<Long> hashtagIds) {
        return hashtagService.getHashtagsByIds(hashtagIds);
    }

    @PostMapping("/filtered")
    public List<HashtagResponseDto> getHashtagsByFilters(@RequestBody HashtagFilterDto filter) {
        return hashtagService.getHashtagsByFilters(filter);
    }

    @GetMapping("/{postId}")
    public List<Long> getHashtagsIdsByPostId(@PathVariable Long postId) {
        return hashtagService.getHashtagsIdsByPostId(postId);
    }

    @GetMapping
    public Map<Long, List<Long>> getHashtagsIdsByPostIds(@RequestParam List<Long> postIds) {
        return hashtagService.getHashtagsIdsByPostIds(postIds);
    }

    @GetMapping("/posts")
    public List<PostResponseDto> getPostsByHashtagIds(@RequestParam List<Long> hashtagIds) {
        return hashtagService.getPostsByHashtagIds(hashtagIds);
    }
}
