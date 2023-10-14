import com.amazonaws.util.json.Jackson;
import group.rxcloud.ava.aigc.controller.AIGCController;
import group.rxcloud.ava.aigc.database.DataBaseService;
import group.rxcloud.ava.aigc.entity.TripRecordInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.AbstractSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.ImageSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.TextSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.noteinfo.VoiceSingleNoteInfo;
import group.rxcloud.ava.aigc.entity.request.UploadTextRequestData;
import group.rxcloud.ava.aigc.entity.response.ResponseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootTest(classes = group.rxcloud.ava.aigc.SpringMain.class)
public class AIGCControllerTest {
    public static String mp3FileLocation = "/Users/wangjie_fourth/IdeaProjects/temp/trip-ava-aigc/demo/AwsPollyVoiceService.mp3";
    public static String imageFileLocation = "/Users/wangjie_fourth/IdeaProjects/temp/trip-ava-aigc/demo/awsrekognitioncelebrity_jinitaimei.jpeg";
    private static final String userId = "ava";
    private static final String longitude = "111";
    private static final String latitude = "222";
    @Resource
    private AIGCController aigcController;
    @Resource
    private DataBaseService dataBaseService;

    @Test
    public void testGenCurrentTripSession() {
        dataBaseService.clear();
        this.testVoiceUpload(false);
        this.testImageUpload(false);
        this.testTextUpload(false);
        ResponseInfo<?> responseInfo = aigcController.genCurrentTripSession();
        System.out.println("responseInfo.getData() = " + responseInfo.getData());
    }

    @Test
    public void testSingleFlow() {
        testVoiceUpload(true);
        testImageUpload(true);
        testTextUpload(true);
    }

    @Test
    public void test() {
        UploadTextRequestData requestData = new UploadTextRequestData();
        requestData.setText("早上，我从呼伦贝尔古城酒店启程");
        requestData.setLatitude(latitude);
        requestData.setLongitude(longitude);
        aigcController.uploadText(requestData);


        aigcController.uploadVoice(null, longitude, latitude);
        aigcController.uploadVoice(null, longitude, latitude);

        requestData.setText("好多牛，羊，蒙古包");
        aigcController.uploadText(requestData);

        aigcController.uploadImage(null, longitude, latitude);
        aigcController.uploadImage(null, longitude, latitude);

        ResponseInfo<?> responseInfo = aigcController.genCurrentTripSession();
        System.out.println("responseInfo = " + Jackson.toJsonString(responseInfo));
    }

    public void testVoiceUpload(boolean clear) {
        if (clear) {
            dataBaseService.clear();
        }
        ResponseInfo<?> responseInfo = aigcController.uploadVoice(fileToMultipartFile(new File(mp3FileLocation)), longitude, latitude);
        Assertions.assertNotNull(responseInfo);
        Assertions.assertEquals(1, responseInfo.getResponseCode());

        List<TripRecordInfo> tripRecordInfoList = dataBaseService.getTripRecordInfoByUserId(userId);
        Assertions.assertEquals(1, tripRecordInfoList.size());
        if (clear) {
            Assertions.assertTrue(tripRecordInfoList.get(0).getSingleNoteInfoList().stream().allMatch(x -> {
                if (!(x instanceof VoiceSingleNoteInfo)) {
                    return false;
                }
                if (!abstractSingleNoteInfoVerify(x)) {
                    return false;
                }
                return true;
            }));
        }
    }

    public void testImageUpload(boolean clear) {
        if (clear) {
            dataBaseService.clear();
        }

        ResponseInfo<?> responseInfo = aigcController.uploadImage(fileToMultipartFile(new File(imageFileLocation)), longitude, latitude);
        Assertions.assertNotNull(responseInfo);
        Assertions.assertEquals(1, responseInfo.getResponseCode());

        List<TripRecordInfo> tripRecordInfoList = dataBaseService.getTripRecordInfoByUserId(userId);
        Assertions.assertEquals(1, tripRecordInfoList.size());
        if (clear) {
            Assertions.assertTrue(tripRecordInfoList.get(0).getSingleNoteInfoList().stream().allMatch(x -> {
                if (!(x instanceof ImageSingleNoteInfo)) {
                    return false;
                }
                if (!abstractSingleNoteInfoVerify(x)) {
                    return false;
                }
                return true;
            }));
        }
    }

    public void testTextUpload(boolean clear) {
        if (clear) {
            dataBaseService.clear();
        }

        UploadTextRequestData requestData = new UploadTextRequestData();
        requestData.setText("test function");
        requestData.setLatitude(latitude);
        requestData.setLongitude(longitude);

        ResponseInfo<?> responseInfo = aigcController.uploadText(requestData);
        Assertions.assertNotNull(responseInfo);
        Assertions.assertEquals(1, responseInfo.getResponseCode());

        List<TripRecordInfo> tripRecordInfoList = dataBaseService.getTripRecordInfoByUserId(userId);
        Assertions.assertEquals(1, tripRecordInfoList.size());
        if (clear) {
            Assertions.assertTrue(tripRecordInfoList.get(0).getSingleNoteInfoList().stream().allMatch(x -> {
                if (!(x instanceof TextSingleNoteInfo)) {
                    return false;
                }
                if (!abstractSingleNoteInfoVerify(x)) {
                    return false;
                }
                return true;
            }));
        }
    }

    private static MultipartFile fileToMultipartFile(File file) {
        Path path = file.toPath();
        String name = file.getName();
        String originalFileName = file.getName();
        String contentType = "multipart/form-data";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }
        return new MockMultipartFile(name,
                originalFileName, contentType, content);
    }

    private static boolean abstractSingleNoteInfoVerify(AbstractSingleNoteInfo abstractSingleNoteInfo) {
        if (!abstractSingleNoteInfo.getPosition().getLatitude().equals(latitude)) {
            return false;
        }
        if (!abstractSingleNoteInfo.getPosition().getLongitude().equals(longitude)) {
            return false;
        }
        if (abstractSingleNoteInfo.getCreateDateTime() == null) {
            return false;
        }
        if (abstractSingleNoteInfo.getNoteInfoType() == null) {
            return false;
        }
        return true;
    }
}
