package com.ksh.portfolio.security.oauth2;


import com.ksh.portfolio.exception.BadRequestException;
import com.ksh.portfolio.payload.response.AuthResponse;
import com.ksh.portfolio.security.Token;
import com.ksh.portfolio.security.UserPrincipal;
import com.ksh.portfolio.service.auth.AuthService;
import com.ksh.portfolio.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

import static com.ksh.portfolio.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy/MM/dd HH:mm:ss", Locale.KOREA );
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);

        if (!redirectUri.isPresent()) {
            throw new BadRequestException("인증되지 않은 REDIRECT_URI입니다.");
        }

        String targetUri = redirectUri.orElse(getDefaultTargetUrl());

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse accessAndRefreshToken = authService.createAccessAndRefreshToken(userPrincipal.getId());
        Token accessToken = accessAndRefreshToken.getAccessToken();
        Token refreshToken = accessAndRefreshToken.getRefreshToken();

        return UriComponentsBuilder.fromUriString(targetUri)
                .queryParam("accessToken",accessToken.getJwtToken())
                .queryParam("accessTokenExpiryDate", formatter.format(accessToken.getExpiryDate()))
                .queryParam("refreshToken", refreshToken.getJwtToken())
                .queryParam("refreshTokenExpiryDate", formatter.format(refreshToken.getExpiryDate()))
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}

