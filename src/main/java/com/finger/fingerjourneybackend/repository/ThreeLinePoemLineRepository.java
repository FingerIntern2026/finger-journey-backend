// ThreeLinePoemLineRepository
// 3행시 한 편의 줄들을 순서대로 조회하는 데 사용 (AI 서버에 letter/text 배열로 전달)

package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.ThreeLinePoemLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreeLinePoemLineRepository extends JpaRepository<ThreeLinePoemLine, Long> {

    List<ThreeLinePoemLine> findByPoemIdOrderByLineOrderAsc(Long poemId);

    // 3행시 재저장(덮어쓰기) 시 기존 줄들을 지우고 새로 넣기 위함
    void deleteByPoemId(Long poemId);
}
