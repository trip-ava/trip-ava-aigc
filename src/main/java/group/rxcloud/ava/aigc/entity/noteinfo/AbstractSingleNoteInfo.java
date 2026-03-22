package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.Position;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Abstract base class for single note info
 */
@Getter
@Setter
public abstract class AbstractSingleNoteInfo {

    private LocalDateTime createDateTime;
    private Position position;

    protected AbstractSingleNoteInfo(Position position) {
        this.position = position;
        this.createDateTime = LocalDateTime.now();
    }
}
