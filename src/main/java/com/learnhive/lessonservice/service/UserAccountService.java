package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.UserAccountDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.jwt.JwtTokenManager;
import com.learnhive.lessonservice.repository.UserAccountRepository;
import com.learnhive.lessonservice.security.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenManager jwtUtil;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserEmailVerificationService userEmailVerificationService;

    @Transactional
    public void signUp(UserAccountDto userAccountDto) {
        // 이메일 중복 확인
        if (userAccountRepository.existsByEmail(userAccountDto.email())) {
            throw new CustomException(ExceptionCode.EMAIL_ALREADY_EXISTS);
        }

        // 사용자명 중복 확인
        if (userAccountRepository.existsByEmail(userAccountDto.username())) {
            throw new CustomException(ExceptionCode.USERNAME_ALREADY_EXISTS);
        }

        // User 객체 생성 및 저장
         UserAccount newUserAccount = UserAccount.builder()
                 .username(userAccountDto.username())
                 .userPassword(userAccountDto.userPassword())
                 .userRole(userAccountDto.userRole())
                 .email(userAccountDto.email())
                 .build();

        // 이메일 인증 요청 전
        userEmailVerificationService.sendEmailVerificationRequest(newUserAccount);
    }

    @Transactional
    public String signIn(String username, String password) {
        // 사용자 인증 처리 및 정보 로드
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 로그인 처리 후 정보 업데이트
        userAccountRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 인증된 사용자 정보를 사용해 토큰 생성 및 반환
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    @Transactional
    public void signOut(Long userId) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 삭제 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }
    }

    @Transactional
    public void updateUser(Long userId, UserAccountDto updateForm) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 정보 변경 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }

        // 이름, 비밀번호, 이메일 회원 정보 수정
        if (userAccountRepository.existsByUsername(updateForm.username())) {
            throw new CustomException(ExceptionCode.USERNAME_ALREADY_EXISTS);
        }
        authenticatedUser.changeUserName(updateForm.username());

        if (userAccountRepository.existsByEmail(updateForm.email())) {
            throw new CustomException(ExceptionCode.EMAIL_ALREADY_EXISTS);
        }
        authenticatedUser.changeEmail(updateForm.email());

        authenticatedUser.changePassword(updateForm.userPassword());
    }

    @Transactional
    public void deleteUser(Long userId) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 삭제 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }

        // 소프트 삭제 처리
        authenticatedUser.softDeleteAccount();
    }

    public String findUsernameByEmail(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        if (!userAccount.isEmailVerified()) {
            throw new CustomException(ExceptionCode.USER_NOT_FOUND);
        }

        if (userAccount.isDeleted()) {
            throw new CustomException(ExceptionCode.USER_DELETED);
        }

        return userAccount.getUsername();
    }
}
