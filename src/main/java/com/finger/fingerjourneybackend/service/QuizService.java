// QuizService
// 징검다리 퀴즈 문항 조회 / 답변 저장 / 내 답변 조회 (API-QUZ-001~003)

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.quiz.request.QuizAnswerSaveRequestDto;
import com.finger.fingerjourneybackend.dto.quiz.response.QuizMyAnswerResponseDto;
import com.finger.fingerjourneybackend.dto.quiz.response.QuizQuestionResponseDto;
import com.finger.fingerjourneybackend.entity.Quiz;
import com.finger.fingerjourneybackend.entity.QuizResponse;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.QuizRepository;
import com.finger.fingerjourneybackend.repository.QuizResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResponseRepository quizResponseRepository;

    public List<QuizQuestionResponseDto> getQuestions() {
        return quizRepository.findAll().stream()
                .sorted(Comparator.comparing(Quiz::getDisplayOrder))
                .map(QuizQuestionResponseDto::from)
                .toList();
    }

    // 이미 응답한 문항이 하나라도 섞여 있으면 전체를 막음 (중복 응답 방지, QUZ_002)
    public int saveAnswers(QuizAnswerSaveRequestDto request) {
        Set<Long> alreadyAnswered = quizResponseRepository.findByEmployeeId(request.getEmployeeId()).stream()
                .map(QuizResponse::getQuizId)
                .collect(Collectors.toSet());

        for (QuizAnswerSaveRequestDto.AnswerItem item : request.getAnswers()) {
            if (alreadyAnswered.contains(item.getQuizId())) {
                throw new CustomException(ErrorCode.QUIZ_ALREADY_ANSWERED);
            }
            if (!quizRepository.existsById(item.getQuizId())) {
                throw new CustomException(ErrorCode.QUIZ_SET_NOT_FOUND);
            }
        }

        List<QuizResponse> responses = request.getAnswers().stream()
                .map(item -> {
                    QuizResponse response = new QuizResponse();
                    response.setEmployeeId(request.getEmployeeId());
                    response.setQuizId(item.getQuizId());
                    response.setSelectedOption(item.getSelectedOption());
                    response.setAnsweredAt(LocalDateTime.now());
                    return response;
                })
                .toList();

        quizResponseRepository.saveAll(responses);
        return responses.size();
    }

    public List<QuizMyAnswerResponseDto> getMyAnswers(Long employeeId) {
        List<QuizResponse> responses = quizResponseRepository.findByEmployeeId(employeeId);
        Map<Long, Quiz> quizById = quizRepository.findAllById(
                responses.stream().map(QuizResponse::getQuizId).toList()
        ).stream().collect(Collectors.toMap(Quiz::getQuizId, q -> q));

        return responses.stream()
                .map(r -> new QuizMyAnswerResponseDto(
                        r.getQuizId(),
                        quizById.containsKey(r.getQuizId()) ? quizById.get(r.getQuizId()).getQuestion() : "",
                        r.getSelectedOption()
                ))
                .toList();
    }
}
