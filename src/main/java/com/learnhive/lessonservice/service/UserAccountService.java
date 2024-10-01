package com.learnhive.lessonservice.service;


import com.learnhive.lessonservice.auth.AuthenticatedUserService;
import com.learnhive.lessonservice.auth.JwtTokenManager;
import com.learnhive.lessonservice.domain.UserAccount;
import com.learnhive.lessonservice.dto.UserAccountDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenManager jwtUtil;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public UserAccount signUp(UserAccountDto userAccountDto) {
        // 이메일 중복 확인
        if (userAccountRepository.existsByEmail(userAccountDto.getEmail())) {
            throw new CustomException(ExceptionCode.EMAIL_ALREADY_EXISTS);
        }

        // 사용자명 중복 확인
        if (userAccountRepository.existsByEmail(userAccountDto.getUsername())) {
            throw new CustomException(ExceptionCode.USERNAME_ALREADY_EXISTS);
        }

        // User 객체 생성 및 저장
        return UserAccount.builder()
                .username(userAccountDto.getUsername())
                .userPassword(passwordEncoder.encode(userAccountDto.getUserPassword()))
                .email(userAccountDto.getEmail())
                .build();
    }

    @Transactional
    public String signIn(String username, String password) {
        // 사용자 인증 처리 및 정보 로드
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 로그인 처리 후 정보 업데이트
        UserAccount userAccount = userAccountRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));
        userAccount.setLastLogin(LocalDateTime.now());
//
//        // 인증된 사용자의 roles 변환
//        Set<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        // 인증된 사용자 정보를 사용해 토큰 생성 및 반환
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    @Transactional
    public void signOut(Long userId) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 삭제 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getUserId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }
    }

    @Transactional
    public void updateUser(Long userId, UserAccountDto updateForm) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 정보 변경 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getUserId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }

        // 회원 정보 수정
        if (updateForm.getUsername() != null && authenticatedUser.getUsername().equals(updateForm.getUsername())) {
            if (userAccountRepository.existsByUsername(updateForm.getUsername())) {
                throw new CustomException(ExceptionCode.USERNAME_ALREADY_EXISTS);
            }
            authenticatedUser.setUsername(updateForm.getUsername());
        }
        if (updateForm.getEmail() != null && authenticatedUser.getEmail().equals(updateForm.getEmail())) {
            if (userAccountRepository.existsByEmail(updateForm.getEmail())) {
                throw new CustomException(ExceptionCode.EMAIL_ALREADY_EXISTS);
            }
            authenticatedUser.setEmail(updateForm.getEmail());
        }
        if (updateForm.getUserPassword() != null) {
            if (userAccountRepository.existsByUsername(updateForm.getUsername())) {
                authenticatedUser.setUserPassword(passwordEncoder.encode(updateForm.getUserPassword()));
            }
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        // 현재 인증된 사용자 가져오기
        UserAccount authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        // 현재 인증된 사용자 아이디와 삭제 요청한 계정 아이디가 같은지 확인
        if (!Objects.equals(userId, authenticatedUser.getUserId())) { // 객체 비교 사용해서 null 반환 없이 false 반환 유도
            throw new CustomException(ExceptionCode.FORBIDDEN_ACTION);
        }

        // 소프트 삭제 처리
        authenticatedUser.setDeleted(true);
    }

    public String findUsernameByEmail(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 계정이 삭제되었는지 확인 (soft delete 처리된 계정 차단)
        if (userAccount.isDeleted()) {
            return "계정이 존재하지 않습니다.";
        }
        return userAccount.getUsername();
    }
}
