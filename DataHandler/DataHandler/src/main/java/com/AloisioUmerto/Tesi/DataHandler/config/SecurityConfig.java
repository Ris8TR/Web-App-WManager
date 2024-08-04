package com.AloisioUmerto.Tesi.DataHandler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**").permitAll()
                        /* If needed, uncomment these lines for specific routes
                        .requestMatchers(HttpMethod.GET, "/v1/admin/users").denyAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/{idUser}").permitAll()
                        */
                        .anyRequest().authenticated()
                )
                .cors(cors -> {});  // Use this to configure CORS if needed

        return http.build();
    }

}