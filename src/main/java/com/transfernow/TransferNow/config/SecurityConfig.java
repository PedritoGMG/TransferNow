package com.transfernow.TransferNow.config;

import com.transfernow.TransferNow.filter.TemporaryAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TemporaryAccessFilter tempFilter) throws Exception {
        http
                .addFilterBefore(tempFilter, AuthorizationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/requestUpload",
                                "/api/temp/**",
                                "/api/links/**",
                                "/index.html",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.woff",
                                "/*.woff2",
                                "/media/**",
                                "/api/secure-upload/**",
                                "/app/request/initiate",
                                "/ws/**"
                        ).permitAll()
                        .anyRequest().access(this::isLocalhost)
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    private AuthorizationDecision isLocalhost(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        IpAddressMatcher localhostMatcher = new IpAddressMatcher("127.0.0.1");
        IpAddressMatcher ipv6Matcher = new IpAddressMatcher("::1");

        boolean isLocal = localhostMatcher.matches(context.getRequest()) ||
                ipv6Matcher.matches(context.getRequest());

        return new AuthorizationDecision(isLocal);
    }
}
