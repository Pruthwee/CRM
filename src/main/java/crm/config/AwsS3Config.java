package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 Configuration for cloud-native storage.
 * Uses DefaultCredentialsProvider which automatically uses:
 * - Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
 * - System properties
 * - IAM instance profile credentials (in EC2/ECS/EKS)
 * - Container credentials (in ECS)
 * - Web identity token credentials (in EKS)
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    /**
     * Creates S3Client bean for dependency injection.
     * Uses default credentials provider chain for cloud-native authentication.
     * 
     * @return Configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
