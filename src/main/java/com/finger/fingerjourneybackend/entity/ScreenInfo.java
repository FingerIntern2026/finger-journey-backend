package com.finger.fingerjourneybackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

/**
 * 화면별 route와 논리적인 뒤로가기 규칙을 관리하는 엔티티.
 */
@Entity
@Table(
        name = "screen_info",
        indexes = @Index(
                name = "idx_screen_info_task_order",
                columnList = "task_id, display_order"
        )
)
@Check(constraints = "(back_action = 'TARGET' AND back_screen_code IS NOT NULL) "
        + "OR (back_action IN ('EXIT', 'BLOCK') AND back_screen_code IS NULL)")
@Getter
@Setter
@NoArgsConstructor
public class ScreenInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screen_info_task")
    )
    private Task task;

    @Column(nullable = false, unique = true, length = 30)
    private String screenCode;

    @Column(nullable = false, length = 100)
    private String screenName;

    @Column(nullable = false, unique = true, length = 200)
    private String routePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BackAction backAction;

    @Column(length = 30)
    private String backScreenCode;

    @Column(nullable = false)
    private Integer displayOrder = 0;
}
