package com.ksh.jwpboot.service.member;


import com.ksh.jwpboot.domain.member.AuthProvider;
import com.ksh.jwpboot.domain.member.Member;
import com.ksh.jwpboot.domain.member.Role;
import com.ksh.jwpboot.exception.BadRequestException;
import com.ksh.jwpboot.exception.NotSupportedException;
import com.ksh.jwpboot.exception.ResourceNotFoundException;
import com.ksh.jwpboot.payload.request.PasswordRequest;
import com.ksh.jwpboot.repository.member.MemberRepository;
import com.ksh.jwpboot.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final S3Uploader s3Uploader;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.s3.profile.dir}")
    private String profileDir;

    @Value("${app.s3.profile.defaultImage}")
    private String userDefaultImage;

    @Transactional
    public void memberJoin(String email, String name, String password, MultipartFile multipartFile) throws IOException {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("이미 존재하는 이메일입니다.");
        }

        String imageUrI;

        if (multipartFile != null) {
            imageUrI = s3Uploader.uploadImageFile(multipartFile);
        } else {
            imageUrI = userDefaultImage;
        }

        Member member = Member.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .imageUrl(imageUrI)
                .provider(AuthProvider.local)
                .providerId("local")
                .role(Role.ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public void updateMemberInfo(Long id, MultipartFile imageFile, String name) throws IOException {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member", "id", id, "200"));

        String imageUrl;
        if (imageFile != null) {
            imageUrl = uploadImageFile(imageFile);
        } else {
            imageUrl = member.getImageUrl();
        }
        member.updateExistingMember(name, imageUrl);
    }

    @Transactional
    public boolean checkOldPassword(String oldPassword, String newPassword) {
        return !passwordEncoder.matches(newPassword, oldPassword);
    }

    @Transactional
    public void changePassword(Long id, PasswordRequest passwordRequest) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member", "memberId", id, "200"));
        member.memberChangePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }

    @Transactional
    public boolean checkEmail(String email) {
        return !memberRepository.findByEmail(email).isPresent();
    }

    public String uploadImageFile(MultipartFile imageFile) throws IOException {
        String imageUrl;
        String fileName = imageFile.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        List<String> supportedExtension = Arrays.asList(".jpg", ".jpeg", ".png");

        if (!supportedExtension.contains(extension)) {
            throw new NotSupportedException(extension + "은 지원하지 않는 확장자 입니다.");
        }

        imageUrl = s3Uploader.upload(imageFile, profileDir);
        return s3Uploader.changeImageUrl(imageUrl, "?q=82&s=80x80&t=crop");
    }
}
