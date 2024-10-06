package com.learnhive.lessonservice.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = "com.learnhive.lessonservice.client")  // MailgunClient가 있는 패키지 경로
@Configuration
public class OpenFeignConfig {

    @Value("${mailgun.key}")
    private String mailgunKey;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() { // Mailgun API 요청에 인증 정보를 추가하는 인터셉터
        return new BasicAuthRequestInterceptor("api", mailgunKey);
    }

}
