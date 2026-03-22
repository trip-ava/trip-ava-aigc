package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.Position;
import lombok.Getter;
import lombok.Setter;

/**
 * Text note info
 */
@Getter
@Setter
public class TextSingleNoteInfo extends AbstractSingleNoteInfo {

    private String text;

    public TextSingleNoteInfo(String text, Position position) {
        super(position);
        this.text = text;
    }
}
