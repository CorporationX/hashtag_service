package school.faang.hashtagservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.hashtagservice.client.PostServiceClient;
import school.faang.hashtagservice.client.UserServiceClient;
import school.faang.hashtagservice.config.context.UserContext;
import school.faang.hashtagservice.dto.HashtagFilterDto;
import school.faang.hashtagservice.dto.HashtagResponseDto;
import school.faang.hashtagservice.dto.HashtagStringsDto;
import school.faang.hashtagservice.dto.client.PostResponseDto;
import school.faang.hashtagservice.dto.client.UserDto;
import school.faang.hashtagservice.dto.event.HashtagAddingEvent;
import school.faang.hashtagservice.dto.event.HashtagRemovingEvent;
import school.faang.hashtagservice.dto.event.HashtagRequestEvent;
import school.faang.hashtagservice.exception.UserNotFoundException;
import school.faang.hashtagservice.filter.HashtagFilter;
import school.faang.hashtagservice.mapper.HashtagMapper;
import school.faang.hashtagservice.model.Hashtag;
import school.faang.hashtagservice.model.PostHashtag;
import school.faang.hashtagservice.publisher.HashtagRemovingEventPublisher;
import school.faang.hashtagservice.publisher.HashtagRequestEventPublisher;
import school.faang.hashtagservice.repository.ElasticsearchHashtagRepository;
import school.faang.hashtagservice.repository.HashtagRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ElasticsearchHashtagRepository elasticRepository;

    @Mock
    private UserServiceClient userClient;

    @Mock
    private PostServiceClient postClient;

    @Mock
    private UserContext userContext;

    @Spy
    private HashtagMapper hashtagMapper;

    @Mock
    private HashtagFilter fromDateFilter;

    @Mock
    private HashtagFilter toDateFilter;

    @Mock
    private HashtagFilter keywordFilter;

    @Mock
    private HashtagRequestEventPublisher requestEventPublisher;

    @Mock
    private HashtagRemovingEventPublisher removingEventPublisher;

    @Mock
    private HashtagCacheService cacheService;

    @Mock
    private Executor unusedHashtagCleaner;

    @BeforeEach
    void setUp() {
        hashtagService = new HashtagService(hashtagRepository, elasticRepository, userClient, postClient, userContext,
                hashtagMapper, List.of(fromDateFilter, toDateFilter, keywordFilter), requestEventPublisher,
                removingEventPublisher, cacheService, unusedHashtagCleaner);
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
        Hashtag hashtag = createHashtag(firstName);
        when(userContext.getUserId()).thenReturn(id);
        when(userClient.getUser(id)).thenReturn(user);
        when(hashtagRepository.existsByName(firstName)).thenReturn(false);
        hashtag.setCreatedAt(null);
        when(hashtagRepository.save(hashtag)).thenReturn(hashtag);

        hashtagService.addHashtags(dto);

        verify(hashtagRepository, times(1)).save(hashtag);
        verify(elasticRepository, times(1)).save(hashtag);
    }

    @Test
    void testPositiveGetHashtagsByIds() {
        when(cacheService.getPopularHashtags()).thenReturn(Collections.emptyList());
        when(hashtagRepository.findAllByIdIn(ids)).thenReturn(hashtags);
        when(hashtagMapper.toDtoList(hashtags)).thenReturn(responses);

        List<HashtagResponseDto> result = hashtagService.getHashtagsByIds(ids);

        verify(requestEventPublisher, times(2)).publish(any(HashtagRequestEvent.class));
        assertEquals(result, responses);
    }

    @Test
    void testPositiveGetHashtagsByFilters() throws IOException {
        HashtagFilterDto filter = createFilter();
        when(fromDateFilter.isApplicable(filter)).thenReturn(true);
        when(toDateFilter.isApplicable(filter)).thenReturn(true);
        when(keywordFilter.isApplicable(filter)).thenReturn(true);
        when(toDateFilter.apply(filter)).thenReturn(hashtags);
        when(fromDateFilter.apply(filter)).thenReturn(hashtags);
        when(keywordFilter.apply(filter)).thenReturn(hashtags);
        when(hashtagMapper.toDtoList(hashtags)).thenReturn(responses);

        List<HashtagResponseDto> result = hashtagService.getHashtagsByFilters(filter);

        assertEquals(result, responses);
    }

    @Test
    void testPositiveGetHashtagsIdsByPostId() {
        when(hashtagRepository.findAllByPostsWithHashtagId(id)).thenReturn(hashtags);

        List<Long> hashtagIds = hashtagService.getHashtagsIdsByPostId(id);

        assertEquals(hashtagIds.size(), hashtags.size());
    }

    @Test
    void testPositiveGetHashtagsIdsByPostIds() {
        Map<Long, List<Long>> groupingHashtagsByPost = Map.of(ids.get(0), ids, ids.get(1), ids);
        when(hashtagRepository.findAllByPostsWithHashtagIdIn(ids)).thenReturn(hashtagList);

        Map<Long, List<Long>> result = hashtagService.getHashtagsIdsByPostIds(ids);

        assertEquals(result, groupingHashtagsByPost);
    }

    @Test
    void testPositiveLinkHashtagOnPost() throws IOException {
        HashtagAddingEvent event = createAddingEvent(ids.get(0));
        when(hashtagRepository.existsByName(firstName)).thenReturn(true);
        when(hashtagRepository.findByName(firstName)).thenReturn(hashtags.get(0));

        hashtagService.linkHashtagOnPost(event);

        verify(hashtagRepository, times(1)).save(hashtags.get(0));
        verify(elasticRepository, times(1)).save(hashtags.get(0));
    }

    @Test
    void testPositiveUnlinkHashtagOnPost() throws IOException {
        List<Hashtag> newHashtagList = new ArrayList<>(hashtagList.subList(0, 2));
        when(hashtagRepository.findAllByPostsWithHashtagId(ids.get(0))).thenReturn(newHashtagList);

        hashtagService.unlinkHashtagOnPost(ids.get(0));

        verify(hashtagRepository, times(2)).save(any(Hashtag.class));
        verify(elasticRepository, times(2)).save(any(Hashtag.class));
    }

    @Test
    void testPositiveGetPostsByHashtagIds() {
        List<PostResponseDto> responses = List.of(createPostResponse(ids), createPostResponse(ids));
        when(cacheService.getPopularHashtags()).thenReturn(Collections.emptyList());
        when(hashtagRepository.findAllByIdIn(ids)).thenReturn(hashtagList);
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
}
