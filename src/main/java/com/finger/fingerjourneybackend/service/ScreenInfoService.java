package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.dto.admin.response.ScreenInfoResponseDto;
import com.finger.fingerjourneybackend.repository.ScreenInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreenInfoService {

    private final ScreenInfoRepository screenInfoRepository;

    public List<ScreenInfoResponseDto> getScreenList() {
        return screenInfoRepository.findAllByOrderByTaskTaskCodeAscDisplayOrderAsc()
                .stream()
                .map(ScreenInfoResponseDto::from)
                .toList();
    }
}
