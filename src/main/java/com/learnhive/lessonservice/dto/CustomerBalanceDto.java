package com.learnhive.lessonservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerBalanceDto {
    private String initiator;
    private String message;
    private Integer requestedAmount;
}
