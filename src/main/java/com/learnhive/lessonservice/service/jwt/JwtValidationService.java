package com.learnhive.lessonservice.service.jwt;

import com.learnhive.lessonservice.jwt.TokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtValidationService {

    private final TokenProperties tokenProperties;

    public String extractToken(HttpServletRequest request) {
        String requestToken = request.getParameter("requestToken");
        String bearerToken = request.getHeader("Authorization");
        String cookieToken = extractTokenFromCookies(request);

        if (requestToken != null) {
            return requestToken;
        } else if (cookieToken != null) {
            return cookieToken;
        } else if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(tokenProperties.getCookieName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(tokenProperties.getCookieName(), null); // 토큰을 지운 쿠키 반환
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS일 경우 true로 설정
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }
}
