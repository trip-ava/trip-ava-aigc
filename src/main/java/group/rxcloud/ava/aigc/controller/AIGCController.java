package group.rxcloud.ava.aigc.controller;


import java.io.File;
import java.io.IOException;

import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.request.UploadTextRequestData;
import group.rxcloud.ava.aigc.entity.response.ResponseInfo;
import group.rxcloud.ava.aigc.service.AvaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController(value = "/aigc")
public class AIGCController {

    @Resource
    private AvaService avaService;

    @PostMapping("/upload/voice")
    public ResponseInfo<?> uploadVoice(@RequestParam("file") MultipartFile file, @RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude) {
        try {
            avaService.uploadVoice(multipartFileToFile(file), new Position(longitude, latitude));
            return ResponseInfo.buildSuccess("upload success");
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    @PostMapping("/upload/image")
    public ResponseInfo<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude) {
        try {
            avaService.uploadImage(multipartFileToFile(file), new Position(longitude, latitude));
            return ResponseInfo.buildSuccess("upload success");
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    @PostMapping("/upload/text")
    public ResponseInfo<?> uploadText(@RequestBody UploadTextRequestData requestData) {
        try {
            if (requestData == null || requestData.isIllegal()) {
                return ResponseInfo.buildError("illegal param");
            }
            avaService.uploadText(requestData.getText(), new Position(requestData.getLongitude(), requestData.getLatitude()));
            return ResponseInfo.buildSuccess("upload success");
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    @GetMapping("/genCurrentTripSession")
    public ResponseInfo<?> genCurrentTripSession() {
        try {
            return ResponseInfo.buildSuccess(avaService.genCurrentTripSession());
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    public static File multipartFileToFile(MultipartFile multiFile) throws IOException {
        // 获取文件名
        String fileName = multiFile.getOriginalFilename();
        // 获取文件后缀
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        // 若需要防止生成的临时文件重复,可以在文件名后添加随机码
        File file = File.createTempFile(fileName, prefix);
        multiFile.transferTo(file);
        return file;
    }

}
