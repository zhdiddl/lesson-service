package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
