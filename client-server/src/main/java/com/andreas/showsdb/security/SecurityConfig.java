package com.andreas.showsdb.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
public class SecurityConfig {

    @Value("${showsdb.env}")
    private String env;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> {
                    if (!env.equals("dev")) {
                        String apiEndpoints = "/api/**";
                        String admin = "ADMIN";
                        String user = "USER";
                        authHttp.requestMatchers("/authorized").permitAll()
                                .requestMatchers(HttpMethod.GET, apiEndpoints).hasAnyRole(user, admin)
                                .requestMatchers(apiEndpoints).hasRole(admin)
                                .requestMatchers("/graphql").hasRole(admin)
                                .requestMatchers("/graphiql").hasRole(admin)
                                .anyRequest().permitAll();
                    } else {
                        authHttp.anyRequest().permitAll();
                    }
                })
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

            // Get the roles from the token
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null)
                log.error("Authorization server did not specify roles");
            else
                authorities.addAll(roles);
            log.info("New authorities: %s".formatted(authorities));
            return AuthorityUtils.createAuthorityList(authorities.toArray(String[]::new));
        });

        return authenticationConverter;
    }
}
