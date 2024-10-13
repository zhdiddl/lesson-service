package com.learnhive.lessonservice.service.customer;

import com.learnhive.lessonservice.domain.user.CustomerBalance;
import com.learnhive.lessonservice.domain.user.UserAccount;
import com.learnhive.lessonservice.dto.CustomerBalanceDto;
import com.learnhive.lessonservice.exception.CustomException;
import com.learnhive.lessonservice.exception.ExceptionCode;
import com.learnhive.lessonservice.repository.CustomerBalanceRepository;
import com.learnhive.lessonservice.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerBalanceService {

    private final CustomerBalanceRepository customerBalanceRepository;
    private final UserAccountRepository customerRepository;

    @Transactional(noRollbackFor = {CustomException.class}) // 잔액 변경 시도에 예외가 발생해도 기록을 모두 남김
    public void adjustCustomerBalance(Long customerId, CustomerBalanceDto form) {
        CustomerBalance currentBalance = retrieveLatestBalance(customerId);
        checkBalanceAvailability(currentBalance, form.getRequestedAmount());

        CustomerBalance updatedBalance = createBalanceRecord(currentBalance, form);
        customerBalanceRepository.save(updatedBalance);
    }


    private CustomerBalance createBalanceRecord(CustomerBalance requestedBalance, CustomerBalanceDto form) {
        return CustomerBalance.builder()
                .customer(requestedBalance.getCustomer())
                .requestedAmount(form.getRequestedAmount())
                .currentBalance(requestedBalance.getCurrentBalance() + form.getRequestedAmount())
                .initiator(form.getInitiator())
                .description("잔액 업데이트 완료")
                .build();
    }

    public Integer getCurrentBalance(Long customerId) {
        // 고객의 최신 잔액 이력을 가져와 현재 잔액만 반환
        CustomerBalance latestBalance = retrieveLatestBalance(customerId);
        return latestBalance.getCurrentBalance();
    }


    private void checkBalanceAvailability(CustomerBalance latestBalanceHistory, Integer requestedAmount) {
        if (latestBalanceHistory.getCurrentBalance() + requestedAmount < 0) {
            // 잔액이 부족해서 거래가 수행되지 않은 경우, 해당 내역을 기록
            CustomerBalance insufficientBalanceHistory = CustomerBalance
                    .builder()
                    .customer(latestBalanceHistory.getCustomer())
                    .requestedAmount(requestedAmount)
                    .currentBalance(latestBalanceHistory.getCurrentBalance())
                    .initiator("System")
                    .description("잔액 부족으로 거래 취소")
                    .build();
            customerBalanceRepository.save(insufficientBalanceHistory);
            throw new CustomException(ExceptionCode.INSUFFICIENT_BALANCE);
        }
    }

    private CustomerBalance retrieveLatestBalance(Long customerId) {
        // 기존 거래 내역이 있는지 확인
        return customerBalanceRepository.findFirstByCustomer_IdOrderByIdDesc(customerId)
                .orElseGet(() -> initializeCustomerBalance(customerId));
    }

    private CustomerBalance initializeCustomerBalance(Long customerId) {
        // 존재하는 사용자인지 확인
        UserAccount customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));
        return CustomerBalance.builder()
                .customer(customer)
                .requestedAmount(0)
                .currentBalance(0)
                .initiator("System")
                .description("initial balance")
                .build();
    }
}
