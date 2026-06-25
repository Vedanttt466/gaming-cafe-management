package com.gamingcafe.dto.session;

import com.gamingcafe.entity.BillingType;
import com.gamingcafe.entity.Session;
import com.gamingcafe.entity.SessionStatus;
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
public class SessionResponse {
    private Long id;
    private Long bookingId;
    private Integer pcNumber;
    private Long customerId;
    private String customerName; // registered name or walk-in name
    private String customerPhone;
    private boolean walkIn;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BillingType billingType;
    private Integer durationMinutes;
    private BigDecimal grossAmount;
    private BigDecimal tokenAdjusted;
    private BigDecimal netAmountDue;
    private SessionStatus status;

    public static SessionResponse fromEntity(Session s) {
        boolean isWalkIn = s.getCustomer() == null;
        return SessionResponse.builder()
                .id(s.getId())
                .bookingId(s.getBooking() != null ? s.getBooking().getId() : null)
                .pcNumber(s.getPc().getPcNumber())
                .customerId(s.getCustomer() != null ? s.getCustomer().getId() : null)
                .customerName(isWalkIn ? s.getWalkInName() : s.getCustomer().getName())
                .customerPhone(isWalkIn ? s.getWalkInPhone() : s.getCustomer().getPhone())
                .walkIn(isWalkIn)
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .billingType(s.getBillingType())
                .durationMinutes(s.getDurationMinutes())
                .grossAmount(s.getGrossAmount())
                .tokenAdjusted(s.getTokenAdjusted())
                .netAmountDue(s.getNetAmountDue())
                .status(s.getStatus())
                .build();
    }
}
