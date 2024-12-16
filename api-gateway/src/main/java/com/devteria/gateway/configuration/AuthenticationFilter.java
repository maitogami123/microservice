package com.devteria.gateway.configuration;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import com.devteria.gateway.dto.ApiResponse;
import com.devteria.gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
  IdentityService identityService;
  ObjectMapper objectMapper;

  @Override
  public int getOrder() {
    return -1;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Enter authentication filter...");

    List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
    if (CollectionUtils.isEmpty(authHeader)) {
      return unauthenticated(exchange.getResponse());
    }

    String token = authHeader.getFirst().replace("Bearer ", "");
    log.info(token);

    return identityService.introspect(token).flatMap(introspectReponseApiResponse -> {
      if (introspectReponseApiResponse.getResult().isValid()) {
        return chain.filter(exchange);
      } else {
        return unauthenticated(exchange.getResponse());
      }
    }).onErrorResume(throwable -> {
      return unauthenticated(exchange.getResponse());
    });
  }

  Mono<Void> unauthenticated(ServerHttpResponse response) {
    ApiResponse<?> apiResponse = ApiResponse.builder().code(1401).message("Unauthenticated").build();
    String body = null;
    try {
      body = objectMapper.writeValueAsString(apiResponse);
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
  }
}