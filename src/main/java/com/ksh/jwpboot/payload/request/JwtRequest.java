package com.ksh.jwpboot.payload.request;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtRequest {
    @ApiParam(value = "jwt 토큰", required = true)
    @NotBlank(message = "jwt 토큰이 비어있습니다.")
    private String jwt;
}
