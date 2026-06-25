package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    @Value("${cafe.happy-hour.start}")
    private String happyHourStart;

    @Value("${cafe.happy-hour.end}")
    private String happyHourEnd;

    @Value("${cafe.happy-hour.package-price}")
    private BigDecimal happyHourPrice;

    @Value("${cafe.standard-rate-per-hour}")
    private BigDecimal standardRate;

    @Value("${cafe.billing-block-minutes}")
    private int blockMinutes;

    @Value("${cafe.booking.token-amount}")
    private BigDecimal tokenAmount;

    @Value("${cafe.booking.hold-minutes}")
    private int holdMinutes;

    @Value("${cafe.operating-hours.open}")
    private String openTime;

    @Value("${cafe.operating-hours.close}")
    private String closeTime;

    @Data
    @Builder
    @AllArgsConstructor
    public static class PricingInfo {
        private String operatingHours;
        private String happyHourWindow;
        private BigDecimal happyHourPackagePrice;
        private BigDecimal standardRatePerHour;
        private int billingBlockMinutes;
        private BigDecimal bookingTokenAmount;
        private int reservationHoldMinutes;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PricingInfo>> getPricing() {
        PricingInfo info = PricingInfo.builder()
                .operatingHours(openTime + " - " + closeTime)
                .happyHourWindow(happyHourStart + " - " + happyHourEnd)
                .happyHourPackagePrice(happyHourPrice)
                .standardRatePerHour(standardRate)
                .billingBlockMinutes(blockMinutes)
                .bookingTokenAmount(tokenAmount)
                .reservationHoldMinutes(holdMinutes)
                .build();
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}
