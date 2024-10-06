package com.learnhive.lessonservice.service;

import com.learnhive.lessonservice.client.MailgunClient;
import com.learnhive.lessonservice.client.MailgunForm;
import com.learnhive.lessonservice.domain.UserAccount;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEmailVerificationService {

    private final MailgunClient mailgunClient;
    private final UserAccountRepository userAccountRepository;

    @Value("$({email.from}")
    private String fromEmail;

    @Value("${email.subject}")
    private String emailSubject;

    @Value("$({email.verification.url}")
    private String verificationUrl;

    @Transactional
    public void sendEmailVerificationRequest(UserAccount userAccount) {
        String verificationCode = RandomStringUtils.random(10, true, true);

        MailgunForm sendMailForm = MailgunForm.builder()
                .to(userAccount.getEmail())
                .from(fromEmail)
                .subject(emailSubject)
                .text(buildEmailVerificationMessage(userAccount.getEmail(), userAccount.getUsername(), verificationCode))
                .build();

        mailgunClient.sendEmail(sendMailForm);
        log.info("사용자에게 이메일 인증 요청 메일이 발송되었습니다.");

        userAccount.setEmailVerificationCode(verificationCode);
        userAccount.setEmailVerificationCodeExpiry(LocalDateTime.now().plusMinutes(30));
    }

    private String buildEmailVerificationMessage(String email, String userName, String verificationCode) {
        return "안녕하세요, " + userName + "님! 아래 링크를 클릭해서 이메일 인증을 완료해 주세요. \n\n" +
                verificationUrl + "?email=" + email + "&code=" + verificationCode;
    }

    @Transactional
    public void validateEmailVerificationCode(String email, String verificationCode) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        if (userAccount.getEmailVerificationCodeExpiry() != null &&
                userAccount.getEmailVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException(ExceptionCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!userAccount.getEmailVerificationCode().equals(verificationCode)) {
            throw new CustomException(ExceptionCode.EMAIL_VERIFICATION_FAILED);
        }

        userAccount.setEmailVerified(true);
    }

}
