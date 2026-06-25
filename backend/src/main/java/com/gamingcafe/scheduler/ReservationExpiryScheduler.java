package com.gamingcafe.scheduler;

import com.gamingcafe.entity.Booking;
import com.gamingcafe.entity.BookingStatus;
import com.gamingcafe.entity.Pc;
import com.gamingcafe.repository.BookingRepository;
import com.gamingcafe.service.AuditLogService;
import com.gamingcafe.service.PaymentService;
import com.gamingcafe.service.PcService;
import com.gamingcafe.websocket.PcStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs every minute. Finds reservations whose 30-minute hold window has passed
 * without the customer checking in, automatically expires them, releases the
 * held PC back to AVAILABLE, and forfeits the booking token if it was paid.
 *
 * This directly solves the manual pen-and-paper tracking problem: PCs are
 * never left "stuck" as reserved due to staff forgetting to free them up.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final PcService pcService;
    private final PaymentService paymentService;
    private final PcStatusBroadcaster broadcaster;
    private final AuditLogService auditLogService;

    @Scheduled(fixedRate = 60_000) // every 60 seconds
    @Transactional
    public void expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> overdueConfirmed = bookingRepository
                .findByStatusAndExpiresAtBefore(BookingStatus.CONFIRMED, now);
        for (Booking booking : overdueConfirmed) {
            booking.setStatus(BookingStatus.NO_SHOW);
            booking.setTokenForfeited(true);
            bookingRepository.save(booking);

            paymentService.forfeitTokenIfPaid(booking);
            releasePcIfHeld(booking);

            auditLogService.log(null, "AUTO_EXPIRE_NO_SHOW", "Booking", booking.getId(),
                    "Token of Rs." + booking.getTokenAmount() + " forfeited for customer "
                            + booking.getCustomer().getName(), null);

            log.info("Booking #{} auto-expired as NO_SHOW. Token forfeited. PC #{} released.",
                    booking.getId(), booking.getPc() != null ? booking.getPc().getPcNumber() : "N/A");
        }

        List<Booking> overduePending = bookingRepository
                .findByStatusAndExpiresAtBefore(BookingStatus.PENDING_PAYMENT, now);
        for (Booking booking : overduePending) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            releasePcIfHeld(booking);

            auditLogService.log(null, "AUTO_EXPIRE_UNPAID", "Booking", booking.getId(),
                    "Reservation expired - token was never paid", null);
        }

        if (!overdueConfirmed.isEmpty() || !overduePending.isEmpty()) {
            broadcaster.notifyDashboardChanged();
        }
    }

    private void releasePcIfHeld(Booking booking) {
        Pc pc = booking.getPc();
        if (pc != null) {
            pcService.releasePc(pc);
        }
    }
}
