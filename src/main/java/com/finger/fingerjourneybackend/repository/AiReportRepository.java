// AiReportRepository
// 사원별 리포트 조회 및 "이미 생성된 리포트가 있는지"(재생성 차단) 확인에 사용

package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {

    Optional<AiReport> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);
}
