package group.rxcloud.ava.aigc.entity;

import group.rxcloud.ava.aigc.entity.noteinfo.SingleNoteInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripRecordInfo {
    private String userId;
    private String tripSessionId;
    private List<SingleNoteInfo> singleNoteInfoList;
}
