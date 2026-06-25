package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.dto.dashboard.CustomerHistoryResponse;
import com.gamingcafe.dto.dashboard.PeakHourPoint;
import com.gamingcafe.dto.dashboard.RevenueTrendPoint;
import com.gamingcafe.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class AnalyticsController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue-trend")
    public ResponseEntity<ApiResponse<List<RevenueTrendPoint>>> revenueTrend(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRevenueTrend(days)));
    }

    @GetMapping("/peak-hours")
    public ResponseEntity<ApiResponse<List<PeakHourPoint>>> peakHours() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPeakHours()));
    }

    @GetMapping("/customer-history")
    public ResponseEntity<ApiResponse<List<CustomerHistoryResponse>>> customerHistory() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCustomerHistories()));
    }
}
