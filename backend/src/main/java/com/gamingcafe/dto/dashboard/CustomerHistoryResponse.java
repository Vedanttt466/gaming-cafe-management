package com.gamingcafe.dto.dashboard;

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
public class CustomerHistoryResponse {
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private long totalVisits;
    private long noShowCount;
    private BigDecimal totalSpend;
    private LocalDateTime lastVisit;
}
