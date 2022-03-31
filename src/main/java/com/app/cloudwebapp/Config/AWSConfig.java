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

@Configuration
public class AWSConfig {

    //public static final String BucketName = "your-bucket-name";
//     @Value("${amazonProperties.accesskey}")
//     private String awsAccessKey;

//     @Value("${amazonProperties.secretkey}")
//     private String awsSecretKey;

    @Value("${amazonProperties.region}")
    private String awsRegion = System.getenv("AWS_REGION");

   @Bean
   @Primary
   AmazonS3 generateS3Client(){
       return AmazonS3ClientBuilder
               .standard()
               .withRegion(awsRegion)
               .withCredentials(new AWSStaticCredentialsProvider(false))
               .build();
//    }

//     @Primary
//     @Bean
//     AmazonS3 generateS3Client() {
//         AWSCredentials awsCredentials =
//                 new BasicAWSCredentials(awsAccessKey, awsSecretKey);
//         return AmazonS3ClientBuilder
//                 .standard()
//                 .withRegion(awsRegion)
//                 .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                 .build();

    }
}
