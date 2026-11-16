// WikiService
// 역할: 입사자가 보는 핑거위키 조회 (API-WIK-001 목록, 002 검색, 003 상세)
// 관리자의 등록·수정·삭제는 AdminWikiService가 맡는다.

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.wiki.response.WikiCategoryListResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiItemDetailResponseDto;
import com.finger.fingerjourneybackend.dto.wiki.response.WikiItemSummaryResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WikiService {

    // 검색 결과의 snippet은 검색어 앞 10글자부터 최대 50글자까지 보여준다.
    private static final int SNIPPET_LENGTH = 50;
    private static final int SNIPPET_LEAD = 10;
    private static final String ELLIPSIS = "...";

    private final WikiCategoryRepository wikiCategoryRepository;
    private final WikiItemRepository wikiItemRepository;
    private final WikiMediaRepository wikiMediaRepository;
    private final MediaRepository mediaRepository;

    /**
     * 위키 목록 조회
     * - 카테고리를 노출 순서대로, 그 아래 항목을 노출 순서대로 반환한다.
     * - 항목이 하나도 없는 카테고리도 포함한다. (관리자가 항목을 추가하려면 보여야 한다)
     * - 항목은 전체를 한 번에 조회해서 메모리에서 카테고리별로 묶는다.
     *   카테고리마다 따로 조회하면 카테고리 수만큼 쿼리가 늘어난다.
     */
    public List<WikiCategoryListResponseDto> getWikiList() {
        List<WikiCategory> categories = wikiCategoryRepository.findAllByOrderByDisplayOrderAsc();

        // groupingBy는 조회된 순서(displayOrder 순)를 그룹 안에서도 유지한다.
        Map<Long, List<WikiItem>> itemsByCategoryId = wikiItemRepository.findAllByOrderByDisplayOrderAsc().stream()
                .collect(Collectors.groupingBy(WikiItem::getWikiCategoryId));

        return categories.stream()
                .map(category -> WikiCategoryListResponseDto.builder()
                        .categoryId(category.getWikiCategoryId())
                        .categoryName(category.getCategoryName())
                        .icon(category.getIcon())
                        .items(itemsByCategoryId.getOrDefault(category.getWikiCategoryId(), List.of()).stream()
                                .map(item -> WikiItemSummaryResponseDto.builder()
                                        .itemId(item.getWikiItemId())
                                        .title(item.getTitle())
                                        .icon(item.getIcon())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    /**
     * 위키 검색
     * - 제목이나 본문에 검색어가 들어 있으면 찾는다. (대소문자 구분 없음)
     * - 결과는 카테고리 노출 순서 → 항목 노출 순서로 정렬한다.
     * - 검색어가 비어 있으면 400이다.
     */
    public List<WikiSearchResponseDto> search(String keyword) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();

        if (trimmedKeyword.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Map<Long, WikiCategory> categoryById = wikiCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(WikiCategory::getWikiCategoryId, Function.identity()));

        return wikiItemRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(trimmedKeyword, trimmedKeyword)
                .stream()
                .sorted(Comparator
                        .comparing((WikiItem item) -> categoryOrder(categoryById.get(item.getWikiCategoryId())))
                        .thenComparing(WikiItem::getDisplayOrder))
                .map(item -> {
                    WikiCategory category = categoryById.get(item.getWikiCategoryId());

                    return WikiSearchResponseDto.builder()
                            .itemId(item.getWikiItemId())
                            .title(item.getTitle())
                            .categoryName(category == null ? null : category.getCategoryName())
                            .snippet(makeSnippet(item.getContent(), trimmedKeyword))
                            .build();
                })
                .toList();
    }

    /**
     * 위키 항목 상세 조회
     * - 없는 항목이면 WIK_001(404)이다.
     * - 이미지는 연결 테이블(WikiMedia)의 표시 순서대로, 실제 파일 주소(fileUrl)로 바꿔서 반환한다.
     */
    public WikiItemDetailResponseDto getItem(Long itemId) {
        WikiItem item = wikiItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.WIKI_ITEM_NOT_FOUND));

        List<Long> mediaIds = wikiMediaRepository.findByWikiItemIdOrderByDisplayOrderAsc(itemId).stream()
                .map(WikiMedia::getMediaId)
                .toList();

        // 미디어를 한 번에 조회한 뒤, 연결 순서대로 주소를 뽑는다. (findAllById는 순서를 보장하지 않는다)
        Map<Long, String> fileUrlByMediaId = mediaRepository.findAllById(mediaIds).stream()
                .collect(Collectors.toMap(Media::getMediaId, Media::getFileUrl));

        // 파일이 없는 이미지는 건너뛰고, 주소(mediaUrl)와 ID(mediaId)가 같은 순서로 짝이 맞게 만든다.
        List<Long> existingMediaIds = mediaIds.stream()
                .filter(fileUrlByMediaId::containsKey)
                .toList();

        List<String> mediaUrls = existingMediaIds.stream()
                .map(fileUrlByMediaId::get)
                .toList();

        return WikiItemDetailResponseDto.builder()
                .title(item.getTitle())
                .content(item.getContent())
                .mediaUrl(mediaUrls)
                .mediaId(existingMediaIds)
                .build();
    }

    // 카테고리를 찾지 못한 항목은 맨 뒤로 보낸다.
    private int categoryOrder(WikiCategory category) {
        return category == null ? Integer.MAX_VALUE : category.getDisplayOrder();
    }

    /**
     * 본문 중 검색어 주변만 잘라서 보여준다.
     * - 줄바꿈과 연속 공백은 한 칸 공백으로 바꾼다.
     * - 검색어가 본문에 없고 제목에만 있으면 본문 처음부터 보여준다.
     * - 앞이나 뒤가 잘렸으면 "..."을 붙인다.
     */
    private String makeSnippet(String content, String keyword) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String flat = content.replaceAll("\\s+", " ").trim();
        int matchIndex = flat.toLowerCase(Locale.ROOT).indexOf(keyword.toLowerCase(Locale.ROOT));

        int start = matchIndex > SNIPPET_LEAD ? matchIndex - SNIPPET_LEAD : 0;
        int end = Math.min(flat.length(), start + SNIPPET_LENGTH);

        return (start > 0 ? ELLIPSIS : "") + flat.substring(start, end) + (end < flat.length() ? ELLIPSIS : "");
    }
}
