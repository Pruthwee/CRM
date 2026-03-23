package crm;

import crm.service.SpringDataUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Security Configuration - Cloud-Native with Distributed Session Support
 * 
 * This configuration has been updated for cloud readiness:
 * - Supports distributed session management via Redis
 * - Compatible with horizontal scaling
 * - Works with load balancers (no session affinity required)
 * - Sessions are stored externally, not in application memory
 * - Enables stateless application instances
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringDataUserDetailsService customUserDetailsService() {
        return new SpringDataUserDetailsService();
    }

    /**
     * HttpSessionEventPublisher for distributed session management.
     * Required for proper session lifecycle management with Redis.
     * Ensures session creation/destruction events are properly handled
     * across multiple application instances.
     * 
     * @return HttpSessionEventPublisher for session event handling
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService()).passwordEncoder(passwordEncoder());
    }

    /**
     * HTTP Security configuration with distributed session support.
     * 
     * Cloud-Native Features:
     * - Sessions stored in Redis (external to application)
     * - No session affinity required at load balancer
     * - Sessions survive instance restarts
     * - Compatible with auto-scaling
     * - Works across multiple availability zones
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**", "/user/delete/**").hasRole("ADMIN")
                .antMatchers("/pdf-generator", "/search/**", "/customer/**", "/user/edit/**", "/user/list", "/contract/**").hasAnyRole( "ADMIN", "USER", "MANAGER", "OWNER")
                .anyRequest().permitAll()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and()
                .logout().logoutSuccessUrl("/").permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/403")
                .and()
                // Session management configuration for distributed sessions
                .sessionManagement()
                    // Maximum 1 session per user (can be adjusted for cloud requirements)
                    .maximumSessions(1)
                    // Prevent login if max sessions reached (can be set to false for better UX)
                    .maxSessionsPreventsLogin(false)
                    // Expire old sessions when new session is created
                    .expiredUrl("/login?expired");
    }

}
