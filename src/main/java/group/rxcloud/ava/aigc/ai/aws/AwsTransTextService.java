package group.rxcloud.ava.aigc.ai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import group.rxcloud.ava.aigc.config.AwsConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * AWS Translate 文本翻译服务
 */
@Service
public class AwsTransTextService {

    public static final String EN = "en";
    public static final String ZH = "zh";

    private AmazonTranslate translate;

    public static void main(String[] args) throws IOException {
        AwsTransTextService awsTransTextService = new AwsTransTextService();
        awsTransTextService.init();

        String translateText = awsTransTextService.translateText("你好", ZH, EN);
        System.out.println(translateText);
    }

    @PostConstruct
    public void init() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(AwsConfig.accessKey, AwsConfig.secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

        // 创建AmazonTranslate
        translate = AmazonTranslateClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(AwsConfig.regions)
                .build();
    }

    /**
     * 翻译文本
     */
    public String translateText(String text, String sourceLanguageCode, String targetLanguageCode) {
        TranslateTextRequest translateTextRequest = new TranslateTextRequest()
                .withText(text)
                .withSourceLanguageCode(sourceLanguageCode)
                .withTargetLanguageCode(targetLanguageCode);
        return translate.translateText(translateTextRequest).getTranslatedText();
    }
}
