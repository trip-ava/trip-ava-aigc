package group.rxcloud.ava.aigc.database;

import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.ImageSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.TextSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.VoiceSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataBaseService {

    private static final List<TripRecordInfo> TRIP_RECORD_INFO_TABLE = new ArrayList<>();

    public void addNewFileRecord(String userId, String tripSessionId, File file, SingleNoteInfoType noteInfoType) {
        TripRecordInfo tripRecordInfo = find(userId, tripSessionId);
        switch (noteInfoType) {
            case VOICE -> tripRecordInfo.getSingleNoteInfoList().add(new VoiceSingleNoteInfo(file));
            case IMAGE -> tripRecordInfo.getSingleNoteInfoList().add(new ImageSingleNoteInfo(file));
            default -> throw new RuntimeException("noteInfoType:%s not impl".formatted(noteInfoType.getCode()));
        }
    }

    public void addNewTextRecord(String userId, String tripSessionId, String text) {
        TripRecordInfo tripRecordInfo = find(userId, tripSessionId);
        tripRecordInfo.getSingleNoteInfoList().add(new TextSingleNoteInfo(text));
    }

    private TripRecordInfo find(String userId, String tripSessionId) {
        TripRecordInfo tripRecordInfo = TRIP_RECORD_INFO_TABLE.stream().filter(x -> userId.equalsIgnoreCase(x.getUserId())
                && tripSessionId.equalsIgnoreCase(x.getTripSessionId())).findFirst().orElse(null);
        if (tripRecordInfo == null) {
            tripRecordInfo = new TripRecordInfo();
            tripRecordInfo.setUserId(userId);
            tripRecordInfo.setTripSessionId(tripSessionId);
            tripRecordInfo.setSingleNoteInfoList(new ArrayList<>());
            TRIP_RECORD_INFO_TABLE.add(tripRecordInfo);
        }
        return tripRecordInfo;
    }

    public TripRecordInfo getTripRecordInfoByTripSessionId(String tripSessionId) {
        return TRIP_RECORD_INFO_TABLE.stream().filter(x -> tripSessionId.equalsIgnoreCase(x.getTripSessionId()))
                .findFirst().orElse(null);
    }

    public List<TripRecordInfo> getTripRecordInfoByUserId(String userId) {
        return TRIP_RECORD_INFO_TABLE.stream().filter(x -> userId.equalsIgnoreCase(x.getUserId())).collect(Collectors.toList());
    }

    public void clear() {
        TRIP_RECORD_INFO_TABLE.removeIf(tripRecordInfo -> true);
    }
}
