// WikiItem 엔티티
// ERD의 WIKI_ITEM 테이블과 매핑됨 (핑거위키 카테고리 하위 항목)

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
public class WikiItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wikiItemId;

    @Column(nullable = false)
    private Long wikiCategoryId;

    @Column(nullable = false, length = 255)
    private String title;

    // @Lob만 쓰면 Postgres에서 text가 아니라 oid(대용량 객체)로 생성돼서 다루기 번거로움 → columnDefinition으로 명시
    @Column(columnDefinition = "text")
    private String content;

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
