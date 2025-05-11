package school.faang.hashtagservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.service.HashtagService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = HashtagController.class)
public class HashtagControllerTest {

    private final List<Long> postIds = List.of(1L, 2L);
    private final List<HashtagResponseDto> responses = List.of(
            createHashtagResponse("name"), createHashtagResponse("name 2")
    );
    private final List<Long> hashtagIds = List.of(1L, 2L);

    @MockBean
    private HashtagService hashtagService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPositiveAddHashtags() throws Exception {
        HashtagStringsDto hashtagDto = createHashtagDto();
        doNothing().when(hashtagService).addHashtags(hashtagDto);

        mockMvc.perform(post("/hashtags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hashtagDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testPositiveGetHashtagsByIds() throws Exception {
        when(hashtagService.getHashtagsByIds(hashtagIds)).thenReturn(responses);

        String hashtagIdsString = convertListToString(hashtagIds);

        mockMvc.perform(get("/hashtags/all")
                        .param("hashtagIds", hashtagIdsString))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }

    @Test
    void testPositiveGetHashtagsByFilters() throws Exception {
        HashtagFilterDto filter = createFilter();
        when(hashtagService.getHashtagsByFilters(filter)).thenReturn(responses);

        mockMvc.perform(post("/hashtags/filtered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }

    @Test
    void testPositiveGetHashtagsIdsByPostId() throws Exception {
        Long postId = postIds.get(0);
        when(hashtagService.getHashtagsIdsByPostId(postId)).thenReturn(hashtagIds);

        mockMvc.perform(get("/hashtags/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(hashtagIds)));
    }

    @Test
    void testPositiveGetHashtagsIdsByPostIds() throws Exception {
        Map<Long, List<Long>> groupingHashtagsByPost = Map.of(
                postIds.get(0), hashtagIds, postIds.get(1), hashtagIds
        );
        when(hashtagService.getHashtagsIdsByPostIds(postIds)).thenReturn(groupingHashtagsByPost);

        String postIdsString = convertListToString(postIds);

        mockMvc.perform(get("/hashtags")
                        .param("postIds", postIdsString))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(groupingHashtagsByPost)));
    }

    @Test
    void testPositiveGetPostsByHashtagIds() throws Exception {
        List<PostResponseDto> posts = List.of(
                createPostResponse("content 1"), createPostResponse("content 2")
        );
        when(hashtagService.getPostsByHashtagIds(hashtagIds)).thenReturn(posts);

        String hashtagIdsString = convertListToString(hashtagIds);

        mockMvc.perform(get("/hashtags/posts")
                        .param("hashtagIds", hashtagIdsString))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(posts)));
    }

    private HashtagStringsDto createHashtagDto() {
        return HashtagStringsDto.builder()
                .hashtagNames(List.of("name 1", "name 2"))
                .build();
    }

    private HashtagFilterDto createFilter() {
        return HashtagFilterDto.builder()
                .fromDate(LocalDateTime.of(2000, 1, 1, 0, 0))
                .toDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                .keyword("n")
                .build();
    }

    private HashtagResponseDto createHashtagResponse(String name) {
        return HashtagResponseDto.builder()
                .name(name)
                .build();
    }

    private PostResponseDto createPostResponse(String content) {
        return PostResponseDto.builder()
                .content(content)
                .build();
    }

    private String convertListToString(List<Long> ids) {
        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
