package group.rxcloud.ava.aigc.ai.aws;

import com.google.gson.Gson;
import group.rxcloud.ava.aigc.config.AwsConfig;
import group.rxcloud.ava.aigc.utils.AwsS3Utils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * AWS Transcribe 语音文本翻译服务
 */
@Slf4j
@Service
public class AwsTranscribeVoiceService {

    public static final String ZH = "zh-CN";

    private static Gson GSON = new Gson();

    private TranscribeClient transcribeClient;

    @Autowired
    private AwsS3Utils awsS3Utils;

    @PostConstruct
    public void init() {
        // 创建Amazon Transcribe V2
        transcribeClient = TranscribeClient.builder()
                .region(AwsConfig.regionsV2)
                .credentialsProvider(() -> AwsBasicCredentials.create(AwsConfig.accessKey, AwsConfig.secretKey))
                .build();
    }

    public static void main(String[] args) throws IOException {
        AwsTranscribeVoiceService awsTranscribeVoiceService = new AwsTranscribeVoiceService();
        awsTranscribeVoiceService.init();
        awsTranscribeVoiceService.awsS3Utils = new AwsS3Utils();
        awsTranscribeVoiceService.awsS3Utils.init();

        awsTranscribeVoiceService.awsS3Utils.mp3UpToS3ReUrl("AwsPollyVoiceService.mp3", "./demo/AwsPollyVoiceService.mp3");
        awsTranscribeVoiceService.transMp3ToText("AwsPollyVoiceService.mp3");
    }

    public Transcript transferMp3ToText(String fileName, File file) {
        awsS3Utils.mp3UpToS3ReUrl(fileName, file.getPath());
        Transcript transcript = this.transMp3ToText(fileName);
        if (transcript == null) {
            throw new RuntimeException("transfer mp3 to text fail");
        }
        return transcript;

    }

    /**
     * 翻译Mp3到文本
     */
    public Transcript transMp3ToText(String mp3S3KeyName) {
        // 替换为您的转录任务名称
        String jobName = "transmp3totext-" + UUID.randomUUID().toString().substring(0, 5);
        String mediaS3Url = "s3://" + AwsS3Utils.bucketName + "/" + mp3S3KeyName;

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(ZH)
                .mediaFormat(MediaFormat.MP3)
                .media(Media.builder()
                        .mediaFileUri(mediaS3Url)
                        .build())
                .build();

        StartTranscriptionJobResponse response = transcribeClient.startTranscriptionJob(request);
        String transcriptionJobName = response.transcriptionJob().transcriptionJobName();
        log.info("Transcription job name: " + transcriptionJobName);

        GetTranscriptionJobRequest getJobRequest = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(transcriptionJobName)
                .build();

        String transcriptResult = null;
        while (true) {
            GetTranscriptionJobResponse getJobResponse = transcribeClient.getTranscriptionJob(getJobRequest);
            TranscriptionJob transcriptionJob = getJobResponse.transcriptionJob();
            if (transcriptionJob.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED) {
                transcriptResult = transcriptionJob.transcript().transcriptFileUri();
                break;
            } else if (transcriptionJob.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
                log.info("Transcription job failed");
                break;
            }
            try {
                Thread.sleep(500); // 每500ms轮询一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (transcriptResult != null) {
            // 解析转录结果文件并提取转录文本
            String transcripts = convertURLToString(transcriptResult);
            // {"jobName":"transmp3totext-b947b","accountId":"445153820371","status":"COMPLETED","results":{"transcripts":[{"transcript":"你好"}],"items":[{"type":"pronunciation","alternatives":[{"confidence":"0.9999","content":"你好"}],"start_time":"0.0","end_time":"0.65"}]}}
            Transcript transcript = GSON.fromJson(transcripts, Transcript.class);
            log.info("Transcription result: " + transcript);
            return transcript;
        }

        return null;
    }

    public String convertURLToString(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
        } catch (IOException e) {
            log.error("读取URL内容时出现错误: ", e);
        }
        return content.toString();
    }

    @Data
    public static class Transcript {
        private Result results;
    }

    @Data
    public static class Result {
        private List<Item> transcripts;
    }

    @Data
    public static class Item {
        private String transcript;
    }
}
