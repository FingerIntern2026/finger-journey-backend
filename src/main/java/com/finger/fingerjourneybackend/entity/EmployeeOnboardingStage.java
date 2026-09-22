// EmployeeOnboardingStage 엔티티
// ERD의 EMPLOYEE_ONBOARDING_STAGE 테이블과 매핑됨 (사원별 오솔길 스테이지 진행 상태)

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "onboardingStageId"}))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmployeeOnboardingStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeOnboardingStageId;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long onboardingStageId;

    // NOT_STARTED / IN_PROGRESS / COMPLETED
    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
