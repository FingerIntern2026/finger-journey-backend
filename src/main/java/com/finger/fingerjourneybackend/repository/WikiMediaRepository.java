package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.WikiMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WikiMediaRepository extends JpaRepository<WikiMedia, Long> {

    // 위키 상세용: 항목에 붙은 이미지 연결을 표시 순서(1, 2, 3번)대로 가져온다.
    List<WikiMedia> findByWikiItemIdOrderByDisplayOrderAsc(Long wikiItemId);

    // 이미지를 통째로 교체할 때 쓴다.
    // 파생 삭제 메서드는 삭제를 insert 뒤로 미뤄서 (wikiItemId, mediaId) 유니크 제약에 걸릴 수 있어
    // 즉시 실행되는 벌크 삭제를 사용한다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WikiMedia wm where wm.wikiItemId = :wikiItemId")
    void deleteAllByWikiItemId(@Param("wikiItemId") Long wikiItemId);
}
