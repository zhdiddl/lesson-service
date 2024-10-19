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
    public CustomerBalanceDto adjustCustomerBalance(Long customerId, CustomerBalanceDto form) {
        // 현재 잔액 조회
        CustomerBalance currentBalance = retrieveLatestBalance(customerId);

        // 잔액이 충분한지 체크
        checkBalanceAvailability(currentBalance, form.requestedAmount());

        // 새로운 잔액으로 업데이트
        CustomerBalance updatedBalance = deductRequestedAmountFromBalance(currentBalance, form);
        customerBalanceRepository.save(updatedBalance);

        return updatedBalance.toDto();
    }

    private CustomerBalance deductRequestedAmountFromBalance(CustomerBalance requestedBalance, CustomerBalanceDto form) {
        return CustomerBalance.builder()
                .customer(requestedBalance.getCustomer())
                .requestedAmount(form.requestedAmount())
                .currentBalance(requestedBalance.getCurrentBalance() - form.requestedAmount())
                .initiator(form.initiator())
                .balanceChangeReason("요청 금액 차감")
                .build();
    }

    public Integer getCurrentBalance(Long customerId) {
        // 고객의 최신 잔액 이력을 가져와 현재 잔액만 반환
        CustomerBalance latestBalance = retrieveLatestBalance(customerId);
        return latestBalance.getCurrentBalance();
    }

    private void checkBalanceAvailability(CustomerBalance latestBalanceHistory, Integer requestedAmount) {
        // 잔액이 부족해서 거래가 수행되지 않은 경우, 해당 내역을 기록
        if (latestBalanceHistory.getCurrentBalance() + requestedAmount < 0) {
            CustomerBalance insufficientBalanceHistory = CustomerBalance
                    .builder()
                    .customer(latestBalanceHistory.getCustomer())
                    .requestedAmount(requestedAmount)
                    .currentBalance(latestBalanceHistory.getCurrentBalance())
                    .initiator("System")
                    .balanceChangeReason("잔액 부족으로 시스템에서 거래 취소")
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
        // 예치금 사용을 요청한 사용자 정보 조회
        UserAccount customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));

        // 사용자의 예치금을 0원으로 초기 설정
        return CustomerBalance.builder()
                .customer(customer)
                .requestedAmount(0)
                .currentBalance(0)
                .initiator("System")
                .balanceChangeReason("예치금 최초 사용 시 금액을 0원으로 설정")
                .build();
    }
}
