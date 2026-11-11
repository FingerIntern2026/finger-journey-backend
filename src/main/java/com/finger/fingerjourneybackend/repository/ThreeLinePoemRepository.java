// ThreeLinePoemRepository
// AI 완주 리포트 생성 시 사원의 3행시 존재 여부/조회에 사용

package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.ThreeLinePoem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThreeLinePoemRepository extends JpaRepository<ThreeLinePoem, Long> {

    Optional<ThreeLinePoem> findByEmployeeId(Long employeeId);

    // 사원 삭제 시 3행시를 함께 지운다. 줄(ThreeLinePoemLine)은 먼저 지워야 한다. (API-ADM-005)
    void deleteByEmployeeId(Long employeeId);
}
