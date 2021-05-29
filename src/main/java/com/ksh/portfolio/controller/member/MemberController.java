package com.ksh.portfolio.controller.member;


import com.ksh.portfolio.domain.member.Member;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    
    private final MemberService memberService;

    @ApiOperation(value = "사용자 정보 반환")
    @GetMapping("/member/me")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<?> memberInfo(@CurrentUser UserPrincipal userPrincipal) {

        Member member = userPrincipal.getMember();

        MemberInfoResponse memberInfoResponse = new MemberInfoResponse(member.getId(), member.getName(), member.getEmail(), member.getImageUrl());

        return ResponseEntity.ok(new ApiResponse(true, "사용자 정보 반환", "memberInfo", memberInfoResponse));
    }
    
}
