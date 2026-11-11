package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WikiCategoryRepository extends JpaRepository<WikiCategory, Long> {

    boolean existsByCategoryName(String categoryName);

    // 수정 시 자기 자신을 제외하고 이름 중복을 검사한다.
    boolean existsByCategoryNameAndWikiCategoryIdNot(String categoryName, Long wikiCategoryId);

    // 노출 순서를 따로 주지 않은 카테고리를 맨 뒤에 붙일 때 사용한다.
    Optional<WikiCategory> findTopByOrderByDisplayOrderDesc();
}
