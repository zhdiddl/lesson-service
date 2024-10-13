package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.user.CustomerBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerBalanceRepository extends JpaRepository<CustomerBalance, Long> {
    Optional<CustomerBalance> findByCustomerId(Long customerId);

    // customerId를 매개변수로 받아서 해당 고객의 잔액 변동 내역 중 최신 내역 하나를 반환
    Optional<CustomerBalance> findFirstByCustomer_IdOrderByIdDesc(Long customerId);
}
