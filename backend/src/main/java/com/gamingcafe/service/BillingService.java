package com.gamingcafe.service;

import com.gamingcafe.entity.BillingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Core pricing/billing engine for the gaming cafe.
 *
 * Rules:
 *  - Happy Hour (08:00 - 12:00): flat package price (default Rs.120) regardless of duration,
 *    as long as the session starts within the happy-hour window.
 *  - Standard pricing (after 12:00): Rs.50/hour, billed in 30-minute blocks, rounding UP
 *    any partial block (e.g. 20 min -> 1 block -> Rs.25, 45 min -> 2 blocks -> Rs.50,
 *    70 min -> 3 blocks -> Rs.75).
 *  - Mixed: a session that starts during happy hour and continues past 12:00 is charged
 *    the flat happy-hour package for the portion up to 12:00, PLUS standard block-billing
 *    for the remainder after 12:00.
 */
@Service
public class BillingService {

    @Value("${cafe.happy-hour.start}")
    private String happyHourStartStr;

    @Value("${cafe.happy-hour.end}")
    private String happyHourEndStr;

    @Value("${cafe.happy-hour.package-price}")
    private BigDecimal happyHourPackagePrice;

    @Value("${cafe.standard-rate-per-hour}")
    private BigDecimal standardRatePerHour;

    @Value("${cafe.billing-block-minutes}")
    private int blockMinutes;

    @Getter
    @AllArgsConstructor
    public static class BillingResult {
        private final BillingType billingType;
        private final int durationMinutes;
        private final BigDecimal grossAmount;
    }

    public BillingResult calculate(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Session end time cannot be before start time");
        }

        LocalTime happyStart = LocalTime.parse(happyHourStartStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime happyEnd = LocalTime.parse(happyHourEndStr, DateTimeFormatter.ofPattern("HH:mm"));

        LocalDate sessionDate = startTime.toLocalDate();
        LocalDateTime happyHourBoundary = LocalDateTime.of(sessionDate, happyEnd);

        int totalMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        if (totalMinutes <= 0) {
            totalMinutes = 1; // minimum billable duration safeguard
        }

        boolean startsInHappyHour = !startTime.toLocalTime().isBefore(happyStart)
                && startTime.toLocalTime().isBefore(happyEnd);

        if (!startsInHappyHour) {
            // Pure standard billing
            BigDecimal amount = calculateStandardAmount(totalMinutes);
            return new BillingResult(BillingType.STANDARD, totalMinutes, amount);
        }

        if (!endTime.isAfter(happyHourBoundary)) {
            // Entire session within happy hour -> flat package price
            return new BillingResult(BillingType.HAPPY_HOUR, totalMinutes, happyHourPackagePrice);
        }

        // Mixed: happy hour flat package + standard billing for the remainder
        int standardMinutes = (int) java.time.Duration.between(happyHourBoundary, endTime).toMinutes();
        BigDecimal standardAmount = calculateStandardAmount(standardMinutes);
        BigDecimal total = happyHourPackagePrice.add(standardAmount);
        return new BillingResult(BillingType.MIXED, totalMinutes, total);
    }

    /** Rs.50/hour billed in 30-minute blocks, rounding UP any partial block. */
    private BigDecimal calculateStandardAmount(int minutes) {
        if (minutes <= 0) return BigDecimal.ZERO;
        int blocks = (int) Math.ceil((double) minutes / blockMinutes);
        BigDecimal pricePerBlock = standardRatePerHour
                .multiply(BigDecimal.valueOf(blockMinutes))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return pricePerBlock.multiply(BigDecimal.valueOf(blocks)).setScale(2, RoundingMode.HALF_UP);
    }
}
