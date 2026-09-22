// WikiCategory 엔티티
// ERD의 WIKI_CATEGORY 테이블과 매핑됨 (핑거위키 카테고리, 이모지 아이콘 포함)

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
public class WikiCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wikiCategoryId;

    @Column(nullable = false, length = 100)
    private String categoryName;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
