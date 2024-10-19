package com.learnhive.lessonservice.dto;

import lombok.Builder;

@Builder
public record CustomerBalanceDto(
        String initiator,
        String balanceChangeReason,
        Integer requestedAmount
) {}

