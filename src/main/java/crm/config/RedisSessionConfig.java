package crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

/**
 * Redis Session Configuration for Cloud-Native Distributed Session Management
 * 
 * This configuration enables the application to store HTTP sessions in Redis instead of 
 * in-memory, which is critical for cloud deployments where:
 * 1. Multiple instances need to share session state
 * 2. Instances can be terminated and recreated dynamically
 * 3. Load balancers distribute requests across multiple servers
 * 
 * Benefits:
 * - Enables horizontal scaling without session affinity
 * - Prevents session data loss when instances are terminated
 * - Supports stateless application architecture
 * - Compatible with AWS ElastiCache, Azure Cache for Redis, or GCP Memorystore
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
public class RedisSessionConfig {

    /**
     * Configure Redis connection factory for session storage
     * Uses Lettuce client for better performance and connection pooling
     * 
     * @return RedisConnectionFactory configured for cloud environment
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // LettuceConnectionFactory is configured via application.properties
        // This allows environment-specific configuration through environment variables
        return new LettuceConnectionFactory();
    }

    /**
     * Configure session strategy to support both cookie and header-based sessions
     * This is useful for REST APIs and web applications
     * 
     * @return HttpSessionStrategy for flexible session management
     */
    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        // HeaderHttpSessionStrategy allows session tokens in HTTP headers
        // This is more cloud-native and works better with load balancers
        return new HeaderHttpSessionStrategy();
    }
}
