package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public final class TextSingleNoteInfo extends AbstractSingleNoteInfo {
    private String text;


    public TextSingleNoteInfo(String text) {
        super(LocalDateTime.now(), SingleNoteInfoType.TEXT);
        this.text = text;
    }
}
