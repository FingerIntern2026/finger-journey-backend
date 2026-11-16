package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.wiki.response.WikiCategoryListResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiItemDetailResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiSearchResponseDto;
import com.finger.fingerjourneybackend.entity.Media;
import com.finger.fingerjourneybackend.entity.WikiCategory;
import com.finger.fingerjourneybackend.entity.WikiItem;
import com.finger.fingerjourneybackend.entity.WikiMedia;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.MediaRepository;
import com.finger.fingerjourneybackend.repository.WikiCategoryRepository;
import com.finger.fingerjourneybackend.repository.WikiItemRepository;
import com.finger.fingerjourneybackend.repository.WikiMediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikiServiceTest {

    @Mock
    private WikiCategoryRepository wikiCategoryRepository;
    @Mock
    private WikiItemRepository wikiItemRepository;
    @Mock
    private WikiMediaRepository wikiMediaRepository;
    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private WikiService wikiService;

    // ───── 목록 ─────

    @Test
    void listGroupsItemsUnderTheirCategoryKeepingOrder() {
        when(wikiCategoryRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(category(1L, "회사생활 꿀팁", 1), category(2L, "회사 지도", 2), category(3L, "빈 카테고리", 3)));
        // 리포지토리가 노출 순서대로 돌려준 전체 항목
        when(wikiItemRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(item(10L, 1L, "복합기 사용법", "본문", 1), item(11L, 2L, "회의실 위치", "본문", 1), item(12L, 1L, "택배 받는 법", "본문", 2)));

        List<WikiCategoryListResponseDto> response = wikiService.getWikiList();

        assertThat(response).extracting(WikiCategoryListResponseDto::getCategoryName)
                .containsExactly("회사생활 꿀팁", "회사 지도", "빈 카테고리");
        assertThat(response.get(0).getItems()).extracting(item -> item.getItemId()).containsExactly(10L, 12L);
        assertThat(response.get(1).getItems()).extracting(item -> item.getItemId()).containsExactly(11L);
        assertThat(response.get(2).getItems()).isEmpty();
        assertThat(response.get(0).getItems().get(0).getIcon()).isEqualTo("📌");
    }

    // ───── 검색 ─────

    @Test
    void searchSortsByCategoryOrderThenItemOrder() {
        when(wikiCategoryRepository.findAll()).thenReturn(List.of(category(1L, "뒤 카테고리", 2), category(2L, "앞 카테고리", 1)));
        when(wikiItemRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("검색", "검색"))
                .thenReturn(List.of(item(1L, 1L, "a", "검색", 1), item(2L, 2L, "b", "검색", 2), item(3L, 2L, "c", "검색", 1)));

        List<WikiSearchResponseDto> response = wikiService.search("검색");

        assertThat(response).extracting(WikiSearchResponseDto::getItemId).containsExactly(3L, 2L, 1L);
        assertThat(response).extracting(WikiSearchResponseDto::getCategoryName)
                .containsExactly("앞 카테고리", "앞 카테고리", "뒤 카테고리");
    }

    @Test
    void searchTrimsTheKeywordBeforeQuerying() {
        when(wikiCategoryRepository.findAll()).thenReturn(List.of());
        when(wikiItemRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("복합기", "복합기"))
                .thenReturn(List.of());

        assertThat(wikiService.search("  복합기 ")).isEmpty();

        verify(wikiItemRepository).findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("복합기", "복합기");
    }

    @Test
    void searchRejectsBlankKeyword() {
        assertError(() -> wikiService.search("   "), ErrorCode.INVALID_REQUEST);
        assertError(() -> wikiService.search(null), ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(wikiItemRepository);
    }

    @Test
    void snippetStartsAtTheBeginningWhenTheKeywordIsAtTheStartAndCutsTheEnd() {
        String content = "복합기에서 사번을 입력한 뒤 출력, 복사, 스캔 중 필요한 기능을 선택해주세요. 용지가 없으면 관리자에게 문의해주세요.";

        String snippet = searchOne("복합기", "복합기 사용법", content).getSnippet();

        assertThat(snippet).startsWith("복합기에서").endsWith("...").doesNotStartWith("...");
        assertThat(snippet.length()).isLessThanOrEqualTo(53);
    }

    @Test
    void snippetShowsTextAroundAKeywordFoundDeepInTheContent() {
        String content = "가".repeat(30) + "KEYWORD" + "나".repeat(60);

        String snippet = searchOne("keyword", "제목", content).getSnippet();

        assertThat(snippet).startsWith("...").contains("KEYWORD").endsWith("...");
    }

    @Test
    void snippetStartsFromTheContentWhenOnlyTheTitleMatches() {
        String snippet = searchOne("제목", "제목에만 있어요", "짧은 본문").getSnippet();

        assertThat(snippet).isEqualTo("짧은 본문");
    }

    @Test
    void snippetCollapsesLineBreaksAndRepeatedSpaces() {
        String snippet = searchOne("첫", "제목", "첫 줄\n\n  둘째 줄").getSnippet();

        assertThat(snippet).isEqualTo("첫 줄 둘째 줄");
    }

    // ───── 상세 ─────

    @Test
    void detailReturnsImageUrlsInLinkOrder() {
        when(wikiItemRepository.findById(5L)).thenReturn(Optional.of(item(5L, 1L, "복합기 사용법", "본문", 1)));
        when(wikiMediaRepository.findByWikiItemIdOrderByDisplayOrderAsc(5L))
                .thenReturn(List.of(link(5L, 12L, 1), link(5L, 10L, 2)));
        // findAllById는 순서를 보장하지 않으므로 일부러 연결 순서와 다르게 돌려준다.
        when(mediaRepository.findAllById(List.of(12L, 10L)))
                .thenReturn(List.of(media(10L, "/media/b.png"), media(12L, "/media/a.png")));

        WikiItemDetailResponseDto response = wikiService.getItem(5L);

        assertThat(response.getTitle()).isEqualTo("복합기 사용법");
        assertThat(response.getContent()).isEqualTo("본문");
        assertThat(response.getMediaUrl()).containsExactly("/media/a.png", "/media/b.png");
        // 주소와 ID는 같은 순서로 짝이 맞아야 한다.
        assertThat(response.getMediaId()).containsExactly(12L, 10L);
    }

    @Test
    void detailReturnsEmptyImageListWhenNothingIsAttached() {
        when(wikiItemRepository.findById(5L)).thenReturn(Optional.of(item(5L, 1L, "제목", "본문", 1)));
        when(wikiMediaRepository.findByWikiItemIdOrderByDisplayOrderAsc(5L)).thenReturn(List.of());
        when(mediaRepository.findAllById(List.of())).thenReturn(List.of());

        assertThat(wikiService.getItem(5L).getMediaUrl()).isEmpty();
    }

    @Test
    void detailSkipsAttachmentsWhoseMediaIsMissing() {
        when(wikiItemRepository.findById(5L)).thenReturn(Optional.of(item(5L, 1L, "제목", "본문", 1)));
        when(wikiMediaRepository.findByWikiItemIdOrderByDisplayOrderAsc(5L))
                .thenReturn(List.of(link(5L, 10L, 1), link(5L, 11L, 2)));
        when(mediaRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(media(11L, "/media/only.png")));

        WikiItemDetailResponseDto response = wikiService.getItem(5L);

        assertThat(response.getMediaUrl()).containsExactly("/media/only.png");
        // 건너뛴 이미지의 ID도 함께 빠져서 주소와 짝이 맞는다.
        assertThat(response.getMediaId()).containsExactly(11L);
    }

    @Test
    void detailFailsWhenTheItemDoesNotExist() {
        when(wikiItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> wikiService.getItem(999L), ErrorCode.WIKI_ITEM_NOT_FOUND);
    }

    // ───── 도우미 ─────

    private WikiSearchResponseDto searchOne(String keyword, String title, String content) {
        when(wikiCategoryRepository.findAll()).thenReturn(List.of(category(1L, "카테고리", 1)));
        when(wikiItemRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword))
                .thenReturn(List.of(item(1L, 1L, title, content, 1)));

        return wikiService.search(keyword).get(0);
    }

    private WikiCategory category(Long id, String name, int displayOrder) {
        WikiCategory category = new WikiCategory();
        category.setWikiCategoryId(id);
        category.setCategoryName(name);
        category.setIcon("🏠");
        category.setDisplayOrder(displayOrder);
        return category;
    }

    private WikiItem item(Long id, Long categoryId, String title, String content, int displayOrder) {
        WikiItem item = new WikiItem();
        item.setWikiItemId(id);
        item.setWikiCategoryId(categoryId);
        item.setTitle(title);
        item.setContent(content);
        item.setIcon("📌");
        item.setDisplayOrder(displayOrder);
        return item;
    }

    private WikiMedia link(Long itemId, Long mediaId, int displayOrder) {
        WikiMedia link = new WikiMedia();
        link.setWikiItemId(itemId);
        link.setMediaId(mediaId);
        link.setDisplayOrder(displayOrder);
        return link;
    }

    private Media media(Long id, String fileUrl) {
        Media media = new Media();
        media.setMediaId(id);
        media.setFileUrl(fileUrl);
        return media;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
