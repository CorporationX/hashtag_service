package school.faang.hashtagservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagSmartDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.dto.client.UserDto;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.exception.UserNotFoundException;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.mapper.HashtagSmartDataMapper;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.publisher.HashtagRemovingEventPublisher;
import school.faang.hashtagservice.publisher.HashtagRequestEventPublisher;
import school.faang.hashtagservice.repository.ElasticSearchHashtagRepository;
import school.faang.hashtagservice.repository.HashtagRepository;
import school.faang.hashtagservice.repository.PostHashtagRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashtagServiceTest {

    private final Long id = 1L;
    private final String firstName = "hashtag1";
    private final String secondName = "hashtag2";
    private final List<String> hashtagNames = List.of(firstName);
    private final HashtagStringsDto dto = createHashtagStringsDto(hashtagNames);
    private final LocalDateTime createdAt = LocalDateTime.of(2019, 1, 1, 0, 0);
    private final List<Hashtag> hashtags = List.of(createHashtag(firstName), createHashtag(secondName));
    private final List<HashtagResponseDto> responses = List.of(createResponse(firstName), createResponse(secondName));
    private final List<Long> ids = List.of(1L, 2L);
    private final List<Hashtag> hashtagList = List.of(
            createHashtagWithPost(ids.get(0), ids),
            createHashtagWithPost(ids.get(1), ids)
    );

    @InjectMocks
    private HashtagService hashtagService;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private PostHashtagRepository postHashtagRepository;

    @Mock
    private ElasticSearchHashtagRepository elasticRepository;

    @Mock
    private UserServiceClient userClient;

    @Mock
    private PostServiceClient postClient;

    @Mock
    private UserContext userContext;

    @Spy
    private HashtagMapper hashtagMapper;

    @Mock
    private HashtagRequestEventPublisher requestEventPublisher;

    @Mock
    private HashtagRemovingEventPublisher removingEventPublisher;

    @Mock
    private Executor unusedHashtagCleaner;

    @Mock
    private HashtagSmartDataMapper smartHashtagMapper;

    @BeforeEach
    void setUp() {
        hashtagService = new HashtagService(hashtagRepository, postHashtagRepository, elasticRepository,
                userClient, postClient, userContext, hashtagMapper, smartHashtagMapper, requestEventPublisher,
                removingEventPublisher, unusedHashtagCleaner);
    }

    @Test
    void testNegativeAddHashtagsWhenUserNotFound() {
        when(userContext.getUserId()).thenReturn(id);
        when(userClient.getUser(id)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> hashtagService.addHashtags(dto));
    }

    @Test
    void testPositiveAddHashtags() throws IOException {
        UserDto user = createUserDto();
        ArgumentCaptor<Hashtag> hashtagCaptor = ArgumentCaptor.forClass(Hashtag.class);
        ArgumentCaptor<HashtagSmartDto> hashtagSmartCaptor = ArgumentCaptor.forClass(HashtagSmartDto.class);
        when(userContext.getUserId()).thenReturn(id);
        when(userClient.getUser(id)).thenReturn(user);
        when(hashtagRepository.existsByName(firstName)).thenReturn(false);

        hashtagService.addHashtags(dto);

        verify(hashtagRepository, times(1)).save(hashtagCaptor.capture());
        verify(elasticRepository, times(1)).save(hashtagSmartCaptor.capture());

        Hashtag capturedHashtag = hashtagCaptor.getValue();
        assertNotNull(capturedHashtag);
        assertEquals(firstName, capturedHashtag.getName());
    }

    @Test
    void testPositiveGetHashtagsByIds() {
        when(hashtagRepository.findById(ids.get(0))).thenReturn(Optional.of(hashtags.get(0)));
        when(hashtagRepository.findById(ids.get(1))).thenReturn(Optional.of(hashtags.get(1)));
        when(hashtagMapper.toDtoList(hashtags)).thenReturn(responses);

        List<HashtagResponseDto> result = hashtagService.getHashtagsByIds(ids);

        verify(requestEventPublisher, times(2)).publish(any(HashtagRequestEvent.class));
        assertEquals(result, responses);
    }

    @Test
    void testPositiveGetHashtagsByFilters() {
        HashtagFilterDto filter = createFilter();
        when(elasticRepository.findHashtagsByFilters(filter)).thenReturn(hashtags);
        when(hashtagMapper.toDtoList(hashtags)).thenReturn(responses);

        List<HashtagResponseDto> result = hashtagService.getHashtagsByFilters(filter);

        assertEquals(result, responses);
    }

    @Test
    void testPositiveGetHashtagsIdsByPostId() {
        List<PostHashtag> posts = List.of(
                createPostHashtag(id, hashtags.get(0)), createPostHashtag(id, hashtags.get(1))
        );
        when(postHashtagRepository.findAllByPostId(id)).thenReturn(posts);

        List<Long> hashtagIds = hashtagService.getHashtagsIdsByPostId(id);

        assertEquals(hashtagIds.size(), hashtags.size());
    }

    @Test
    void testPositiveGetHashtagsIdsByPostIds() {
        Map<Long, List<Long>> groupingHashtagsByPost = Map.of(ids.get(0), ids, ids.get(1), ids);
        List<PostHashtag> posts = List.of(
                createPostHashtag(ids.get(0), hashtagList.get(0)), createPostHashtag(ids.get(1), hashtagList.get(0)),
                createPostHashtag(ids.get(0), hashtagList.get(1)), createPostHashtag(ids.get(1), hashtagList.get(1))
        );
        when(postHashtagRepository.findAllByPostIdIn(ids)).thenReturn(posts);

        Map<Long, List<Long>> result = hashtagService.getHashtagsIdsByPostIds(ids);

        assertEquals(result, groupingHashtagsByPost);
    }

    @Test
    void testPositiveLinkHashtagOnPost() throws IOException {
        HashtagAddingEvent event = createAddingEvent(ids.get(0));
        HashtagSmartDto smartHashtag = createSmartHashtag(hashtags.get(0));
        when(hashtagRepository.existsByName(firstName)).thenReturn(true);
        when(hashtagRepository.findByName(firstName)).thenReturn(hashtags.get(0));
        when(smartHashtagMapper.toSmartDto(hashtags.get(0))).thenReturn(smartHashtag);

        hashtagService.linkHashtagOnPost(event);

        verify(hashtagRepository, times(1)).save(hashtags.get(0));
        verify(elasticRepository, times(1)).save(any(HashtagSmartDto.class));
    }

    @Test
    void testPositiveUnlinkHashtagOnPost() {
        hashtagService.unlinkHashtagOnPost(ids.get(0));

        verify(postHashtagRepository, times(1)).deleteByPostId(ids.get(0));
    }

    @Test
    void testPositiveGetPostsByHashtagIds() {
        List<PostResponseDto> responses = List.of(createPostResponse(ids), createPostResponse(ids));
        when(hashtagRepository.findById(ids.get(0))).thenReturn(Optional.of(hashtagList.get(0)));
        when(hashtagRepository.findById(ids.get(1))).thenReturn(Optional.of(hashtagList.get(1)));
        when(postClient.getPostsByIds(ids)).thenReturn(responses);

        List<PostResponseDto> result = hashtagService.getPostsByHashtagIds(ids);

        assertEquals(result, responses);
    }

    @Test
    void testPositiveClearUnusedHashtags() {
        when(hashtagRepository.findAllByPostsWithHashtagEmptyAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(hashtags);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(unusedHashtagCleaner).execute(any());

        hashtagService.clearUnusedHashtags();

        verify(hashtagRepository, times(1)).deleteAll(hashtags);
        verify(removingEventPublisher, times(2)).publish(any(HashtagRemovingEvent.class));
    }

    private Hashtag createHashtag(String name) {
        return Hashtag.builder()
                .userId(id)
                .name(name)
                .postsWithHashtag(new ArrayList<>())
                .createdAt(createdAt)
                .build();
    }

    private UserDto createUserDto() {
        return UserDto.builder()
                .id(id)
                .username("username")
                .build();
    }

    private HashtagStringsDto createHashtagStringsDto(List<String> hashtagNames) {
        return HashtagStringsDto.builder()
                .hashtagNames(hashtagNames)
                .build();
    }

    private HashtagResponseDto createResponse(String name) {
        return HashtagResponseDto.builder()
                .name(name)
                .build();
    }

    private HashtagFilterDto createFilter() {
        return HashtagFilterDto.builder()
                .fromDate(LocalDateTime.of(2000, 1, 1, 0, 0))
                .toDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                .keyword("h")
                .build();
    }

    private PostHashtag createPostOnHashtag(Long postId, Hashtag hashtag) {
        return PostHashtag.builder()
                .postId(postId)
                .hashtag(hashtag)
                .build();
    }

    private Hashtag createHashtagWithPost(Long id, List<Long> postIds) {
        Hashtag hashtag = Hashtag.builder()
                .id(id)
                .build();
        hashtag.setPostsWithHashtag(List.of(
                createPostOnHashtag(postIds.get(0), hashtag), createPostOnHashtag(postIds.get(1), hashtag)
        ));
        return hashtag;
    }

    private HashtagAddingEvent createAddingEvent(Long postId) {
        return HashtagAddingEvent.builder()
                .hashtagName(firstName)
                .postId(postId)
                .build();
    }

    private PostResponseDto createPostResponse(List<Long> ids) {
        return PostResponseDto.builder()
                .hashtagsId(ids)
                .build();
    }

    private PostHashtag createPostHashtag(Long postId, Hashtag hashtag) {
        return PostHashtag.builder()
                .postId(postId)
                .hashtag(hashtag)
                .build();
    }

    private HashtagSmartDto createSmartHashtag(Hashtag hashtag) {
        return HashtagSmartDto.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .userId(hashtag.getUserId())
                .createdAt(hashtag.getCreatedAt())
                .postIds(hashtag.getPostsWithHashtag().stream()
                        .map(PostHashtag::getId)
                        .toList())
                .build();
    }
}
