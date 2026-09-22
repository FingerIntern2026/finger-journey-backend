// PoemService
// 완주 3행시 저장 / 조회 (API-CPL-001~002)
// ERD 변경(9/22 팀 설계): line1~3 고정 대신 이름 글자수만큼 가변으로 ThreeLinePoemLine에 저장

package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.poem.request.PoemSaveRequestDto;
import com.finger.fingerjourneybackend.dto.poem.response.PoemLineDto;
import com.finger.fingerjourneybackend.dto.poem.response.PoemMyResponseDto;
import com.finger.fingerjourneybackend.dto.poem.response.PoemSaveResponseDto;
import com.finger.fingerjourneybackend.entity.ThreeLinePoem;
import com.finger.fingerjourneybackend.entity.ThreeLinePoemLine;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemLineRepository;
import com.finger.fingerjourneybackend.repository.ThreeLinePoemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PoemService {

    private final ThreeLinePoemRepository threeLinePoemRepository;
    private final ThreeLinePoemLineRepository threeLinePoemLineRepository;

    @Transactional
    public PoemSaveResponseDto savePoem(PoemSaveRequestDto request) {
        if (request.getLines().size() != request.getName().length()) {
            throw new CustomException(ErrorCode.ACROSTIC_LENGTH_MISMATCH);
        }

        ThreeLinePoem poem = threeLinePoemRepository.findByEmployeeId(request.getEmployeeId()).orElse(null);
        if (poem == null) {
            poem = new ThreeLinePoem();
            poem.setEmployeeId(request.getEmployeeId());
            poem = threeLinePoemRepository.save(poem);
        } else {
            // 이미 3행시가 있으면 재작성으로 간주하고 기존 줄을 지운 뒤 새로 저장 (덮어쓰기)
            // Hibernate는 기본적으로 insert를 delete보다 먼저 flush하기 때문에,
            // flush 없이 두면 새 줄 insert가 (poemId, lineOrder) 유니크 제약과 먼저 충돌함
            threeLinePoemLineRepository.deleteByPoemId(poem.getPoemId());
            threeLinePoemLineRepository.flush();
        }

        Long poemId = poem.getPoemId();
        List<ThreeLinePoemLine> lines = new ArrayList<>();
        String name = request.getName();
        for (int i = 0; i < request.getLines().size(); i++) {
            ThreeLinePoemLine line = new ThreeLinePoemLine();
            line.setPoemId(poemId);
            line.setLetter(String.valueOf(name.charAt(i)));
            line.setText(request.getLines().get(i));
            line.setLineOrder(i);
            lines.add(line);
        }
        threeLinePoemLineRepository.saveAll(lines);

        return new PoemSaveResponseDto(poemId, LocalDateTime.now());
    }

    public PoemMyResponseDto getMyPoem(Long employeeId) {
        ThreeLinePoem poem = threeLinePoemRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACROSTIC_NOT_FOUND));

        List<PoemLineDto> lines = threeLinePoemLineRepository.findByPoemIdOrderByLineOrderAsc(poem.getPoemId()).stream()
                .map(PoemLineDto::from)
                .toList();

        return new PoemMyResponseDto(lines);
    }
}
