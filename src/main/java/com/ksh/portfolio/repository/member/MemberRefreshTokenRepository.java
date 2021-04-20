package com.ksh.portfolio.repository.member;

import com.ksh.portfolio.domain.member.MemberRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {
    Optional<MemberRefreshToken> findByMemberId(Long memberId);

    boolean existsByMemberIdAndRefreshToken(Long memberId, String refreshToken);

}
