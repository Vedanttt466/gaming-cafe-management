package com.gamingcafe.dto.booking;

import com.gamingcafe.entity.Booking;
import com.gamingcafe.entity.BookingStatus;
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
public class BookingResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Integer pcNumber;
    private LocalDateTime bookingTime;
    private LocalDateTime expiresAt;
    private LocalDateTime checkedInAt;
    private BigDecimal tokenAmount;
    private boolean tokenPaid;
    private boolean tokenForfeited;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public static BookingResponse fromEntity(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomer().getId())
                .customerName(b.getCustomer().getName())
                .pcNumber(b.getPc() != null ? b.getPc().getPcNumber() : null)
                .bookingTime(b.getBookingTime())
                .expiresAt(b.getExpiresAt())
                .checkedInAt(b.getCheckedInAt())
                .tokenAmount(b.getTokenAmount())
                .tokenPaid(b.isTokenPaid())
                .tokenForfeited(b.isTokenForfeited())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
