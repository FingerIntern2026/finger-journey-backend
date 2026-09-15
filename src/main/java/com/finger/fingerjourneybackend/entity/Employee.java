// Employee 엔티티
// 예시 서비스(입사자 등록/조회/수정/삭제)의 대상이 되는 직원 정보 테이블
// ERD의 EMPLOYEE 테이블과 1:1로 매핑됨 (organization_id, position_id는 FK지만
// 예시 서비스 스코프상 관계 매핑 없이 순수 컬럼(Long)으로만 둠)

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(nullable = false, unique = true, length = 20)
    private String employeeNo;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private Long positionId;

    private LocalDate hireDate;

    // 온보딩 진행 단계: PREBOARDING / BAG / ONBOARDING / COMPLETED
    // 생성 시 서버가 기본값 "PREBOARDING"으로 세팅 (요청 body로 안 받음)
    @Column(nullable = false, length = 20)
    private String currentPhase;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
