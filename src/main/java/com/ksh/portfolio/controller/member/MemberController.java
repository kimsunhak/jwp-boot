package com.ksh.portfolio.controller.member;


import com.ksh.portfolio.domain.member.Member;
import com.ksh.portfolio.exception.RequestParamException;
import com.ksh.portfolio.payload.request.PasswordRequest;
import com.ksh.portfolio.payload.response.ApiResponse;
import com.ksh.portfolio.payload.response.MemberInfoResponse;
import com.ksh.portfolio.repository.member.MemberRepository;
import com.ksh.portfolio.service.member.MemberService;
import com.ksh.portfolio.security.CurrentUser;
import com.ksh.portfolio.security.UserPrincipal;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    
}
