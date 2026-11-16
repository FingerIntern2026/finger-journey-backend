package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WikiItemRepository extends JpaRepository<WikiItem, Long> {

    // 카테고리 삭제 전에 하위 항목이 있는지 확인한다.
    boolean existsByWikiCategoryId(Long wikiCategoryId);

    // 카테고리 안에서 새 항목을 맨 뒤에 붙일 때 사용한다.
    Optional<WikiItem> findTopByWikiCategoryIdOrderByDisplayOrderDesc(Long wikiCategoryId);

    // 위키 목록용: 전체 항목을 한 번에 가져온다. (카테고리마다 따로 조회하면 쿼리가 카테고리 수만큼 늘어난다)
    List<WikiItem> findAllByOrderByDisplayOrderAsc();

    // 위키 검색용: 제목 또는 본문에 검색어가 들어 있으면 찾는다. 대소문자는 구분하지 않는다.
    List<WikiItem> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);
}
