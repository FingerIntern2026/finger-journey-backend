package com.finger.fingerjourneybackend.dto.admin.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// API-ADM-003 명세서의 길이 제한(사번 20자, 이름 100자) 검증
class EmployeeCreateRequestDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsEmployeeNumberAndNameAtTheSpecLimit() {
        EmployeeCreateRequestDto request = request("1".repeat(20), "가".repeat(100));

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void rejectsEmployeeNumberLongerThan20() {
        Set<ConstraintViolation<EmployeeCreateRequestDto>> violations =
                VALIDATOR.validate(request("1".repeat(21), "김신입"));

        assertThat(violations).singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("사번은 20자 이하여야 합니다.");
    }

    @Test
    void rejectsNameLongerThan100() {
        Set<ConstraintViolation<EmployeeCreateRequestDto>> violations =
                VALIDATOR.validate(request("12609010", "가".repeat(101)));

        assertThat(violations).singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("이름은 100자 이하여야 합니다.");
    }

    private EmployeeCreateRequestDto request(String employeeNo, String name) {
        EmployeeCreateRequestDto request = new EmployeeCreateRequestDto();
        ReflectionTestUtils.setField(request, "employeeNo", employeeNo);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "organizationId", 2L);
        ReflectionTestUtils.setField(request, "positionId", 1L);
        ReflectionTestUtils.setField(request, "hireDate", LocalDate.of(2026, 11, 1));
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(2000, 3, 15));
        return request;
    }
}
