package school.faang.hashtagservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Hashtag API", description = "API для управления хэш-тегами постов")
public class HashtagController {

    private final HashtagService hashtagService;

    @PostMapping
    @Operation(summary = "Добавить хэш-тег", description = "Добавляет хэш-теги по переданному списку названий")
    public ResponseEntity<String> addHashtags(@Valid @RequestBody HashtagStringsDto hashtagDto) {
        hashtagService.addHashtags(hashtagDto);
        return ResponseEntity.ok().body("Hashtags added successfully");
    }

    @GetMapping("/all")
    @Operation(summary = "Найти хэш-теги", description = "Находит хэш-теги по переданному списку идентификаторов")
    public List<HashtagResponseDto> getHashtagsByIds(@Parameter(description = "Список идентификаторов хэш-тегов")
                                                         @RequestParam List<Long> hashtagIds) {
        return hashtagService.getHashtagsByIds(hashtagIds);
    }

    @PostMapping("/filtered")
    @Operation(summary = "Найти хэш-теги по фильтрам", description = "Находит хэш-теги по переданным фильтрам")
    public List<HashtagResponseDto> getHashtagsByFilters(@RequestBody HashtagFilterDto filter) {
        return hashtagService.getHashtagsByFilters(filter);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Найти хэш-теги по посту",
            description = "Находит хэш-теги по переданному идентификатору поста")
    public List<Long> getHashtagsIdsByPostId(@Parameter(description = "Идентификатор поста")
                                                 @PathVariable Long postId) {
        return hashtagService.getHashtagsIdsByPostId(postId);
    }

    @GetMapping
    @Operation(summary = "Найти хэш-теги по постам",
            description = "Находит хэш-теги по переданному списку идентификаторов постов")
    public Map<Long, List<Long>> getHashtagsIdsByPostIds(@Parameter(description = "Список идентификаторов постов")
                                                             @RequestParam List<Long> postIds) {
        return hashtagService.getHashtagsIdsByPostIds(postIds);
    }

    @GetMapping("/posts")
    @Operation(summary = "Найти посты по хэш-тегам",
            description = "Находит посты по переданному списку идентификаторов хэш-тегов")
    public List<PostResponseDto> getPostsByHashtagIds(@Parameter(description = "Список идентификаторов хэш-тегов")
                                                          @RequestParam List<Long> hashtagIds) {
        return hashtagService.getPostsByHashtagIds(hashtagIds);
    }
}
