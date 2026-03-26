package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * Redis Session Configuration for Cloud-Native Distributed Session Management
 * 
 * This configuration enables stateless horizontal scaling by storing HTTP sessions
 * in Redis instead of in-memory. This allows:
 * - Multiple application instances to share session data
 * - Session persistence across instance restarts
 * - Load balancing without sticky sessions
 * - Auto-scaling in cloud environments (AWS, Azure, GCP)
 * 
 * For AWS deployment, configure REDIS_HOST to point to ElastiCache endpoint
 * For local development, Redis can be disabled by setting SESSION_STORE_TYPE=none
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    /**
     * Configure Redis connection factory for session storage
     * Uses Lettuce client for better performance and connection pooling
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        
        // Set password only if provided (AWS ElastiCache may require authentication)
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * Configure session ID resolution strategy
     * Uses HTTP headers for better REST API compatibility
     * Falls back to cookies for browser-based sessions
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }
}
