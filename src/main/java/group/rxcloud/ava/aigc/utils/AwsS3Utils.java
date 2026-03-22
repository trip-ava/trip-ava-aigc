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
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Slf4j
@Component
public class AwsS3Utils {

    /**
     * URL过期时间, ms。默认3天
     */
    public static final int EXPIRE_TIME = 3600 * 1000 * 24 * 3;

    /**
     * S3 Bucket 名称，从环境变量读取
     * 环境变量名: AWS_S3_BUCKET_NAME
     * 默认值: "your-bucket-name"
     */
    public static String bucketName;

    private AmazonS3 s3client;

    @PostConstruct
    public void initBucketName() {
        String envBucketName = System.getenv("AWS_S3_BUCKET_NAME");
        if (envBucketName != null && !envBucketName.isEmpty()) {
            bucketName = envBucketName;
            log.info("Using S3 bucket from environment variable: {}", bucketName);
        } else {
            bucketName = "your-bucket-name";
            log.warn("AWS_S3_BUCKET_NAME not set in environment variables, using placeholder: {}", bucketName);
        }
    }

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

    /**
     * Upload a file to S3 and return the public URL
     * @param keyName S3 object key
     * @param file File to upload
     * @return Public URL for the uploaded file
     */
    public String uploadAndGetUrl(String keyName, File file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/webm");
            metadata.setContentLength(file.length());

            PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
            request.setMetadata(metadata);
            s3client.putObject(request);

            Date expiration = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            URL url = s3client.generatePresignedUrl(bucketName, keyName, expiration);
            log.info("File uploaded to S3. Key: {}, URL: {}", keyName, url);
            return url.toString();
        } catch (AmazonServiceException ase) {
            log.error("S3 upload error: {} - {}", ase.getStatusCode(), ase.getErrorCode());
            throw ase;
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Upload an input stream to S3 and return the public URL
     * @param keyName S3 object key
     * @param inputStream Input stream to upload
     * @return Public URL for the uploaded file
     */
    public String uploadStreamAndGetUrl(String keyName, InputStream inputStream) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/mpeg");

            // Convert to byte array to get length
            byte[] bytes = inputStream.readAllBytes();
            metadata.setContentLength(bytes.length);

            PutObjectRequest request = new PutObjectRequest(bucketName, keyName,
                    new java.io.ByteArrayInputStream(bytes), metadata);
            s3client.putObject(request);

            Date expiration = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            URL url = s3client.generatePresignedUrl(bucketName, keyName, expiration);
            log.info("Stream uploaded to S3. Key: {}, URL: {}", keyName, url);
            return url.toString();
        } catch (AmazonServiceException ase) {
            log.error("S3 upload error: {} - {}", ase.getStatusCode(), ase.getErrorCode());
            throw new RuntimeException("Failed to upload to S3", ase);
        } catch (Exception e) {
            log.error("Error uploading stream: {}", e.getMessage());
            throw new RuntimeException("Failed to upload stream", e);
        }
    }
}
