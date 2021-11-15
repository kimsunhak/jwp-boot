package com.ksh.jwpboot.controller.member;


import com.ksh.jwpboot.domain.member.Member;
import com.ksh.jwpboot.exception.RequestParamException;
import com.ksh.jwpboot.payload.request.PasswordRequest;
import com.ksh.jwpboot.payload.response.ApiResponse;
import com.ksh.jwpboot.payload.response.MemberInfoResponse;
import com.ksh.jwpboot.repository.member.MemberRepository;
import com.ksh.jwpboot.service.member.MemberService;
import com.ksh.jwpboot.security.CurrentUser;
import com.ksh.jwpboot.security.UserPrincipal;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    private final MemberService memberService;

    private final PasswordEncoder passwordEncoder;

    @ApiOperation(value = "사용자 정보 반환")
    @GetMapping("/member/me")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<?> memberInfo(@CurrentUser UserPrincipal userPrincipal) {

        Member member = userPrincipal.getMember();

        MemberInfoResponse memberInfoResponse = new MemberInfoResponse(member.getId(), member.getName(), member.getEmail(), member.getImageUrl());

        return ResponseEntity.ok(new ApiResponse(true, "사용자 정보 반환", "memberInfo", memberInfoResponse));
    }

    @ApiOperation(value = "이메일 중복검사", notes = "이메일 중복검사를 실시합니다.")
    @GetMapping("/member/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.checkEmail(email));
    }

    @ApiOperation(value = "사용자 정보 수정", notes = "사용자의 정보를 수정합니다.")
    @PostMapping("/member/{id}")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<?> updateMemberInfo(@CurrentUser UserPrincipal userPrincipal,
                                              @RequestParam(required = false) MultipartFile imageFile,
                                              @RequestParam String name,
                                              @PathVariable Long id) throws IOException {
        if (!id.equals(userPrincipal.getId())) {
            throw new RequestParamException("jwt Token의 유저 아이디와 일치하지 않습니다. : " + id, "103");
        }

        memberService.updateMemberInfo(id, imageFile, name);

        return ResponseEntity.ok(new ApiResponse(true, "사용자 정보 수정 완료"));
    }

    @ApiOperation(value = "기존 비밀번호 검사", notes = "axios 실시간 이벤트")
    @GetMapping("/member/checkOldPassword")
    public ResponseEntity<?> checkPassword(@CurrentUser UserPrincipal userPrincipal,
                                           @RequestParam String newPassword) {
        return ResponseEntity.ok(memberService.checkOldPassword(userPrincipal.getPassword(), newPassword));
    }

    @ApiOperation(value = "비밀번호 변경", notes = "사용자의 비밀번호를 변경합니다. 버튼 클릭 이벤트")
    @PutMapping("/member/{id}/changePassword")
    public ResponseEntity<?> changePassword(@CurrentUser UserPrincipal userPrincipal,
                                            @PathVariable Long id,
                                            @RequestBody PasswordRequest passwordRequest) {
        if (!id.equals(userPrincipal.getId())) {
            throw new RequestParamException("jwt Token의 사용자 아이디와 일치하지 않습니다. : " + id, "103");
        }

        if (!passwordEncoder.matches(passwordRequest.getNewPassword(), userPrincipal.getPassword())) {
            memberService.changePassword(id, passwordRequest);
        } else {
            throw new RequestParamException("현재 비밀번호와 동일 할 수 없습니다.", "103");
        }

        return ResponseEntity.ok(new ApiResponse(true, "비밀번호 변경이 완료되었습니다."));
    }

    @ApiOperation(value = "회원가입", notes = "사용자를 추가합니다.")
    @PutMapping("/member/join")
    public ResponseEntity<?> join(@RequestParam @Email String email,
                                  @RequestParam @NotBlank String name,
                                  @RequestParam String password,
                                  @RequestParam (required = false) MultipartFile imageFile) throws IOException {

        memberService.memberJoin(email, name, password, imageFile);

        return ResponseEntity.ok(new ApiResponse(true, "회원가입이 완료되었습니다."));
    }
}
