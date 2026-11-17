package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.config.JwtTokenProvider;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import com.finger.fingerjourneybackend.dto.auth.request.LoginRequestDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.repository.RefreshTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @ParameterizedTest
    @CsvSource({"1999-12-31,991231", "2000-02-29,000229", "2005-03-15,050315"})
    void loginAcceptsSixDigitBirthDate(String storedDate, String input) {
        Employee employee = employee(LocalDate.parse(storedDate));
        when(employeeRepository.findByEmployeeNo("12345678")).thenReturn(Optional.of(employee));
        when(organizationRepository.findById(2L)).thenReturn(Optional.empty());
        when(positionRepository.findById(3L)).thenReturn(Optional.empty());
        when(adminRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh");

        var response = authService.authenticate(request(input));

        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getRedirectScreen()).isEqualTo("PB_LGN_P02");
        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(refreshTokenRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @ParameterizedTest
    @CsvSource({"990230", "991232", "991230", "19991231", "abcdef", "12345"})
    void loginRejectsInvalidOrMismatchedBirthDate(String input) {
        when(employeeRepository.findByEmployeeNo("12345678"))
                .thenReturn(Optional.of(employee(LocalDate.of(1999, 12, 31))));
        assertThatThrownBy(() -> authService.authenticate(request(input)))
                .isInstanceOfSatisfying(CustomException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED));
        verifyNoInteractions(jwtTokenProvider, refreshTokenRepository);
    }

    private LoginRequestDto request(String birthDate) {
        LoginRequestDto request = new LoginRequestDto();
        ReflectionTestUtils.setField(request, "employeeNo", "12345678");
        ReflectionTestUtils.setField(request, "birthDate", birthDate);
        return request;
    }

    private Employee employee(LocalDate birthDate) {
        Employee employee = new Employee();
        employee.setEmployeeId(1L);
        employee.setOrganizationId(2L);
        employee.setPositionId(3L);
        employee.setBirthDate(birthDate);
        employee.setCurrentPhase("PREBOARDING");
        return employee;
    }

    //정상 토큰 테스트 -> Bearer  제거 후 사원 ID 반환
    @Test
    void getEmployeeIdReturnsEmployeeIdFromAccessToken() {
        when(jwtTokenProvider.validateAccessToken("test-access-token"))
                .thenReturn(1L);
        //AuthService가 Bearer 를 제거했기 때문에 JwtTokenProvider에는  test-access-token만 전달되어야 함
        Long employeeId =
                authService.getEmployeeId("Bearer test-access-token");

        assertThat(employeeId).isEqualTo(1L);

        verify(jwtTokenProvider)
                .validateAccessToken("test-access-token");
    }

    //헤더 없음 -> AUTH_002 에러 발생
    @Test
    void getEmployeeIdFailsWhenAuthorizationHeaderIsMissing() {
        assertThatThrownBy(() -> authService.getEmployeeId(null))
                .isInstanceOfSatisfying(
                        CustomException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_SESSION)
                );

        verifyNoInteractions(jwtTokenProvider);
    }

    //Bearer 없음 -> AUTH_002 에러 발생
    @Test
    void getEmployeeIdFailsWhenBearerPrefixIsMissing() {
        assertThatThrownBy(
                () -> authService.getEmployeeId("test-access-token")
        )
                .isInstanceOfSatisfying(
                        CustomException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_SESSION)
                );

        verifyNoInteractions(jwtTokenProvider);
    }
}