package com.sumax.pdfa.security;

import com.sumax.pdfa.dao.ClientDao;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PdfaSecurityProperties.class)
public class PdfaSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientDao clientDao,
                                                  PdfaSecurityProperties properties) {
        return http
                // API sin cookies ni sesion; usa X-API-Key o X-Admin-Key.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/conversion-pdfa").hasAuthority("PDFA_CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/clientes/conversiones-restantes").hasAuthority("PDFA_CLIENT")
                        .requestMatchers("/api/clientes", "/api/clientes/**").hasAuthority("PDFA_ADMIN")
                        .anyRequest().denyAll())
                .addFilterBefore(new ClientAuthenticationFilter(clientDao, properties), AnonymousAuthenticationFilter.class)
                .build();
    }
}
