package com.ksh.portfolio.service.member;


import com.ksh.portfolio.domain.member.Member;
import com.ksh.portfolio.exception.ResourceNotFoundException;
import com.ksh.portfolio.payload.request.PasswordRequest;
import com.ksh.portfolio.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean checkOldPassword(String oldPassword, String newPassword) {
        return !passwordEncoder.matches(newPassword, oldPassword);
    }

    @Transactional
    public void changePassword(Long id, PasswordRequest passwordRequest) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member", "memberId", id, "200"));
        member.memberChangePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }
}
