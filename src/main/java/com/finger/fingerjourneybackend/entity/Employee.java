package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    private String employeeNo;
    private String name;
    private Long organizationId;
    private Long positionId;
    private LocalDate hireDate;


}
