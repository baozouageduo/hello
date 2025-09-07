package com.tests.campuslostandfoundsystem.config;


import com.tests.campuslostandfoundsystem.security.JsonAccessDeniedHandler;
import com.tests.campuslostandfoundsystem.security.JsonAuthEntryPoint;
import com.tests.campuslostandfoundsystem.security.SkipPathRequestMatcher;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // TODO: Inject your real JWT filter bean here (if you have one)
    // @Autowired private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/refreshToken", "/auth/GraphCaptcha").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
