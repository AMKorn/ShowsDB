package com.andreas.showsdb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

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

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<String> authorities = AuthorityUtils.authorityListToSet(authoritiesConverter.convert(jwt));
            System.out.println(authorities);
//            if (authorities.contains("flights:write")) {
//                authorities.add("flights:approve");
//            }
//            if (authorities.contains("flights:read")) {
//                authorities.add("flights:all");
//            }
            return AuthorityUtils.createAuthorityList(authorities.toArray(String[]::new));
        });

        return authenticationConverter;
    }
}
