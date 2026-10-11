package com.example.clinic;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(antMatcher("/css/**"), antMatcher("/login")).permitAll()
                // Permanent deletes: ADMIN only
                .requestMatchers(
                        antMatcher(HttpMethod.POST, "/owners/*/delete"),
                        antMatcher(HttpMethod.POST, "/owners/*/pets/*/delete"),
                        antMatcher(HttpMethod.POST, "/appointments/*/delete")).hasRole("ADMIN")
                // Doctor accounts + database console: ADMIN only
                .requestMatchers(antMatcher("/admin/**"), antMatcher("/h2-console/**")).hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll())
            .csrf(c -> c.ignoringRequestMatchers(antMatcher("/h2-console/**")))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
