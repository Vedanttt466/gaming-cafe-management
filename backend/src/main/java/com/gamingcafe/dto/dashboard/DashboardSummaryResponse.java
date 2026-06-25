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
public class DashboardSummaryResponse {
    private int totalPcs;
    private int availablePcs;
    private int occupiedPcs;
    private int reservedPcs;
    private int maintenancePcs;
    private int activeSessions;
    private int pendingReservations;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private long todaySessionsCount;
    private long todayNoShowCount;
    private BigDecimal todayForfeitedTokens;
}
