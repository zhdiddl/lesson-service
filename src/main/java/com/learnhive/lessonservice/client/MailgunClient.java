package com.learnhive.lessonservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3")
public interface MailgunClient {

    @PostMapping("sandbox9519a866647246b287381e702ec5adbf.mailgun.org/messages")
    ResponseEntity<String> sendEmail(@RequestBody MailgunForm form);

}
