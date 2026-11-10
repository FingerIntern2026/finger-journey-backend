package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.poem.request.PoemSaveRequestDto;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemLineRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoemServiceTest {

    @Mock ThreeLinePoemRepository threeLinePoemRepository;
    @Mock ThreeLinePoemLineRepository threeLinePoemLineRepository;
    @InjectMocks PoemService poemService;

    @Test
    void savePoemFailsWhenLineCountDoesNotMatchNameLength() {
        PoemSaveRequestDto request = new PoemSaveRequestDto();
        ReflectionTestUtils.setField(request, "employeeId", 1L);
        ReflectionTestUtils.setField(request, "name", "김신입");
        ReflectionTestUtils.setField(request, "lines", List.of("김", "신"));

        assertError(() -> poemService.savePoem(request), ErrorCode.ACROSTIC_LENGTH_MISMATCH);
        verify(threeLinePoemRepository, never()).save(any());
    }

    @Test
    void getMyPoemFailsWhenPoemDoesNotExist() {
        when(threeLinePoemRepository.findByEmployeeId(999L)).thenReturn(Optional.empty());

        assertError(() -> poemService.getMyPoem(999L), ErrorCode.ACROSTIC_NOT_FOUND);
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
