package com.finger.fingerjourneybackend.config;

import com.finger.fingerjourneybackend.entity.Admin;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthInterceptorTest {

    @Mock
    private AuthService authService;
    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminAuthInterceptor interceptor;

    @Test
    void preflightRequestPassesWithoutToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/admin/employee/list");

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
        verify(authService, never()).getEmployeeId(any());
    }

    @Test
    void missingOrInvalidTokenIsRejected() {
        when(authService.getEmployeeId(null)).thenThrow(new CustomException(ErrorCode.INVALID_SESSION));

        assertError(() -> preHandle(null), ErrorCode.INVALID_SESSION);
        verify(adminRepository, never()).findByEmployeeId(any());
    }

    @Test
    void expiredTokenIsRejected() {
        when(authService.getEmployeeId("Bearer expired")).thenThrow(new CustomException(ErrorCode.TOKEN_EXPIRED));

        assertError(() -> preHandle("Bearer expired"), ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void employeeWithoutAdminRowIsDenied() {
        when(authService.getEmployeeId("Bearer employee")).thenReturn(1L);
        when(adminRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());

        assertError(() -> preHandle("Bearer employee"), ErrorCode.ADMIN_ACCESS_DENIED);
    }

    @Test
    void unknownRoleIsDenied() {
        when(authService.getEmployeeId("Bearer other")).thenReturn(2L);
        when(adminRepository.findByEmployeeId(2L)).thenReturn(Optional.of(admin("MANAGER")));

        assertError(() -> preHandle("Bearer other"), ErrorCode.ADMIN_ACCESS_DENIED);
    }

    @Test
    void hrAndAdminRolesAreAllowed() {
        when(authService.getEmployeeId("Bearer hr")).thenReturn(3L);
        when(authService.getEmployeeId("Bearer admin")).thenReturn(4L);
        when(adminRepository.findByEmployeeId(3L)).thenReturn(Optional.of(admin("HR")));
        when(adminRepository.findByEmployeeId(4L)).thenReturn(Optional.of(admin("ADMIN")));

        assertThat(preHandle("Bearer hr")).isTrue();
        assertThat(preHandle("Bearer admin")).isTrue();
    }

    @Test
    void allowedRequestCarriesTheAdminEmployeeId() {
        when(authService.getEmployeeId("Bearer admin")).thenReturn(4L);
        when(adminRepository.findByEmployeeId(4L)).thenReturn(Optional.of(admin("ADMIN")));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/bag/confirm");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer admin");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(request.getAttribute(AdminAuthInterceptor.ADMIN_EMPLOYEE_ID_ATTRIBUTE)).isEqualTo(4L);
    }

    private boolean preHandle(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/employee/list");
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        return interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
    }

    private Admin admin(String role) {
        Admin admin = new Admin();
        admin.setRole(role);
        return admin;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
