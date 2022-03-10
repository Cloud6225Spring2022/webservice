package com.app.cloudwebapp.Config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
public class AWSConfig {
	
    //public static final String BucketName = "your-bucket-name";

    //@Value("${amazonProperties.region}")
    private String awsRegion = "us-east-1";

    @Bean
    @Primary
    AmazonS3 generateS3Client(){
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(awsRegion)
                .build();
    }

//    @Bean
//    public AmazonS3 s3() {
//        AWSCredentials awsCredentials =
//                new BasicAWSCredentials("accessKey", "secretKey");
//        return AmazonS3ClientBuilder
//                .standard()
//                .withRegion("ap-south-1")
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//
//    }

}
