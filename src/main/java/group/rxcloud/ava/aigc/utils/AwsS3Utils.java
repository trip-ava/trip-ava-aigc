package group.rxcloud.ava.aigc.utils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import group.rxcloud.ava.aigc.config.AwsConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AwsS3Utils {

    private static String bucketName = "";

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

//    public String uploadFileToS3(InputStream inputStream, String fileName) {
//
//    }
//
//    public String genPresignedUrlFromS3Key(String s3Key) {
//
//    }s3Key
}
