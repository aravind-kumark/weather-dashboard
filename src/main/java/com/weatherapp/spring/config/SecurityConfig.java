package com.weatherapp.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Allow all requests
            .formLogin(form -> form.disable()) // Disable login form
            .httpBasic(httpBasic -> httpBasic.disable()) // Disable basic authentication
            .build();
    }
  
    @Bean
    public RestTemplate restTemplate() {
            return new RestTemplate();
        }
}
