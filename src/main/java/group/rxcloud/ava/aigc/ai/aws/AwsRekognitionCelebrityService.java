package group.rxcloud.ava.aigc.ai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import group.rxcloud.ava.aigc.config.AwsConfig;
import group.rxcloud.ava.aigc.utils.AwsFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * AWS Rekognition 名人识别服务
 */
@Service
public class AwsRekognitionCelebrityService {

    private AmazonRekognition rekognition;

    @Autowired
    private AwsFileUtils awsFileUtils;

    public static void main(String[] args) {
        AwsRekognitionCelebrityService rekognitionCelebrityService = new AwsRekognitionCelebrityService();
        rekognitionCelebrityService.init();
        rekognitionCelebrityService.awsFileUtils = new AwsFileUtils();

        List<Celebrity> celebrities = rekognitionCelebrityService.recognizeCelebritiesFromLocalFile("./demo/awsrekognitioncelebrity_jinitaimei.jpeg");

        System.out.println(celebrities);
    }

    @PostConstruct
    public void init() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(AwsConfig.accessKey, AwsConfig.secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

        // 创建AmazonRekognition
        rekognition = AmazonRekognitionClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(AwsConfig.regions)
                .build();
    }

    /**
     * 从本地文件中识别图像名人
     */
    public List<Celebrity> recognizeCelebritiesFromLocalFile(String filePath) {
        ByteBuffer byteBufferFromFilePath = awsFileUtils.getByteBufferFromFilePath(filePath);

        Image image = new Image().withBytes(byteBufferFromFilePath);

        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(image);

        return rekognition.recognizeCelebrities(request).getCelebrityFaces();
    }

    public List<Celebrity> recognizeCelebritiesFromS3(String bucket, String key) {
        Image image = new Image()
                .withS3Object(new com.amazonaws.services.rekognition.model.S3Object()
                        .withBucket(bucket)
                        .withName(key));

        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(image);

        return rekognition.recognizeCelebrities(request).getCelebrityFaces();
    }

    public List<Celebrity> recognizeCelebritiesFromByteBuffer(ByteBuffer byteBuffer) {
        Image image = new Image()
                .withBytes(byteBuffer);

        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(image);

        return rekognition.recognizeCelebrities(request).getCelebrityFaces();
    }

    public List<Celebrity> recognizeCelebritiesFromInputStream(InputStream inputStream) {
        ByteBuffer byteBufferFromInputStream = awsFileUtils.getByteBufferFromInputStream(inputStream);

        Image image = new Image()
                .withBytes(byteBufferFromInputStream);

        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(image);

        return rekognition.recognizeCelebrities(request).getCelebrityFaces();
    }

    public List<Celebrity> recognizeCelebritiesFromS3Object(com.amazonaws.services.rekognition.model.S3Object s3Object) {
        Image image = new Image()
                .withS3Object(s3Object);

        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(image);

        return rekognition.recognizeCelebrities(request).getCelebrityFaces();
    }
}
