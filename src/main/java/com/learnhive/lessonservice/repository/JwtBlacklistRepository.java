package com.learnhive.lessonservice.repository;

import com.learnhive.lessonservice.domain.user.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
    Optional<JwtBlacklist> findTopByUsernameOrderByCreatedAtDesc(String username);
}
