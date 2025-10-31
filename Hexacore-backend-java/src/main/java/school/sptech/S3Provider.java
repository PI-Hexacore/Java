package school.sptech;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Provider {

    public S3Client getS3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                "ASIA3HSRJ576ZUSTCMJ2",
                "hNyrUQQxJkfwS21U8cYW9g3HBbHYgkiGF97fLBKG"
        );

        return S3Client.builder()
                .region(Region.US_EAST_1) // ajuste para a região do seu bucket
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
