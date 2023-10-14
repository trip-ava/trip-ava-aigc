package group.rxcloud.ava.aigc.service;

import com.amazonaws.services.rekognition.model.Label;
import group.rxcloud.ava.aigc.ai.aws.AwsRekognitionImageService;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.database.DataBaseService;
import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AvaService {

    @Resource
    private DataBaseService dataBaseService;

    @Resource
    private AwsTranscribeVoiceService awsTranscribeVoiceService;
    @Resource
    private AwsRekognitionImageService awsRekognitionImageService;

    /**
     * userId-tripSessionId
     * 假设每个用户只能开启一个aigc会话
     */
    private static final Map<String, String> userTripMap = new ConcurrentHashMap<>();

    /**
     * 假设只有userId
     */
    private static final String userId = "ava";

    public void uploadVoice(File voiceFile, Position position) {
        String tripSessionId = computeTripSessionId(userId);
        // 语音转文字
        AwsTranscribeVoiceService.Transcript transcript = awsTranscribeVoiceService.transferMp3ToText(voiceFile.getName(), voiceFile);
        if (transcript == null) {
            throw new RuntimeException("get transfer text fail");
        }
        dataBaseService.addNewVoiceRecord(userId, tripSessionId, voiceFile, transcript, position);
    }

    public void uploadImage(File imageFile, Position position) {
        String tripSessionId = computeTripSessionId(userId);
        List<Label> labels = awsRekognitionImageService.detectLabelsFromLocalFile(imageFile.getPath());
        dataBaseService.addNewImageRecord(userId, tripSessionId, imageFile, labels, position);
    }

    public void uploadText(String text, Position position) {
        String tripSessionId = computeTripSessionId(userId);
        dataBaseService.addNewTextRecord(userId, tripSessionId, text, position);
    }

    public TripRecordInfo getTripShareNoteInfo(String tripSessionId) {
        TripRecordInfo tripRecordInfo = dataBaseService.getTripRecordInfoByTripSessionId(tripSessionId);
        if (tripRecordInfo == null) {
            throw new RuntimeException("not find tripSessionId:%s ".formatted(tripSessionId));
        }
        userTripMap.remove(userId);
        return tripRecordInfo;
    }


    private String computeTripSessionId(String userId) {
        synchronized (userTripMap) {
            if (!userTripMap.containsKey(userId)) {
                userTripMap.put(userId, UUID.randomUUID().toString());
            }
        }
        return userTripMap.get(userId);
    }
}
