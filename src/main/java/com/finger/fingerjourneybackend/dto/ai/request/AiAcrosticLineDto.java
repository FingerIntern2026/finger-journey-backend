// AiAcrosticLineDto
// 3행시 한 줄 (이름 글자 수만큼 2~4개가 배열로 전달됨)

package com.finger.fingerjourneybackend.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiAcrosticLineDto {
    private String letter;
    private String text;
}
