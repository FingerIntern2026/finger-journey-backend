// QuizResponseRepository
// AI 완주 리포트 생성 시 사원의 퀴즈 응답 9개를 조회하는 데 사용

package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {

    List<QuizResponse> findByEmployeeId(Long employeeId);

    // 리포트의 "근거가 된 퀴즈 답변" 그리드용 — 이 문항에 답한 전체 인원 수
    long countByQuizId(Long quizId);

    // 이 문항에서 특정 답변을 고른 인원 수 (위 값과 나누면 실제 비율이 나옴)
    long countByQuizIdAndSelectedOption(Long quizId, String selectedOption);
}
