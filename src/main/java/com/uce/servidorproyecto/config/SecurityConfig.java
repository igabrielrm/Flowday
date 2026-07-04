package com.uce.servidorproyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Permitir TODAS las rutas sin autenticación (para que nuestro AuthController maneje todo)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Deshabilitar el formulario de login de Spring Security
            .formLogin(form -> form.disable())
            // Deshabilitar el logout de Spring Security
            .logout(logout -> logout.disable())
            // Deshabilitar la autenticación HTTP Basic
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}