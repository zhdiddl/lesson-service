package com.learnhive.lessonservice.controller;

import com.learnhive.lessonservice.dto.CustomerBalanceDto;
import com.learnhive.lessonservice.service.customer.CustomerBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customers/{customerId}/balance")
public class CustomerBalanceController {

    private final CustomerBalanceService customerBalanceService;

    // 고객 예치금 조정
    @PostMapping("/adjust")
    public ResponseEntity<CustomerBalanceDto> adjustCustomerBalance(
            @PathVariable Long customerId,
            @RequestBody CustomerBalanceDto balanceDto
    ) {
        return ResponseEntity.ok(customerBalanceService.adjustCustomerBalance(customerId, balanceDto));
    }

    // 고객의 최신 잔액 기록 조회
    @GetMapping("/latest")
    public ResponseEntity<Integer> retrieveLatestBalance(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerBalanceService.getCurrentBalance(customerId));
    }
}