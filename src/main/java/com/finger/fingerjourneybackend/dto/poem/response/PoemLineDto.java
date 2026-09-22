// PoemLineDto
// 3행시 한 줄 (letter + text)

package com.finger.fingerjourneybackend.dto.poem.response;

import com.finger.fingerjourneybackend.entity.ThreeLinePoemLine;
import lombok.Getter;

@Getter
public class PoemLineDto {

    private final String letter;
    private final String text;

    public PoemLineDto(String letter, String text) {
        this.letter = letter;
        this.text = text;
    }

    public static PoemLineDto from(ThreeLinePoemLine line) {
        return new PoemLineDto(line.getLetter(), line.getText());
    }
}
