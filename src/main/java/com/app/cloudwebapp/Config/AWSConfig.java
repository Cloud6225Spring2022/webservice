package com.app.cloudwebapp.Config;





import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

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
               .withCredentials(new InstanceProfileCredentialsProvider(false))
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
    
     @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .withRegion(awsRegion)
                .build();
        return new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);
    }
}
