package com.ksh.jwpboot.security.oauth2;

import com.ksh.jwpboot.config.AppProperties;
import com.ksh.jwpboot.domain.member.AuthProvider;
import com.ksh.jwpboot.domain.member.Member;
import com.ksh.jwpboot.domain.member.Role;
import com.ksh.jwpboot.exception.OAuth2AuthenticationProcessingException;
import com.ksh.jwpboot.repository.member.MemberRepository;
import com.ksh.jwpboot.security.UserPrincipal;
import com.ksh.jwpboot.security.oauth2.user.OAuth2UserInfo;
import com.ksh.jwpboot.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final AppProperties appProperties;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());

        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 Provider에 이메일이 없습니다.");
        }

        Optional<Member> memberOptional = memberRepository.findByEmail(oAuth2UserInfo.getEmail());

        Member member;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
            if (!member.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("이미 등록된 회원입니다.");
            }

            member = updateExistingMember(member, oAuth2UserInfo);
        } else {
            member = registerNewMember(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(member, oAuth2User.getAttributes());
    }

    private Member registerNewMember(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        AuthProvider authProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId());

        Member member = Member.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .provider(authProvider)
                .providerId(oAuth2UserInfo.getId())
                .imageUrl(oAuth2UserInfo.getImageUrl())
                .role(Role.ROLE_MEMBER)
                .build();

        return memberRepository.save(member);
    }

    private Member updateExistingMember(Member member, OAuth2UserInfo oAuth2UserInfo) {
        member.updateExistingMember(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());
        return memberRepository.save(member);
    }

}
