package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiCategoryCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiItemCreateResponseDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWikiServiceTest {

    @Mock
    private WikiCategoryRepository wikiCategoryRepository;
    @Mock
    private WikiItemRepository wikiItemRepository;
    @Mock
    private WikiMediaRepository wikiMediaRepository;
    @Mock
    private MediaRepository mediaRepository;

    @Captor
    private ArgumentCaptor<WikiCategory> categoryCaptor;
    @Captor
    private ArgumentCaptor<List<WikiMedia>> linksCaptor;

    @InjectMocks
    private AdminWikiService adminWikiService;

    // ───── 카테고리 ─────

    @Test
    void createCategoryAppendsAfterLastDisplayOrderWhenNotGiven() {
        WikiCategory last = category(3L, "마지막", 3);
        when(wikiCategoryRepository.existsByCategoryName("회사생활 꿀팁")).thenReturn(false);
        when(wikiCategoryRepository.findTopByOrderByDisplayOrderDesc()).thenReturn(Optional.of(last));
        when(wikiCategoryRepository.save(categoryCaptor.capture())).thenAnswer(invocation -> {
            WikiCategory saved = invocation.getArgument(0);
            saved.setWikiCategoryId(4L);
            return saved;
        });

        WikiCategoryCreateResponseDto response = adminWikiService.createCategory(
                createCategoryRequest("  회사생활 꿀팁 ", "🏠", null));

        assertThat(response.getCategoryId()).isEqualTo(4L);
        assertThat(categoryCaptor.getValue().getCategoryName()).isEqualTo("회사생활 꿀팁");
        assertThat(categoryCaptor.getValue().getDisplayOrder()).isEqualTo(4);
    }

    @Test
    void createCategoryFailsWhenNameIsDuplicated() {
        when(wikiCategoryRepository.existsByCategoryName("회사생활 꿀팁")).thenReturn(true);

        assertError(() -> adminWikiService.createCategory(createCategoryRequest("회사생활 꿀팁", "🏠", 1)),
                ErrorCode.WIKI_CATEGORY_DUPLICATE);
        verify(wikiCategoryRepository, never()).save(any());
    }

    @Test
    void updateCategoryChangesOnlyGivenFields() {
        WikiCategory category = category(1L, "기존 이름", 2);
        category.setIcon("🏠");
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(wikiCategoryRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WikiCategoryUpdateRequestDto request = new WikiCategoryUpdateRequestDto();
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "icon", "📌");

        adminWikiService.updateCategory(request);

        assertThat(category.getCategoryName()).isEqualTo("기존 이름");
        assertThat(category.getIcon()).isEqualTo("📌");
        assertThat(category.getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void updateCategoryFailsWhenAnotherCategoryHasTheName() {
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "기존 이름", 1)));
        when(wikiCategoryRepository.existsByCategoryNameAndWikiCategoryIdNot("다른 이름", 1L)).thenReturn(true);

        WikiCategoryUpdateRequestDto request = new WikiCategoryUpdateRequestDto();
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "categoryName", "다른 이름");

        assertError(() -> adminWikiService.updateCategory(request), ErrorCode.WIKI_CATEGORY_DUPLICATE);
        verify(wikiCategoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateCategoryFailsWhenCategoryDoesNotExist() {
        when(wikiCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        WikiCategoryUpdateRequestDto request = new WikiCategoryUpdateRequestDto();
        ReflectionTestUtils.setField(request, "categoryId", 999L);

        assertError(() -> adminWikiService.updateCategory(request), ErrorCode.WIKI_CATEGORY_NOT_FOUND);
    }

    @Test
    void deleteCategoryFailsWhenItHasItems() {
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "이름", 1)));
        when(wikiItemRepository.existsByWikiCategoryId(1L)).thenReturn(true);

        assertError(() -> adminWikiService.deleteCategory(1L), ErrorCode.WIKI_CATEGORY_HAS_CHILDREN);
        verify(wikiCategoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategoryRemovesEmptyCategory() {
        WikiCategory category = category(1L, "이름", 1);
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(wikiItemRepository.existsByWikiCategoryId(1L)).thenReturn(false);

        adminWikiService.deleteCategory(1L);

        verify(wikiCategoryRepository).delete(category);
    }

    // ───── 항목 ─────

    @Test
    void createItemLinksDistinctMediaInRequestedOrder() {
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "이름", 1)));
        when(mediaRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(new Media(), new Media()));
        when(wikiItemRepository.findTopByWikiCategoryIdOrderByDisplayOrderDesc(1L)).thenReturn(Optional.empty());
        when(wikiItemRepository.save(any())).thenAnswer(invocation -> {
            WikiItem saved = invocation.getArgument(0);
            saved.setWikiItemId(7L);
            return saved;
        });

        WikiItemCreateRequestDto request = new WikiItemCreateRequestDto();
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "title", " 복합기 사용법 ");
        ReflectionTestUtils.setField(request, "content", "복합기에서 사번을 입력한 뒤...");
        ReflectionTestUtils.setField(request, "icon", "🖨️");
        ReflectionTestUtils.setField(request, "mediaId", List.of(10L, 11L, 10L));

        WikiItemCreateResponseDto response = adminWikiService.createItem(request);

        assertThat(response.getItemId()).isEqualTo(7L);
        verify(wikiMediaRepository).saveAll(linksCaptor.capture());
        assertThat(linksCaptor.getValue()).extracting(WikiMedia::getMediaId).containsExactly(10L, 11L);
        assertThat(linksCaptor.getValue()).extracting(WikiMedia::getDisplayOrder).containsExactly(1, 2);
        assertThat(linksCaptor.getValue()).extracting(WikiMedia::getWikiItemId).containsOnly(7L);
    }

    @Test
    void createItemFailsWhenCategoryDoesNotExist() {
        when(wikiCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> adminWikiService.createItem(createItemRequest(999L, null)),
                ErrorCode.WIKI_CATEGORY_NOT_FOUND);
        verify(wikiItemRepository, never()).save(any());
    }

    @Test
    void createItemFailsWhenMediaDoesNotExist() {
        when(wikiCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "이름", 1)));
        when(mediaRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(new Media()));

        assertError(() -> adminWikiService.createItem(createItemRequest(1L, List.of(10L, 11L))),
                ErrorCode.RESOURCE_NOT_FOUND);
        verify(wikiItemRepository, never()).save(any());
    }

    @Test
    void updateItemReplacesMediaOnlyWhenProvided() {
        WikiItem item = item(3L, "기존 제목");
        when(wikiItemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(mediaRepository.findAllById(List.of(20L))).thenReturn(List.of(new Media()));
        when(wikiItemRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WikiItemUpdateRequestDto request = new WikiItemUpdateRequestDto();
        ReflectionTestUtils.setField(request, "itemId", 3L);
        ReflectionTestUtils.setField(request, "mediaId", List.of(20L));

        adminWikiService.updateItem(request);

        assertThat(item.getTitle()).isEqualTo("기존 제목");
        InOrder inOrder = inOrder(wikiMediaRepository);
        inOrder.verify(wikiMediaRepository).deleteAllByWikiItemId(3L);
        inOrder.verify(wikiMediaRepository).saveAll(linksCaptor.capture());
        assertThat(linksCaptor.getValue()).extracting(WikiMedia::getMediaId).containsExactly(20L);
    }

    @Test
    void updateItemKeepsMediaWhenNotProvided() {
        WikiItem item = item(3L, "기존 제목");
        when(wikiItemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(wikiItemRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WikiItemUpdateRequestDto request = new WikiItemUpdateRequestDto();
        ReflectionTestUtils.setField(request, "itemId", 3L);
        ReflectionTestUtils.setField(request, "title", "새 제목");

        adminWikiService.updateItem(request);

        assertThat(item.getTitle()).isEqualTo("새 제목");
        verify(wikiMediaRepository, never()).deleteAllByWikiItemId(any());
        verify(wikiMediaRepository, never()).saveAll(any());
    }

    @Test
    void updateItemFailsWhenItemDoesNotExist() {
        when(wikiItemRepository.findById(999L)).thenReturn(Optional.empty());

        WikiItemUpdateRequestDto request = new WikiItemUpdateRequestDto();
        ReflectionTestUtils.setField(request, "itemId", 999L);

        assertError(() -> adminWikiService.updateItem(request), ErrorCode.WIKI_ITEM_NOT_FOUND);
    }

    @Test
    void deleteItemRemovesMediaLinksBeforeTheItem() {
        when(wikiItemRepository.findById(3L)).thenReturn(Optional.of(item(3L, "제목")));

        adminWikiService.deleteItem(3L);

        InOrder inOrder = inOrder(wikiMediaRepository, wikiItemRepository);
        inOrder.verify(wikiMediaRepository).deleteAllByWikiItemId(3L);
        inOrder.verify(wikiItemRepository).deleteById(3L);
    }

    @Test
    void deleteItemFailsWhenItemDoesNotExist() {
        when(wikiItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> adminWikiService.deleteItem(999L), ErrorCode.WIKI_ITEM_NOT_FOUND);
        verify(wikiItemRepository, never()).deleteById(any());
    }

    // ───── 도우미 ─────

    private WikiCategoryCreateRequestDto createCategoryRequest(String name, String icon, Integer displayOrder) {
        WikiCategoryCreateRequestDto request = new WikiCategoryCreateRequestDto();
        ReflectionTestUtils.setField(request, "categoryName", name);
        ReflectionTestUtils.setField(request, "icon", icon);
        ReflectionTestUtils.setField(request, "displayOrder", displayOrder);
        return request;
    }

    private WikiItemCreateRequestDto createItemRequest(Long categoryId, List<Long> mediaIds) {
        WikiItemCreateRequestDto request = new WikiItemCreateRequestDto();
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "title", "제목");
        ReflectionTestUtils.setField(request, "content", "본문");
        ReflectionTestUtils.setField(request, "icon", "📌");
        ReflectionTestUtils.setField(request, "mediaId", mediaIds);
        return request;
    }

    private WikiCategory category(Long id, String name, int displayOrder) {
        WikiCategory category = new WikiCategory();
        category.setWikiCategoryId(id);
        category.setCategoryName(name);
        category.setDisplayOrder(displayOrder);
        return category;
    }

    private WikiItem item(Long id, String title) {
        WikiItem item = new WikiItem();
        item.setWikiItemId(id);
        item.setWikiCategoryId(1L);
        item.setTitle(title);
        item.setContent("본문");
        item.setIcon("📌");
        item.setDisplayOrder(1);
        return item;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
