package com.learnhive.lessonservice.domain.user;

import com.learnhive.lessonservice.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CustomerBalance extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(updatable = false, nullable = false) // 한 번 설정 후 변경 불가
    private UserAccount customer;

    private Integer requestedAmount;
    private Integer currentBalance;

    private String initiator;
    private String description;

}
