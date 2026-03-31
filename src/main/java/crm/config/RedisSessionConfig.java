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
 * Redis Session Configuration for Cloud-Native Stateless Architecture
 * 
 * This configuration enables distributed session management using Redis,
 * allowing the application to scale horizontally across multiple instances
 * without session affinity (sticky sessions).
 * 
 * Benefits:
 * - Stateless application instances
 * - Horizontal scaling without session loss
 * - Session persistence across instance restarts
 * - Load balancing without sticky sessions
 * 
 * Environment Variables Required:
 * - REDIS_HOST: Redis server hostname (default: localhost)
 * - REDIS_PORT: Redis server port (default: 6379)
 * - REDIS_PASSWORD: Redis password (optional, default: empty)
 * - SESSION_TIMEOUT: Session timeout in seconds (default: 1800 = 30 minutes)
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
     * 
     * In AWS, this should connect to:
     * - Amazon ElastiCache for Redis (recommended for production)
     * - Self-managed Redis on EC2 (for development/testing)
     * 
     * Configuration via environment variables:
     * - SPRING_REDIS_HOST: ElastiCache endpoint
     * - SPRING_REDIS_PORT: ElastiCache port (usually 6379)
     * - SPRING_REDIS_PASSWORD: Auth token if enabled
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        
        // Set password only if provided
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        
        // Enable connection pooling for better performance
        factory.setShareNativeConnection(true);
        factory.setValidateConnection(true);
        
        return factory;
    }
}
