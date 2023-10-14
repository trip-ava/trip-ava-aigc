package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public final class TextSingleNoteInfo extends AbstractSingleNoteInfo {
    private String text;

    public TextSingleNoteInfo(String text, Position position) {
        super(SingleNoteInfoType.TEXT, position);
        this.text = text;
    }
}
