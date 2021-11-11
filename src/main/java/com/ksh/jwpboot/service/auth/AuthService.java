package com.ksh.jwpboot.service.auth;

import com.ksh.jwpboot.domain.member.MemberRefreshToken;
import com.ksh.jwpboot.payload.request.JwtRequest;
import com.ksh.jwpboot.payload.response.AuthResponse;
import com.ksh.jwpboot.repository.member.MemberRefreshTokenRepository;
import com.ksh.jwpboot.security.CustomUserDetailsService;
import com.ksh.jwpboot.security.Token;
import com.ksh.jwpboot.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;

    private final CustomUserDetailsService customUserDetailsService;

    private final MemberRefreshTokenRepository memberRefreshTokenRepository;

    @Transactional
    public AuthResponse createAccessAndRefreshToken(Long memberId) {
        UserDetails userDetails = customUserDetailsService.loadUserById(memberId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Token accessToken = tokenProvider.createToken(authentication);
        Token refreshToken = tokenProvider.createRefreshToken(authentication);

        Optional<MemberRefreshToken> byMemberId = memberRefreshTokenRepository.findByMemberId(memberId);

        if (byMemberId.isPresent()) {
            byMemberId.get().updateRefreshToken(refreshToken.getJwtToken());
        } else {
            memberRefreshTokenRepository.save(new MemberRefreshToken(memberId, refreshToken.getJwtToken()));
        }

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse createAccessAndRefreshToken(Long memberId, long accessExpiryDate, long refreshExpiryDate) {

        UserDetails userDetails = customUserDetailsService.loadUserById(memberId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Token accessToken = tokenProvider.createToken(authentication, accessExpiryDate);
        Token refreshToken = tokenProvider.createRefreshToken(authentication, refreshExpiryDate);

        Optional<MemberRefreshToken> byMemberId = memberRefreshTokenRepository.findByMemberId(memberId);

        if (byMemberId.isPresent()) {
            byMemberId.get().updateRefreshToken(refreshToken.getJwtToken());
        } else {
            memberRefreshTokenRepository.save(new MemberRefreshToken(memberId, refreshToken.getJwtToken()));
        }
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshAccessAndRefreshToken(JwtRequest jwtRequest) {
        String token = jwtRequest.getJwt();
        final Long memberId = tokenProvider.validateRefreshToken(token);

        UserDetails userDetails = customUserDetailsService.loadUserById(memberId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Token accessToken = tokenProvider.createToken(authentication);
        Token refreshToken = tokenProvider.createRefreshToken(authentication);

        Optional<MemberRefreshToken> byMemberId = memberRefreshTokenRepository.findByMemberId(memberId);

        if (byMemberId.isPresent()) {
            byMemberId.get().updateRefreshToken(refreshToken.getJwtToken());
        } else {
            memberRefreshTokenRepository.save(new MemberRefreshToken(memberId, refreshToken.getJwtToken()));
        }

        return new AuthResponse(accessToken, refreshToken);
    }

    public Token refreshAccessToken(JwtRequest jwtRequest) {
        String refreshToken = jwtRequest.getJwt();
        final Long memberId = tokenProvider.validateRefreshToken(refreshToken);

        UserDetails userDetails = customUserDetailsService.loadUserById(memberId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return tokenProvider.createToken(authentication);
    }


}

