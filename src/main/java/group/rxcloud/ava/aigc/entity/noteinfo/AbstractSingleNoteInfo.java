package group.rxcloud.ava.aigc.entity.noteinfo;

import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public sealed abstract class AbstractSingleNoteInfo implements SingleNoteInfo permits ImageSingleNoteInfo, TextSingleNoteInfo, VoiceSingleNoteInfo {
    private LocalDateTime createDateTime;
    private SingleNoteInfoType noteInfoType;

    public AbstractSingleNoteInfo(LocalDateTime createDateTime, SingleNoteInfoType noteInfoType) {
        this.createDateTime = createDateTime;
        this.noteInfoType = noteInfoType;
    }
}
