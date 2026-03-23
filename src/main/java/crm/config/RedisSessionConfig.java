package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Redis Session Configuration for Cloud-Native Distributed Session Management
 * 
 * This configuration enables the application to store HTTP sessions in Redis instead of
 * in-memory, making it stateless and horizontally scalable in cloud environments.
 * 
 * Benefits:
 * - Sessions persist across application restarts
 * - Load balancing works without sticky sessions
 * - Horizontal scaling without session affinity
 * - Cloud-native architecture (12-factor app compliant)
 * 
 * For AWS deployment, use Amazon ElastiCache for Redis
 * For Azure deployment, use Azure Cache for Redis
 * For GCP deployment, use Cloud Memorystore for Redis
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    /**
     * Configure Redis connection factory for session storage
     * Uses environment variables for cloud deployment flexibility
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        
        // Set password only if provided (for cloud environments)
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        
        return new LettuceConnectionFactory(redisConfig);
    }
}
