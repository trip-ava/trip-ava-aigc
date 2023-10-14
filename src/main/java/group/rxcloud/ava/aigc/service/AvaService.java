package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.database.DataBaseService;
import group.rxcloud.ava.aigc.entity.SingleNoteInfoType;
import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AvaService {

    @Resource
    private DataBaseService dataBaseService;

    /**
     * userId-tripSessionId
     * 假设每个用户只能开启一个aigc会话
     */
    private static final Map<String, String> userTripMap = new ConcurrentHashMap<>();

    /**
     * 假设只有userId
     */
    private static final String userId = "ava";

    public void uploadVoice(File voiceFile) {
        String tripSessionId = computeTripSessionId(userId);
        // todo：语音转文字
        dataBaseService.addNewFileRecord(userId, tripSessionId, voiceFile, SingleNoteInfoType.VOICE);
    }

    public void uploadImage(File imageFile) {
        String tripSessionId = computeTripSessionId(userId);
        // todo: AI提取图片内容
        dataBaseService.addNewFileRecord(userId, tripSessionId, imageFile, SingleNoteInfoType.IMAGE);
    }

    public void uploadText(String text) {
        String tripSessionId = computeTripSessionId(userId);
        dataBaseService.addNewTextRecord(userId, tripSessionId, text);
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
