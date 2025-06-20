package gal.marevita.barca.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private final String SECRET_KEY =
      "DoQueixoNonCheMeQueixo" +
          "QueOQueixoBenCheMeSabe" +
          "QueixomeDeQuenOVende" +
          "QueNonMoDeixaDeBalde";

  byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/user/register", "/user/login").permitAll()
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
        )
        .build();
  }


  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    return token -> {
      try {
        Jws<Claims> jws = Jwts.parserBuilder()
            .setSigningKey(keyBytes)
            .build()
            .parseClaimsJws(token);

        Claims claims = jws.getBody();

        Jwt jwt = new Jwt(
            token,
            claims.getIssuedAt().toInstant(),
            claims.getExpiration().toInstant(),
            Map.of("alg", "HS256", "typ", "JWT"),
            claims
        );

        return Mono.just(jwt);
      } catch (JwtException e) {
        return Mono.error(new BadJwtException("Token inválido", e));
      }
    };
  }
}