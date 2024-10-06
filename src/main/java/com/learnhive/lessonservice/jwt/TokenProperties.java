package com.learnhive.lessonservice.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TokenProperties {

    @Value("${token.cookie.name}")
    private String cookieName;
}
