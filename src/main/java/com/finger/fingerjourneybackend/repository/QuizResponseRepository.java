// QuizResponseRepository
// AI 완주 리포트 생성 시 사원의 퀴즈 응답 9개를 조회하는 데 사용

package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {

    List<QuizResponse> findByEmployeeId(Long employeeId);
}
