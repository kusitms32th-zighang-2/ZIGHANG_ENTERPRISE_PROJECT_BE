package zighang2.zighang.global.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zighang2.zighang.global.auth.UserPrincipal;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.BadRequestHandler;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.global.service.RedisService;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.kakaoLogin.TokenResponseDto;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

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

    private final RedisService redisService;

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
                .setSubject(String.valueOf(user.getId()))
                .claim("role", user.getUserRole().name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpirationTime);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("role", user.getUserRole().name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token,String tokenType) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            if (Objects.equals(tokenType, "refresh") && redisService.checkExistsValue(token)) {
                return false;
            }

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

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    // 토큰에서 userId 뽑는 메소드
    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        throw new NotFoundHandler(ErrorStatus.USER_NOT_FOUND);
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
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadRequestHandler(ErrorStatus.INVALID_TOKEN);
        }
    }

    public TokenResponseDto.RefreshTokenResponseDto recreate(User user, String refreshToken) {
        String accessToken = createAccessToken(user);

        if(getExpirationTime(refreshToken) <= getExpirationTime(accessToken)) {
            refreshToken = createRefreshToken(user);
        }

        redisService.setRefreshToken(user.getId(), refreshToken);

        return TokenResponseDto.RefreshTokenResponseDto.of(user.getId(),accessToken,refreshToken);
    }

    public Long getExpirationTime(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().getTime();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
