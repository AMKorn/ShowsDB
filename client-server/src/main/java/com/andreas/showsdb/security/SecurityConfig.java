package com.andreas.showsdb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> {
                            String apiEndpoints = "/api/**";
                            String read = "SCOPE_read";
                            String write = "SCOPE_write";
                            authHttp.requestMatchers("/authorized").permitAll()
                                    .requestMatchers(HttpMethod.GET, apiEndpoints).hasAnyAuthority(read, write)
                                    .requestMatchers(HttpMethod.POST, apiEndpoints).hasAuthority(write)
                                    .requestMatchers(HttpMethod.PUT, apiEndpoints).hasAuthority(write)
                                    .requestMatchers(HttpMethod.DELETE, apiEndpoints).hasAuthority(write)
                                    .anyRequest().authenticated();
                        }
                )
                .csrf(AbstractHttpConfigurer::disable) // disable forms because it's not necessary for REST APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(login -> login.loginPage("/oauth2/authorization/client-showsdb"))
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }
}
