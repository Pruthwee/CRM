package crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;

/**
 * Redis Session Configuration for Cloud-Native Distributed Session Management
 * 
 * This configuration enables stateless horizontal scaling by storing HTTP session data
 * in an external Redis cache instead of in-memory server storage. This allows:
 * - Multiple application instances to share session state
 * - Session persistence across instance restarts
 * - Load balancing without sticky sessions
 * - Auto-scaling without session data loss
 * 
 * For AWS deployment, use Amazon ElastiCache for Redis.
 * For local development, use a local Redis instance or embedded Redis.
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

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Configure Redis connection factory with environment-based configuration
     * Supports AWS ElastiCache Redis endpoints via environment variables
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        // Set password only if provided (AWS ElastiCache may require authentication)
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Configure connection timeout and pool settings for cloud resilience
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = 
            JedisClientConfiguration.builder();
        jedisClientConfiguration.connectTimeout(Duration.ofMillis(redisTimeout));
        jedisClientConfiguration.usePooling();

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(
            redisConfig, jedisClientConfiguration.build());
        
        return jedisConnectionFactory;
    }

    /**
     * Configure RedisTemplate for session data serialization
     * Uses JSON serialization for better debugging and cross-platform compatibility
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values (better for debugging and compatibility)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
