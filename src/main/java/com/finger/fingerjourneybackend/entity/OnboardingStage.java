// OnboardingStage 엔티티
// ERD의 ONBOARDING_STAGE 테이블과 매핑됨 (오솔길 스테이지 정의)

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OnboardingStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long onboardingStageId;

    @Column(nullable = false, length = 100)
    private String stageName;

    @Column(nullable = false)
    private Integer stageOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
