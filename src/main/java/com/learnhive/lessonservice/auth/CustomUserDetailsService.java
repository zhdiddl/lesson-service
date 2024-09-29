package com.learnhive.lessonservice.auth;

import com.learnhive.lessonservice.domain.UserAccount;
import com.learnhive.lessonservice.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("가입한 사용자명이 없습니다: " + username));

        // 계정이 삭제되었는지 확인 (soft delete 처리된 계정 차단)
        if (userAccount.isDeleted()) {
            throw new UsernameNotFoundException("해당 계정은 삭제된 상태입니다: " + username);
        }
//
//        // roles 필드를 SimpleGrantedAuthority 로 변환
//        List<GrantedAuthority> authorities = userAccount.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // ROLE_ 붙여서 권한 생성
//                .collect(Collectors.toList());

        return new User(
                userAccount.getUsername(),
                userAccount.getUserPassword(),
                Collections.emptyList()
        );
    }
}
