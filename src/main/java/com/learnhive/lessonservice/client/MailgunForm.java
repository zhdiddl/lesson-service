package com.learnhive.lessonservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MailgunForm {

    private String to;
    private String from;
    private String subject;
    private String text;

}
