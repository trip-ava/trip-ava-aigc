package group.rxcloud.ava.aigc.controller;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import group.rxcloud.ava.aigc.entity.Position;
import group.rxcloud.ava.aigc.entity.request.UploadTextRequestData;
import group.rxcloud.ava.aigc.entity.response.ResponseInfo;
import group.rxcloud.ava.aigc.service.AvaService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController(value = "/aigc")
public class AIGCController {

    @Resource
    private AvaService avaService;

    private static final AtomicInteger UPLOAD_VOICE_COUNT = new AtomicInteger(0);
    private static final AtomicInteger UPLOAD_IMAGE_COUNT = new AtomicInteger(0);

    @PostMapping("/upload/voice")
    public ResponseInfo<?> uploadVoice(@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude) {
        try {
            if(file == null) {
                if (UPLOAD_VOICE_COUNT.getAndIncrement() % 2 == 0) {
                    file = fileToMultipartFile(new File("./hackson/voice1.mp3"));
                } else {
                    file = fileToMultipartFile(new File("./hackson/voice2.mp3"));
                }
            }
            avaService.uploadVoice(multipartFileToFile(file), new Position(longitude, latitude));
            return ResponseInfo.buildSuccess("upload success");
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    @PostMapping("/upload/image")
    public ResponseInfo<?> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam("longitude") String longitude,
                                       @RequestParam("latitude") String latitude) {
        try {
            if(file == null) {
                if (UPLOAD_IMAGE_COUNT.getAndIncrement() % 2 == 0) {
                    file = fileToMultipartFile(new File("./hackson/image1.jpg"));
                } else {
                    file = fileToMultipartFile(new File("./hackson/image2.jpg"));
                }
            }
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

    @GetMapping("/upload/genCurrentTripSession")
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

}
