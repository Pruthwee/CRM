package crm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Cloud Configuration for Azure deployment
 * Provides cloud-optimized beans and configurations
 */
@Configuration
@Slf4j
public class CloudConfiguration {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    /**
     * Configure HikariCP DataSource for cloud-optimized connection pooling
     * This bean is only active when 'azure' profile is enabled
     */
    @Bean
    @Profile("azure")
    public DataSource azureDataSource() {
        log.info("Configuring Azure-optimized HikariCP DataSource");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(datasourceUrl);
        config.setUsername(datasourceUsername);
        config.setPassword(datasourcePassword);
        
        // Connection pool settings optimized for cloud
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Additional cloud-optimized settings
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("CRM-HikariCP-Pool");
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(60000); // 60 seconds
        
        // MySQL-specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        log.info("HikariCP configured with maxPoolSize={}, minIdle={}", maxPoolSize, minIdle);
        
        return new HikariDataSource(config);
    }
}
