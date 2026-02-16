package crm;

import crm.service.SpringDataUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringDataUserDetailsService customUserDetailsService() {
        return new SpringDataUserDetailsService();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers(request -> request.getServletPath().startsWith("/admin/") || request.getServletPath().startsWith("/user/delete/")).hasRole("ADMIN")
                .requestMatchers(request ->
                    request.getServletPath().equals("/pdf-generator") ||
                    request.getServletPath().startsWith("/search/") ||
                    request.getServletPath().startsWith("/customer/") ||
                    request.getServletPath().startsWith("/user/edit/") ||
                    request.getServletPath().equals("/user/list") ||
                    request.getServletPath().startsWith("/contract/")
                ).hasAnyRole("ADMIN", "USER", "MANAGER", "OWNER")
                .anyRequest().permitAll()
        )
        .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
        )
        .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
        )
        .exceptionHandling(exception -> exception
                .accessDeniedPage("/403")
        );
        return http.build();
    }

}
