package com.catalogue.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.catalogue.apigateway.exception.AppException;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthFilter(WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("Into apply 1");
        return (exchange, chain) -> {
            log.info("Into exchange!");
            ServerHttpRequest request = exchange.getRequest();

            if (isAuthMissing(request))
                return onError(exchange, "Authorization header is missing in request",
                        HttpStatus.UNAUTHORIZED);

            log.info("Authorization header exists!");
            String token = getAuthHeader(request);

            if (!token.startsWith("Bearer") || jwtUtil.isInvalid(token.substring(7)))
                return onError(exchange, "Authorization header is ivalid, please login",
                        HttpStatus.UNAUTHORIZED);

            return chain.filter(exchange);
        };
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        return Mono.error(new AppException(err));
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    public static class Config {
        // empty class as I don't need any particular configuration
    }
}