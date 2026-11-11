package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.config.JwtTokenProvider;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
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

    @InjectMocks
    private AuthService authService;

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