package com.ksh.jwpboot.controller.auth;

import com.ksh.jwpboot.exception.RequestParamException;
import com.ksh.jwpboot.payload.request.JwtRequest;
import com.ksh.jwpboot.payload.request.LoginRequest;
import com.ksh.jwpboot.payload.response.ApiResponse;
import com.ksh.jwpboot.payload.response.AuthResponse;
import com.ksh.jwpboot.security.Token;
import com.ksh.jwpboot.security.TokenProvider;
import com.ksh.jwpboot.security.UserPrincipal;
import com.ksh.jwpboot.service.auth.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    private final TokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;


    @ApiOperation(value = "Token 발급", notes = "새로 갱신된 Token을 발급합니다.")
    @PostMapping("auth/access/token")
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody JwtRequest jwtRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new RequestParamException("Jwt 바인딩 에러가 발생하였습니다.", "102");
        }

        Token refreshedAccessToken = authService.refreshAccessToken(jwtRequest);
        return ResponseEntity.ok(new ApiResponse(true, "토큰 생신완료", "accessToken", refreshedAccessToken));
    }

    @ApiOperation(value = "Token & RefreshToken 재발급", notes = "새로 갱신된 Token, RefreshToken을 발급합니다.")
    @PostMapping("auth/refresh/token")
    public ResponseEntity<?> refreshAccessAndRefreshTokens(@Valid @RequestBody JwtRequest jwtRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new RequestParamException("Jwt 바인딩 에러가 발생하였습니다.", "102");
        }

        AuthResponse authResponse = authService.refreshAccessAndRefreshToken(jwtRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Token & RefreshToken 갱신 완료", "tokens", authResponse));
    }

    @ApiOperation(value = "Token 유효성 검사", notes = "Token의 유효성을 검사하고 결과를 반환합니다.")
    @PostMapping("auth/validation/token")
    public ResponseEntity<?> validateAccessJwtToken(@Valid @RequestBody JwtRequest jwtRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new RequestParamException("Jwt 바인딩 에러가 발생하였습니다.", "102");
        }

        tokenProvider.validateAccessToken(jwtRequest.getJwt());
        return ResponseEntity.ok(new ApiResponse(true, "유효한 토큰 입니다."));
    }


    @ApiOperation(value = "RefreshToken 유효성 검사", notes = "RefreshToken의 유효성을 검사하고 결과를 반환합니다.")
    @PostMapping("auth/validation/refresh/token")
    public ResponseEntity<?> validateRefreshJwtToken(@Valid @RequestBody JwtRequest jwtRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new RequestParamException("Jwt 바인딩 에러가 발생하였습니다.", "102");
        }

        tokenProvider.validateRefreshToken(jwtRequest.getJwt());
        return ResponseEntity.ok(new ApiResponse(true, "유효한 리프레시 토큰 입니다."));
    }

    @ApiOperation(value = "로그인", notes = "로그인")
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse accessAndRefreshToken = authService.createAccessAndRefreshToken(userPrincipal.getId());
        Token accessToken = accessAndRefreshToken.getAccessToken();
        Token refreshToken = accessAndRefreshToken.getRefreshToken();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}
