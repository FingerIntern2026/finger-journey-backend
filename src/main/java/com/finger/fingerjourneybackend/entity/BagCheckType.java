// BagCheckType 엔티티
// ERD의 BAG_CHECK_TYPE 테이블과 매핑됨 (가방싸기 체크항목 종류: 서류/장비/계정/계약/좌석)

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
public class BagCheckType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkTypeId;

    // DOCUMENT / EQUIPMENT / ACCOUNT / CONTRACT / SEAT
    @Column(nullable = false, unique = true, length = 30)
    private String checkCode;

    @Column(nullable = false, length = 100)
    private String checkName;

    private Long mediaId;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
