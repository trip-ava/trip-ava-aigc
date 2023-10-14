package group.rxcloud.ava.aigc.controller;


import java.io.File;
import java.io.IOException;

import group.rxcloud.ava.aigc.entity.request.UploadTextRequestData;
import group.rxcloud.ava.aigc.entity.response.ResponseInfo;
import group.rxcloud.ava.aigc.service.AvaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController(value = "/aigc")
public class AIGCController {

    @Resource
    private AvaService avaService;

    @PostMapping("/upload/voice")
    public ResponseInfo<?> uploadVoice(@RequestParam("file") MultipartFile file) {
        try {
            avaService.uploadVoice(multipartFileToFile(file));
            return ResponseInfo.buildSuccess("upload success");
        } catch (Throwable e) {
            return ResponseInfo.buildError("service error");
        }
    }

    @PostMapping("/upload/image")
    public ResponseInfo<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            avaService.uploadImage(multipartFileToFile(file));
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
            avaService.uploadText(requestData.getText());
            return ResponseInfo.buildSuccess("upload success");
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
