package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.auth.JwtTokenManager;
import com.learnhive.lessonservice.domain.JwtBlacklist;
import com.learnhive.lessonservice.repository.JwtBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JwtBlacklistService {

    private final JwtTokenManager jwtUtil;
    private final JwtBlacklistRepository jwtBlacklistRepository;

    // 특정 토큰을 블랙리스트에 추가하는 메소드
    public void blacklistToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        Instant expirationTime = jwtUtil.getExpirationDateFromToken(token);

        jwtBlacklistRepository.save(JwtBlacklist.builder()
                .token(token)
                .username(username)
                .expirationTime(expirationTime.atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build());
    }

    // 현재 토큰을 사용할 수 있는지 검사하는 메소드
    public boolean isTokenBlacklisted(String currentToken) {
        // 해당 유저의 확인할 블랙리스트가 있는지 먼저 체크
        String username = jwtUtil.getUsernameFromToken(currentToken);
        Optional<JwtBlacklist> topBlacklistOptional = jwtBlacklistRepository.findTopByUsernameOrderByCreatedAtDesc(username);
        if (topBlacklistOptional.isEmpty()) {
            return false;
        }

        // 블랙리스트에 해당 유저가 사용한 토큰이 있는 경우
        LocalDateTime currentTokenExpiration = jwtUtil.getExpirationDateFromToken(currentToken)
                .atZone(ZoneId.systemDefault()).toLocalDateTime(); // 현재 토큰의 만료 시간 추출
        LocalDateTime topBlacklistTokenCreatedAt = topBlacklistOptional.get().getCreatedAt(); // 블랙리스트 토큰이 생성된 시간 추출

        // 현재 토큰 생성 시점이 가장 마지막 블랙리스트 토큰 생성 시점보다 먼저면 무효화
        return currentTokenExpiration.minusHours(1).isBefore(topBlacklistTokenCreatedAt);
    }
}
