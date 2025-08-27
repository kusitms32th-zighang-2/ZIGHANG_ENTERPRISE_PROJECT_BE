package zighang2.zighang.global.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.BadRequestHandler;
import zighang2.zighang.web.domain.user.User;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;
    private Key key;

    @Value("${jwt.token.access-expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshTokenExpirationTime;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access Token 생성
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationTime);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getUserRole())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpirationTime);
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new BadRequestHandler(ErrorStatus.MALFORMED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new BadRequestHandler(ErrorStatus.UNSUPPORTED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new BadRequestHandler(ErrorStatus.EXPIRED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BadRequestHandler(ErrorStatus.EMPTY_CLAIMS);
        } catch (JwtException e) {
            throw new BadRequestHandler(ErrorStatus.INVALID_TOKEN);
        }
    }

    // 토큰에서 사용자 id 추출
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        }catch (ExpiredJwtException e) {
            throw new BadRequestHandler(ErrorStatus.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new BadRequestHandler(ErrorStatus.MALFORMED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new BadRequestHandler(ErrorStatus.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new BadRequestHandler(ErrorStatus.EMPTY_CLAIMS);
        } catch (JwtException e) {
            throw new BadRequestHandler(ErrorStatus.INVALID_TOKEN);
        }
    }

    // 토큰 만료 확인
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
