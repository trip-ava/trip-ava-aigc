package group.rxcloud.ava.aigc.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import group.rxcloud.ava.aigc.config.AwsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.Date;

@Slf4j
@Component
public class AwsS3Utils {

    /**
     * URL过期时间, ms。默认3天
     */
    public static final int EXPIRE_TIME = 3600 * 1000 * 24 * 3;
    public static String bucketName = "aioverflow";

    private AmazonS3 s3client;

    @PostConstruct
    public void init() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(AwsConfig.accessKey, AwsConfig.secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(AwsConfig.region)
                .build();
    }

    public static void main(String[] args) {
        AwsS3Utils awsS3Utils = new AwsS3Utils();
        awsS3Utils.init();

        String s1 = awsS3Utils.picUpToS3ReUrl("awsrekognitionimage_output.jpg", "./demo/awsrekognitionimage_output.jpg");
        String s2 = awsS3Utils.mp3UpToS3ReUrl("AwsPollyVoiceService.mp3", "./demo/AwsPollyVoiceService.mp3");
        log.info("url: " + s1 + ", " + s2);
    }

    /**
     * 将本地mp3文件上传到s3，并返回公网访问的url
     */
    public String mp3UpToS3ReUrl(String mp3KeyName, String path) {
        String keyName = mp3KeyName;
        String filePath = path;
        try {
            File file = new File(filePath);
            // 设置文件的Content - Type为 "audio/mpeg"
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/mpeg");
            PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
            request.setMetadata(metadata);
            s3client.putObject(request);
            // 获取公网访问URL
            Date expiration = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            URL url = s3client.generatePresignedUrl(bucketName, keyName, expiration);
            log.info("File uploaded to S3, URL: " + url.toString());
            return url.toString();
        } catch (AmazonServiceException ase) {
            log.info("Error Message:    " + ase.getMessage());
            log.info("HTTP Status Code: " + ase.getStatusCode());
            log.info("AWS Error Code:   " + ase.getErrorCode());
            log.info("Error Type:       " + ase.getErrorType());
            log.info("Request ID:       " + ase.getRequestId());
        } catch (Exception ace) {
            log.info("Error Message: " + ace.getMessage());
            throw ace;
        }
        return "";
    }

    public String picUpToS3ReUrl(String mp3KeyName, String path) {
        String keyName = mp3KeyName;
        String filePath = path;
        try {
            File file = new File(filePath);
            PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
            PutObjectResult result = s3client.putObject(request);
            // 获取公网可以访问的URL
            Date expiration = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            URL url = s3client.generatePresignedUrl(bucketName, keyName, expiration);
            log.info("File uploaded successfully. URL: " + url.toString());
            return url.toString();
        } catch (Exception ace) {
            log.info("Error Message: " + ace.getMessage());
            throw ace;
        }
    }
}
