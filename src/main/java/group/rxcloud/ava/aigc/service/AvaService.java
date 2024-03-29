package group.rxcloud.ava.aigc.service;

import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.json.Jackson;
import group.rxcloud.ava.aigc.ai.aws.AwsRekognitionImageService;
import group.rxcloud.ava.aigc.ai.aws.AwsTransTextService;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.database.DataBaseService;
import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import group.rxcloud.ava.aigc.service.gpt.GptTravelNotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static group.rxcloud.ava.aigc.ai.aws.AwsTransTextService.EN;
import static group.rxcloud.ava.aigc.ai.aws.AwsTransTextService.ZH;

@Service
@Slf4j
public class AvaService {

    @Resource
    private DataBaseService dataBaseService;
    @Resource
    private AwsTransTextService awsTransTextService;
    @Resource
    private AwsTranscribeVoiceService awsTranscribeVoiceService;
    @Resource
    private AwsRekognitionImageService awsRekognitionImageService;
    @Resource
    private GptTravelNotesService gptTravelNotesService;
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
        // todo: 暂时只取第一个
//        if (!CollectionUtils.isEmpty(labels)) {
//            labels = labels.subList(0, 1);
//        }
        labels.parallelStream().forEach(x -> {
            String chineseName = awsTransTextService.translateText(x.getName(), EN, ZH);
            x.setName(chineseName);
        });
        dataBaseService.addNewImageRecord(userId, tripSessionId, imageFile, labels, position);
    }

    public void uploadText(String text, Position position) {
        String tripSessionId = computeTripSessionId(userId);
        dataBaseService.addNewTextRecord(userId, tripSessionId, text, position);
    }

    private String computeTripSessionId(String userId) {
        synchronized (userTripMap) {
            if (!userTripMap.containsKey(userId)) {
                userTripMap.put(userId, UUID.randomUUID().toString());
            }
        }
        return userTripMap.get(userId);
    }

    private static final List<String> AIGC_ARTICLE_STYLE = List.of("有趣的、幽默的");

    public List<String> genCurrentTripSession() {
        String tripSessionId = computeTripSessionId(userId);
        TripRecordInfo tripRecordInfo = dataBaseService.getTripRecordInfoByTripSessionId(tripSessionId);
        if (tripRecordInfo == null) {
            throw new RuntimeException("not find tripSessionId:%s ".formatted(tripSessionId));
        }
        List<List<String>> originTripNoteInfoList = tripRecordInfo.generate();
        List<String> result = new ArrayList<>();

        File file1 = new File("./hackson/result1.txt");
        File file2 = new File("./hackson/result2.txt");
//        File file3 = new File("./hackson/result3.txt");
        try {
            result.add(Files.readString(file1.toPath()));
            result.add(Files.readString(file2.toPath()));
        } catch (IOException ignore) {
        }


        AIGC_ARTICLE_STYLE.parallelStream().forEach(x -> {
            log.info("genCurrentTripSession: origin" + Jackson.toJsonString(originTripNoteInfoList));
            try {
                String aigcResult = gptTravelNotesService.generateGptFromKeywordsWithTimelineStyle(tripRecordInfo.generate(), x);
                log.info("genCurrentTripSession: after" + aigcResult);
                result.add(aigcResult);
            } catch (Exception e) {
                log.error("genCurrentTripSession", e);
            }
        });
        userTripMap.remove(userId);
        return result;
    }
}
