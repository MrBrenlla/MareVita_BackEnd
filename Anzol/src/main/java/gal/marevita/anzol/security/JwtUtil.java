package gal.marevita.anzol.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
  private final String SECRET_KEY =
      "DoQueixoNonCheMeQueixo" +
          "QueOQueixoBenCheMeSabe" +
          "QueixomeDeQuenOVende" +
          "QueNonMoDeixaDeBalde";

  byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

  public static String getActualUserName() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  public static String getActualUserId() {
    return (String) SecurityContextHolder.getContext().getAuthentication().getDetails();
  }

  public String generateToken(String username, String id) {
    return Jwts.builder()
        .setSubject(username)
        .claim("UserId", id)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
        .signWith(SignatureAlgorithm.HS256, keyBytes)
        .compact();
  }

  public Claims extractClaims(String token) {
    return Jwts.parser().setSigningKey(keyBytes).parseClaimsJws(token).getBody();
  }

  public boolean isTokenValid(String token, String username) {
    return extractClaims(token).getSubject().equals(username)
        && !isTokenExpired(token);
  }

  public String extractUserName(String token) {
    return extractClaims(token).getSubject();
  }

  public String extractUserId(String token) {
    return extractClaims(token).get("UserId", String.class);
  }

  private boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
  }
}
