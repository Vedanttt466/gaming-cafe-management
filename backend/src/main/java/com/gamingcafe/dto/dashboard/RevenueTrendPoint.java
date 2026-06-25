package com.gamingcafe.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendPoint {
    private String label;       // e.g. date or hour label
    private BigDecimal revenue;
    private long sessionCount;
}
