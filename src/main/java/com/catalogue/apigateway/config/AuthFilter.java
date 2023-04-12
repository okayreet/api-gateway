package com.catalogue.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import java.net.URI;
import java.net.URISyntaxException;

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
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().set(VALUE_KEY, NAME_KEY);
            String target = exchange.getRequest().getURI().toString();

            return redirectToLogin(exchange, chain);
            // exchange.getAttributes().put(GATEWAY_PREDICATE_MATCHED_PATH_ATTR,
            // "/api/v1/auth/login");
            // return chain.filter(exchange);
            // System.out.println("target is: " + target);
            // if (isAuthMissing(request))
            // return onError(exchange, "Authorization header is missing in request",
            // HttpStatus.UNAUTHORIZED);

            // log.info("Authorization header exists!");
            // String token = getAuthHeader(request);

            // if (token.startsWith("Basic") && !target.endsWith("auht/login"))
            // return chain.filter(exchange);
            // if (!token.startsWith("Bearer"))
            // return onError(exchange, "Authorization header not Bearer",
            // HttpStatus.UNAUTHORIZED);
            // token = token.substring(7);

            // if (jwtUtil.isInvalid(token))
            // populateRequestWithHeaders(exchange, token);
            // // return this.onError(exchange, "Authorization header is invalid",
            // HttpStatus.UNAUTHORIZED);

            // return chain.filter(exchange);
        };
    }

    private Mono<Void> redirectToLogin(ServerWebExchange exchange, GatewayFilterChain chain) {

        URI redirectURI = null;
        try {
            redirectURI = new URI("http://localhost:8084/api/v1/auth/login");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ServerHttpRequest modifiedRequest = exchange
                .getRequest()
                .mutate()
                .uri(redirectURI)
                .build();

        ServerWebExchange modifiedExchange = exchange
                .mutate()
                .request(modifiedRequest)
                .build();

        modifiedExchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, redirectURI);

        return chain.filter(modifiedExchange);
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        exchange.getRequest().mutate()
                .header("id", String.valueOf(claims.get("id")))
                .header("role", String.valueOf(claims.get("role")))
                .build();
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