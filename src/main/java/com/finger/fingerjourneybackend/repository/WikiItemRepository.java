package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WikiItemRepository extends JpaRepository<WikiItem, Long> {

    // 카테고리 삭제 전에 하위 항목이 있는지 확인한다.
    boolean existsByWikiCategoryId(Long wikiCategoryId);

    // 카테고리 안에서 새 항목을 맨 뒤에 붙일 때 사용한다.
    Optional<WikiItem> findTopByWikiCategoryIdOrderByDisplayOrderDesc(Long wikiCategoryId);
}
