// QuizResponse 엔티티
// ERD의 QUIZ_RESPONSE 테이블과 매핑됨 (사원별 퀴즈 문항 선택 답변)
// AI 완주 리포트 생성 시 이 테이블의 9개 응답을 조회해서 AI 서버에 전달함

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "quizId"}))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class QuizResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false, length = 255)
    private String selectedOption;

    private LocalDateTime answeredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
