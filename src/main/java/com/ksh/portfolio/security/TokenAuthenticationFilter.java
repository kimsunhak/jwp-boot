package com.ksh.portfolio.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksh.portfolio.exception.JwtException;
import com.ksh.portfolio.exception.ResourceNotFoundException;
import com.ksh.portfolio.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private final CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = tokenProvider.getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
//                Long userId = tokenProvider.getMemberIdFromToken(jwt);
                tokenProvider.validateAccessToken(jwt);
                authMember(request, jwt);
            }
        } catch (JwtException e) {
            logger.error(e.getMessage());
            jwtResponse(response, e.getErrorCode(), e.getMessage());
            return;
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            jwtResponse(response, e.getErrorCode(), e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Jwt 토큰으로 인증정보 조회
     * @param request
     * @param jwt
     */
    private void authMember(HttpServletRequest request, String jwt) {
        UserDetails userDetails = loadUserByJwt(jwt);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void jwtResponse(HttpServletResponse response, String errorCode, String message) throws IOException {
        ApiResponse apiResponse = new ApiResponse(false, errorCode, message);
        String responseString = objectMapper.writeValueAsString(apiResponse);
        response.setContentType("application/json;charset=UTF-8");
        logger.info("responseString : " + responseString);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(responseString);
    }

    /**
     * JWT 토큰으로 DB 정보 조회
     * @param jwt
     * @return
     */
    private UserDetails loadUserByJwt(String jwt) {
        String role = tokenProvider.getRoleFromToken(jwt);
        if (role.equals("ROLE_MEMBER")) {
            return customUserDetailsService.loadUserById(tokenProvider.getMemberIdFromToken(jwt));
        } else if (role.equals("ROLE_ADMIN")) {
            return customUserDetailsService.loadUserById(tokenProvider.getMemberIdFromToken(jwt));
        }
        return null;
    }
}
