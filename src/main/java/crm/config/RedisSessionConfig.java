package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis Session Configuration - Cloud-Native Distributed Session Management
 * 
 * This configuration enables distributed session management using Redis:
 * - Sessions are stored in Redis instead of local memory
 * - Enables horizontal scaling across multiple application instances
 * - Sessions persist independently of application instances
 * - Compatible with cloud load balancers
 * - Supports session failover and high availability
 * 
 * Benefits for Cloud Deployment:
 * - Stateless application instances
 * - No session affinity required at load balancer
 * - Instances can be added/removed dynamically
 * - Sessions survive instance restarts
 * - Works across multiple availability zones
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes session timeout
public class RedisSessionConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    /**
     * Creates Redis connection factory for distributed session storage.
     * Uses Lettuce client for better performance and connection pooling.
     * Configuration is externalized via environment variables for cloud deployment.
     * 
     * @return RedisConnectionFactory configured for cloud environment
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

    /**
     * Creates RedisTemplate for custom Redis operations.
     * Uses JSON serialization for better interoperability.
     * 
     * @param connectionFactory Redis connection factory
     * @return RedisTemplate configured with JSON serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values (better for complex objects)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
