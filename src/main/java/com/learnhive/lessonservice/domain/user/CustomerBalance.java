package com.learnhive.lessonservice.domain.user;

import com.learnhive.lessonservice.domain.AuditingFields;
import com.learnhive.lessonservice.dto.CustomerBalanceDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String balanceChangeReason;

    public CustomerBalanceDto toDto() {
        return CustomerBalanceDto.builder()
                .initiator(this.initiator)
                .balanceChangeReason(this.balanceChangeReason)
                .requestedAmount(this.requestedAmount)
                .build();
    }
}
