package com.gamingcafe.dto.payment;

import com.gamingcafe.entity.Payment;
import com.gamingcafe.entity.PaymentMethod;
import com.gamingcafe.entity.PaymentStatus;
import com.gamingcafe.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private PaymentType type;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .type(p.getType())
                .method(p.getMethod())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
