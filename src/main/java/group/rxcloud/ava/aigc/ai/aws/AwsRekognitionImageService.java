package group.rxcloud.ava.aigc.ai.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import group.rxcloud.ava.aigc.config.AwsConfig;
import group.rxcloud.ava.aigc.utils.AwsFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * AWS Rekognition 图像识别服务
 */
@Service
public class AwsRekognitionImageService {

    public static final int MAX_LABELS = 10;
    public static final float MIN_CONFIDENCE = 75F;

    private AmazonRekognition rekognition;

    @Autowired
    private AwsFileUtils awsFileUtils;

    public static void main(String[] args) throws IOException {
        AwsRekognitionImageService awsRekognitionImageService = new AwsRekognitionImageService();
        awsRekognitionImageService.init();
        awsRekognitionImageService.awsFileUtils = new AwsFileUtils();

        List<Label> labels = awsRekognitionImageService.detectLabelsFromLocalFile("./demo/awsrekognitionimage_output.jpg");

        System.out.println(labels);
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
     * 从S3文件中识别图像关键词
     */
    public List<Label> detectLabelsFromS3(String bucket, String key) {
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withName(key)
                                .withBucket(bucket)))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognition.detectLabels(request);
        return result.getLabels();
    }

    /**
     * 从byte[]中识别图像关键词
     */
    public List<Label> detectLabelsFromBytes(byte[] bytes) {
        ByteBuffer byteBufferFromBytes = awsFileUtils.getByteBufferFromBytes(bytes);

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(byteBufferFromBytes))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognition.detectLabels(request);
        return result.getLabels();
    }

    /**
     * 从InputStream中识别图像关键词
     */
    public List<Label> detectLabelsFromStream(InputStream inputStream) {
        ByteBuffer byteBufferFromStream = awsFileUtils.getByteBufferFromInputStream(inputStream);

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(byteBufferFromStream))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognition.detectLabels(request);
        return result.getLabels();
    }

    /**
     * 从本地文件中识别图像关键词
     */
    public List<Label> detectLabelsFromLocalFile(String filePath) {
        ByteBuffer byteBufferFromLocalFile = awsFileUtils.getByteBufferFromFilePath(filePath);

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(byteBufferFromLocalFile))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognition.detectLabels(request);
        return result.getLabels();
    }
}
