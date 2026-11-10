package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.quiz.request.QuizAnswerSaveRequestDto;
import com.finger.fingerjourneybackend.entity.QuizResponse;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.QuizRepository;
import com.finger.fingerjourneybackend.repository.QuizResponseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock QuizResponseRepository quizResponseRepository;
    @InjectMocks QuizService quizService;

    @Test
    void saveAnswersFailsWhenQuizDoesNotExist() {
        QuizAnswerSaveRequestDto request = request(1L, 999L);
        when(quizResponseRepository.findByEmployeeId(1L)).thenReturn(List.of());
        when(quizRepository.existsById(999L)).thenReturn(false);

        assertError(() -> quizService.saveAnswers(request), ErrorCode.QUIZ_SET_NOT_FOUND);
        verify(quizResponseRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAnswersFailsWhenQuizWasAlreadyAnswered() {
        QuizAnswerSaveRequestDto request = request(1L, 10L);
        QuizResponse existing = new QuizResponse();
        existing.setQuizId(10L);
        when(quizResponseRepository.findByEmployeeId(1L)).thenReturn(List.of(existing));

        assertError(() -> quizService.saveAnswers(request), ErrorCode.QUIZ_ALREADY_ANSWERED);
        verify(quizResponseRepository, never()).saveAll(anyList());
    }

    private QuizAnswerSaveRequestDto request(Long employeeId, Long quizId) {
        QuizAnswerSaveRequestDto.AnswerItem item = new QuizAnswerSaveRequestDto.AnswerItem();
        ReflectionTestUtils.setField(item, "quizId", quizId);
        ReflectionTestUtils.setField(item, "selectedOption", "A");
        QuizAnswerSaveRequestDto request = new QuizAnswerSaveRequestDto();
        ReflectionTestUtils.setField(request, "employeeId", employeeId);
        ReflectionTestUtils.setField(request, "answers", List.of(item));
        return request;
    }

    private void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, ErrorCode errorCode) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
