package group.rxcloud.ava.aigc.ai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.util.IOUtils;
import group.rxcloud.ava.aigc.ai.AIGCException;
import group.rxcloud.ava.aigc.config.AwsConfig;
import group.rxcloud.ava.aigc.utils.LocalFileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * AWS Polly 语音合成服务
 */
@Service
public class AwsPollyVoiceService {

    /**
     * 声音角色，默认为Zhiyu(中文)
     */
    public static final String VOICE_ID = "Zhiyu";

    private AmazonPolly polly;
    private String voiceId;

    public static void main(String[] args) throws IOException {
        AwsPollyVoiceService awsPollyVoiceService = new AwsPollyVoiceService();
        awsPollyVoiceService.init();

        InputStream stream = awsPollyVoiceService.genVoiceMp3FromText("你好");

        LocalFileUtils.saveFile(stream, "./demo/AwsPollyVoiceService.mp3");
    }

    @PostConstruct
    public void init() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(AwsConfig.accessKey, AwsConfig.secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

        // 创建AmazonPollyClient
        polly = AmazonPollyClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(AwsConfig.regions)
                .build();

        // 声音角色，默认为Zhiyu(中文)
        voiceId = VOICE_ID;
    }

    /**
     * 通过Text生成Voice
     *
     * @return 内存流
     */
    public InputStream genVoiceMp3FromText(String text) throws AIGCException {
        // 设置要生成的语音文件的参数
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withOutputFormat(OutputFormat.Mp3)
                .withVoiceId(voiceId)
                .withTextType(TextType.Text);

        // 生成语音文件
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

        // 将生成的语音文件保存到内存流
        try (InputStream in = synthRes.getAudioStream()) {
            byte[] bytes = IOUtils.toByteArray(in);
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new AIGCException(e);
        }
    }
}
