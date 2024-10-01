package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.auth.JwtTokenManager;
import com.learnhive.lessonservice.auth.TokenProperties;
import com.learnhive.lessonservice.domain.UserAccount;
import com.learnhive.lessonservice.dto.UserAccountDto;
import com.learnhive.lessonservice.service.JwtBlacklistService;
import com.learnhive.lessonservice.service.JwtValidationService;
import com.learnhive.lessonservice.service.UserAccountService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/users")
public class UserAccountController {

    private final UserAccountService userService;
    private final JwtTokenManager jwtUtil;
    private final JwtValidationService jwtValidationService;
    private final JwtBlacklistService jwtBlacklistService;
    private final TokenProperties tokenProperties;

    @PostMapping("/signUp")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserAccountDto userForm) {
        UserAccount user = userService.signUp(userForm);
        return ResponseEntity.ok("회원가입이 완료되었습니다. 사용자명: " + user.getUsername());
    }

    @PostMapping("/signIn")
    public ResponseEntity<String> signIn(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam(required = false) boolean useCookie,
                                         HttpServletResponse response) throws AuthenticationException {
        try {
            // 로그인 처리 및 토큰 발급
            String token = userService.signIn(username, password);

            if (useCookie) {
                // 쿠키 사용시 토큰을 쿠키에 저장해서 반환
                Cookie cookie = new Cookie(tokenProperties.getCookieName(), token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60);
                response.addCookie(cookie);

                return ResponseEntity.ok("Token stored in Cookie: " + token);
            } else {
                // 헤더에 토큰을 저장
                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + token)
                        .body("Token returned in Authorization header: " + token);
            }

        } catch (AuthenticationException e) {
            if (e instanceof UsernameNotFoundException) {
                log.warn(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 계정이 존재하지 않습니다. 입력하신 내용을 다시 확인해주세요.");
            }
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다. 다시 시도해주세요.");
        }
    }

    // 현재 사용 중인 토큰만 무효화하면서 로그아웃
    @PostMapping("/signOut") // 로그인한 사람만 접근 가능
    public ResponseEntity<String> signOut(@RequestParam Long userId, HttpServletResponse response) {
        try {
            userService.signOut(userId);
            jwtValidationService.clearTokenCookie(response);

            return ResponseEntity.ok("현재 기기에서 로그아웃되었습니다.");

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유저 로그아웃 중 오류가 발생했습니다.");
        }
    }

    // 특정 토큰을 기준으로 다른 토큰들을 무효화하는 방식으로 모든 기기 로그아웃
    @PostMapping("/signOut-all")
    public ResponseEntity<String> signOutAll(@Parameter(description = "직접 토큰 입력 가능") @RequestParam(required = false) String requestToken,
                                             HttpServletRequest request, HttpServletResponse response) {

        String token = requestToken != null ? requestToken : jwtValidationService.extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            jwtBlacklistService.blacklistToken(token); // 토큰을 블랙리스트에 추가
            jwtValidationService.clearTokenCookie(response); // 쿠키에서 토큰 삭제
            return ResponseEntity.ok("모든 기기에서 로그아웃되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용한 토큰이 유효하지 않습니다.");
        }
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsernameByEmail(@Parameter(description = "조회를 원하는 유저 이메일", required = true)
                                                     @RequestParam String email) {
        String usernameByEmail = userService.findUsernameByEmail(email);
        return ResponseEntity.ok("조회 결과: " + usernameByEmail);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@Parameter(description = "정보를 변경할 유저 아이디", required = true)
                                             @PathVariable Long userId,
                                             @Valid @RequestBody UserAccountDto updateForm) {
        userService.updateUser(userId, updateForm);
        return ResponseEntity.ok("사용자 정보 업데이트가 완료되었습니다.");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@Parameter(description = "삭제할 유저 아이디", required = true)
                                             @PathVariable Long userId,
                                             HttpServletResponse response) {
        try {
        userService.deleteUser(userId);
        jwtValidationService.clearTokenCookie(response);

        return ResponseEntity.ok("아이디를 성공적으로 삭제했습니다.");

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("유저 삭제 중 오류가 발생했습니다.");
        }
    }
}
