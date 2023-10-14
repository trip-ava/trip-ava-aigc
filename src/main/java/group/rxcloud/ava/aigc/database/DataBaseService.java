package group.rxcloud.ava.aigc.database;

import com.amazonaws.services.rekognition.model.Label;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import group.rxcloud.ava.aigc.entity.TripSessionAigcGenerateInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.ImageSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.TextSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.VoiceSingleNoteInfo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataBaseService {

    private static final List<TripRecordInfo> TRIP_RECORD_INFO_TABLE = new ArrayList<>();
    private static final List<TripSessionAigcGenerateInfo> TRIP_SESSION_AIGC_GENERATE_INFO_TABLE = new ArrayList<>();

    public void addNewVoiceRecord(String userId, String tripSessionId, File file, AwsTranscribeVoiceService.Transcript transcript,
                                  Position position) {
        TripRecordInfo tripRecordInfo = find(userId, tripSessionId);
        VoiceSingleNoteInfo voiceSingleNoteInfo = new VoiceSingleNoteInfo(file, position);
        voiceSingleNoteInfo.setTranscript(transcript);
        tripRecordInfo.getSingleNoteInfoList().add(voiceSingleNoteInfo);
    }

    public void addNewImageRecord(String userId, String tripSessionId, File file, List<Label> labels, Position position) {
        TripRecordInfo tripRecordInfo = find(userId, tripSessionId);
        ImageSingleNoteInfo imageSingleNoteInfo = new ImageSingleNoteInfo(file, position);
        imageSingleNoteInfo.setLabels(labels);
        tripRecordInfo.getSingleNoteInfoList().add(imageSingleNoteInfo);
    }

    public void addNewTextRecord(String userId, String tripSessionId, String text, Position position) {
        TripRecordInfo tripRecordInfo = find(userId, tripSessionId);
        tripRecordInfo.getSingleNoteInfoList().add(new TextSingleNoteInfo(text, position));
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
