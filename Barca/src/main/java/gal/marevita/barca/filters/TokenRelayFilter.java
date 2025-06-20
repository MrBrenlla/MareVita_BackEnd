package gal.marevita.barca.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenRelayFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap(securityContext -> {
          Authentication authentication = securityContext.getAuthentication();
          if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {

            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                .build();

            return chain.filter(exchange.mutate().request(newRequest).build());
          }
          return chain.filter(exchange);
        })
        .switchIfEmpty(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}