// AdminWikiService
// 역할: 위키 카테고리·항목 등록, 수정, 삭제 (API-ADM-011 ~ 016)
// - 카테고리명은 중복될 수 없고, 하위 항목이 있는 카테고리는 삭제할 수 없다.
// - 항목에는 이미지를 최대 3장 연결하며, 수정 시 이미지는 통째로 교체한다.
// - 항목을 삭제하면 이미지 연결(WikiMedia)만 지우고 Media와 파일은 남긴다.

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiCategoryUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemCreateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.request.WikiItemUpdateRequestDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiCategoryCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiCategoryUpdateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiItemCreateResponseDto;
import com.finger.fingerjourneybackend.dto.admin.response.WikiItemUpdateResponseDto;
import com.finger.fingerjourneybackend.entity.WikiCategory;
import com.finger.fingerjourneybackend.entity.WikiItem;
import com.finger.fingerjourneybackend.entity.WikiMedia;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.MediaRepository;
import com.finger.fingerjourneybackend.repository.WikiCategoryRepository;
import com.finger.fingerjourneybackend.repository.WikiItemRepository;
import com.finger.fingerjourneybackend.repository.WikiMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminWikiService {

    private final WikiCategoryRepository wikiCategoryRepository;
    private final WikiItemRepository wikiItemRepository;
    private final WikiMediaRepository wikiMediaRepository;
    private final MediaRepository mediaRepository;

    // ───── 카테고리 ─────

    @Transactional
    public WikiCategoryCreateResponseDto createCategory(WikiCategoryCreateRequestDto request) {
        String categoryName = request.getCategoryName().trim();

        if (wikiCategoryRepository.existsByCategoryName(categoryName)) {
            throw new CustomException(ErrorCode.WIKI_CATEGORY_DUPLICATE);
        }

        WikiCategory category = new WikiCategory();
        category.setCategoryName(categoryName);
        category.setIcon(request.getIcon().trim());
        category.setDisplayOrder(request.getDisplayOrder() != null
                ? request.getDisplayOrder()
                : nextCategoryDisplayOrder());

        WikiCategory saved = wikiCategoryRepository.save(category);

        return WikiCategoryCreateResponseDto.builder()
                .categoryId(saved.getWikiCategoryId())
                .build();
    }

    @Transactional
    public WikiCategoryUpdateResponseDto updateCategory(WikiCategoryUpdateRequestDto request) {
        WikiCategory category = findCategory(request.getCategoryId());

        if (request.getCategoryName() != null) {
            String categoryName = request.getCategoryName().trim();

            if (wikiCategoryRepository.existsByCategoryNameAndWikiCategoryIdNot(
                    categoryName, category.getWikiCategoryId())) {
                throw new CustomException(ErrorCode.WIKI_CATEGORY_DUPLICATE);
            }
            category.setCategoryName(categoryName);
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon().trim());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        WikiCategory saved = wikiCategoryRepository.saveAndFlush(category);

        return WikiCategoryUpdateResponseDto.builder()
                .categoryId(saved.getWikiCategoryId())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        WikiCategory category = findCategory(categoryId);

        if (wikiItemRepository.existsByWikiCategoryId(categoryId)) {
            throw new CustomException(ErrorCode.WIKI_CATEGORY_HAS_CHILDREN);
        }

        wikiCategoryRepository.delete(category);
    }

    // ───── 항목 ─────

    @Transactional
    public WikiItemCreateResponseDto createItem(WikiItemCreateRequestDto request) {
        findCategory(request.getCategoryId());
        List<Long> mediaIds = validateMediaIds(request.getMediaId());

        WikiItem item = new WikiItem();
        item.setWikiCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle().trim());
        item.setContent(request.getContent());
        item.setIcon(request.getIcon().trim());
        item.setDisplayOrder(nextItemDisplayOrder(request.getCategoryId()));

        WikiItem saved = wikiItemRepository.save(item);
        saveMediaLinks(saved.getWikiItemId(), mediaIds);

        return WikiItemCreateResponseDto.builder()
                .itemId(saved.getWikiItemId())
                .build();
    }

    @Transactional
    public WikiItemUpdateResponseDto updateItem(WikiItemUpdateRequestDto request) {
        WikiItem item = findItem(request.getItemId());
        // 이미지 검증은 수정 전에 먼저 한다.
        List<Long> mediaIds = request.getMediaId() == null ? null : validateMediaIds(request.getMediaId());

        if (request.getTitle() != null) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            item.setContent(request.getContent());
        }
        if (request.getIcon() != null) {
            item.setIcon(request.getIcon().trim());
        }
        if (mediaIds != null) {
            // 이미지만 바꿔도 수정 일시가 갱신되도록 한다.
            item.setUpdatedAt(LocalDateTime.now());
        }

        WikiItem saved = wikiItemRepository.saveAndFlush(item);

        if (mediaIds != null) {
            wikiMediaRepository.deleteAllByWikiItemId(saved.getWikiItemId());
            saveMediaLinks(saved.getWikiItemId(), mediaIds);
        }

        return WikiItemUpdateResponseDto.builder()
                .itemId(saved.getWikiItemId())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteItem(Long itemId) {
        findItem(itemId);

        wikiMediaRepository.deleteAllByWikiItemId(itemId);
        wikiItemRepository.deleteById(itemId);
    }

    // ───── 내부 도우미 ─────

    private WikiCategory findCategory(Long categoryId) {
        return wikiCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.WIKI_CATEGORY_NOT_FOUND));
    }

    private WikiItem findItem(Long itemId) {
        return wikiItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.WIKI_ITEM_NOT_FOUND));
    }

    private int nextCategoryDisplayOrder() {
        return wikiCategoryRepository.findTopByOrderByDisplayOrderDesc()
                .map(category -> category.getDisplayOrder() + 1)
                .orElse(1);
    }

    private int nextItemDisplayOrder(Long categoryId) {
        return wikiItemRepository.findTopByWikiCategoryIdOrderByDisplayOrderDesc(categoryId)
                .map(item -> item.getDisplayOrder() + 1)
                .orElse(1);
    }

    // 중복 ID는 하나로 합치고, 존재하지 않는 미디어가 있으면 404로 응답한다.
    private List<Long> validateMediaIds(List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return List.of();
        }
        if (mediaIds.stream().anyMatch(Objects::isNull)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<Long> distinctIds = mediaIds.stream().distinct().toList();

        if (mediaRepository.findAllById(distinctIds).size() != distinctIds.size()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 미디어가 포함되어 있습니다.");
        }
        return distinctIds;
    }

    // 요청에 담긴 순서대로 1, 2, 3번 표시 순서를 매겨 연결한다.
    private void saveMediaLinks(Long itemId, List<Long> mediaIds) {
        List<WikiMedia> links = IntStream.range(0, mediaIds.size())
                .mapToObj(index -> {
                    WikiMedia link = new WikiMedia();
                    link.setWikiItemId(itemId);
                    link.setMediaId(mediaIds.get(index));
                    link.setDisplayOrder(index + 1);
                    return link;
                })
                .toList();

        wikiMediaRepository.saveAll(links);
    }
}
