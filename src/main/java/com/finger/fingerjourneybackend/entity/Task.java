package com.finger.fingerjourneybackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 여러 화면을 하나의 업무 흐름으로 묶고 시작·종료 화면을 정의하는 엔티티.
 */
@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false, unique = true, length = 20)
    private String taskCode;

    @Column(nullable = false, length = 100)
    private String taskName;

    @Column(nullable = false, length = 30)
    private String entryScreenCode;

    @Column(length = 30)
    private String exitScreenCode;
}
