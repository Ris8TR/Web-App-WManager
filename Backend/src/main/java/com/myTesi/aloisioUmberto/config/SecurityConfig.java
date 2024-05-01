package com.myTesi.aloisioUmberto.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
            http
                    .csrf()
                    .disable()
                    .authorizeHttpRequests()
                    .requestMatchers("/**"
                    ).permitAll()

                    /*      Questi se servono in caso si attivano
                    .requestMatchers(HttpMethod.GET,"/v1/admin/users").denyAll()
                    .requestMatchers(HttpMethod.GET,"/v1/users/{idUser}").permitAll()
                    */

                    .anyRequest()
                    .authenticated()
            ;

            http.cors();


            return http.build();
        }

}