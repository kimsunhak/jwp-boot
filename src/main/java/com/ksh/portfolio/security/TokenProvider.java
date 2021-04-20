package com.ksh.portfolio.security;

import com.ksh.portfolio.config.AppProperties;
import com.ksh.portfolio.exception.JwtException;
import com.ksh.portfolio.exception.RequestParamException;
import com.ksh.portfolio.exception.ResourceNotFoundException;
import com.ksh.portfolio.repository.member.MemberRefreshTokenRepository;
import com.ksh.portfolio.repository.member.MemberRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final AppProperties appProperties;

    private final MemberRepository memberRepository;

    private final MemberRefreshTokenRepository memberRefreshTokenRepository;

    /**
     * UserToken 토큰 생성, 만료시간 X
     * @param authentication
     * @return
     */
    public Token createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date accessExpiryDate = new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpirationMsec());
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("type", "access")
                .claim("role", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))
                )
                .setIssuedAt(new Date())
                .setExpiration(accessExpiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new Token(accessToken, accessExpiryDate);
    }

    /**
     * UserToken 토큰 생성, 만료시간 O
     * @param authentication
     * @param expiryDate
     * @return
     */
    public Token createToken(Authentication authentication, long expiryDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date accessExpiryDate = new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpirationMsec());
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("type", "access")
                .claim("role", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .setIssuedAt(now)
                .setExpiration(accessExpiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new Token(accessToken, accessExpiryDate);
    }

    public Token createRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date refreshExpiryDate = new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpirationMsec());
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(refreshExpiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new Token(accessToken, refreshExpiryDate);
    }

    public Token createRefreshToken(Authentication authentication, long expiryDate) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date refreshExpiryDate = new Date(now.getTime() + expiryDate);
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(refreshExpiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new Token(accessToken, refreshExpiryDate);
    }

    /**
     * Token 유효성검사
     * @param token
     */
    public void validateToken(String token) {
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
    }

    /**
     * AccessToken 유효성검사
     * @param accessToken
     */
    public void validateAccessToken(String accessToken) {

        if (!getTypeFromToken(accessToken).equals("access")) {
            throw new JwtException("토큰 타입이 불일치 합니다. access Token이 필요합니다", "404");
        }

        try {
            validateToken(accessToken);
        } catch (SignatureException exception) {
            throw new JwtException("Invalid JWT signature", "400");
        } catch (MalformedJwtException exception) {
            throw new JwtException("Invalid JWT token", "401");
        } catch (ExpiredJwtException exception) {
            throw new JwtException("Expired JWT token", "402");
        } catch (UnsupportedJwtException exception) {
            throw new JwtException("Unsupported JWT token", "403");
        }
    }

    /**
     * refreshToken 유효성검사
     * @param refreshToken
     * @return
     */
    public Long validateRefreshToken(String refreshToken) {
        if (!getTypeFromToken(refreshToken).equals("refresh")) {
            logger.error("토큰 타입이 불일치 합니다. refresh token이 필요합니다");
            throw new JwtException("토큰 타입이 불일치 합니다. refresh token이 필요합니다.", "404");
        }

        validateToken(refreshToken);

        Long memberId = getMemberIdFromToken(refreshToken);

        if (!memberRepository.existsById(memberId)) {
            logger.error("토큰에 기록된 유저(" + memberId + ")가 존재하지 않습니다.");
            throw new ResourceNotFoundException("user", "id", memberId, "200");
        }

        if (!memberRefreshTokenRepository.existsByMemberIdAndRefreshToken(memberId, refreshToken)) {
            logger.error("유저(" + memberId + ") 저장된 리프레시 토큰과 일치하지 않습니다.");
            throw new JwtException("유저(" + memberId + ") 저장된 리프레시 토큰과 일치하지 않습니다.", "403");
        }
        return memberId;
    }


    /**
     * Token에 UserId값 조회
     * @param token
     * @return
     */
    public Long getMemberIdFromToken(String token) {
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Token에 Type값 조회
     * @param token
     * @return
     */
    public String getTypeFromToken(String token) {
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type").toString();
    }

    /**
     * Token에 Role값 조회
     * @param token
     * @return
     */
    public String getRoleFromToken(String token) {
        String jwtSecret = Base64Utils.encodeToString(appProperties.getAuth().getTokenSecret().getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role").toString();
    }

    /**
     * Header에 토큰값이 있는지 확인
     * @param request
     * @return
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }

        if (!bearerToken.startsWith("Bearer ")) {
            throw new RequestParamException("jwt Token은 Bearer로 시작해야 합니다", "402");
        }

        return bearerToken.substring(7);
    }

}

