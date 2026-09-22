// QuizController
// 징검다리 퀴즈 문항 조회 / 답변 저장 / 내 답변 조회 (API-QUZ-001~003)

package com.finger.fingerjourneybackend.controller;

import com.finger.fingerjourneybackend.dto.ApiResponseDto;
import com.finger.fingerjourneybackend.dto.quiz.request.QuizAnswerSaveRequestDto;
import com.finger.fingerjourneybackend.dto.quiz.request.QuizMyAnswersRequestDto;
import com.finger.fingerjourneybackend.dto.quiz.response.QuizAnswerSaveResponseDto;
import com.finger.fingerjourneybackend.dto.quiz.response.QuizMyAnswerResponseDto;
import com.finger.fingerjourneybackend.dto.quiz.response.QuizQuestionResponseDto;
import com.finger.fingerjourneybackend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 문항 조회: POST /quiz/questions
    @PostMapping("/questions")
    public ApiResponseDto<List<QuizQuestionResponseDto>> getQuestions() {
        return new ApiResponseDto<>(quizService.getQuestions());
    }

    // 답변 저장: POST /quiz/answers
    // 요청 body: { "employeeId": 1, "answers": [{ "quizId": 1, "selectedOption": "메일 확인" }, ...] }
    @PostMapping("/answers")
    public ApiResponseDto<QuizAnswerSaveResponseDto> saveAnswers(@Valid @RequestBody QuizAnswerSaveRequestDto request) {
        int savedCount = quizService.saveAnswers(request);
        return new ApiResponseDto<>(new QuizAnswerSaveResponseDto(savedCount));
    }

    // 내 답변 조회: POST /quiz/answers/my
    // 요청 body: { "employeeId": 1 }
    @PostMapping("/answers/my")
    public ApiResponseDto<List<QuizMyAnswerResponseDto>> getMyAnswers(@Valid @RequestBody QuizMyAnswersRequestDto request) {
        return new ApiResponseDto<>(quizService.getMyAnswers(request.getEmployeeId()));
    }
}
