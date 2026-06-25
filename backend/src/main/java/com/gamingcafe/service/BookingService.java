package com.gamingcafe.service;

import com.gamingcafe.dto.booking.CreateBookingRequest;
import com.gamingcafe.entity.*;
import com.gamingcafe.exception.BadRequestException;
import com.gamingcafe.exception.ResourceNotFoundException;
import com.gamingcafe.repository.BookingRepository;
import com.gamingcafe.repository.PaymentRepository;
import com.gamingcafe.websocket.PcStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PcService pcService;
    private final PcStatusBroadcaster broadcaster;
    private final AuditLogService auditLogService;

    @Value("${cafe.operating-hours.open}")
    private String openTimeStr;

    @Value("${cafe.operating-hours.close}")
    private String closeTimeStr;

    @Value("${cafe.booking.hold-minutes}")
    private int holdMinutes;

    @Value("${cafe.booking.token-amount}")
    private java.math.BigDecimal tokenAmount;

    /**
     * Step 1 of reservation flow: validates the slot, auto-assigns any available PC,
     * and creates a booking in PENDING_PAYMENT state. The booking token must then be
     * paid (see PaymentService) to move it to CONFIRMED.
     */
    @Transactional
    public Booking createBooking(User customer, CreateBookingRequest request) {
        LocalDateTime bookingTime = request.getBookingTime();
        validateOperatingHours(bookingTime);

        LocalDateTime expiresAt = bookingTime.plusMinutes(holdMinutes);

        Pc assignedPc = pcService.assignPcForBooking(bookingTime, expiresAt);

        Booking booking = Booking.builder()
                .customer(customer)
                .pc(assignedPc)
                .bookingTime(bookingTime)
                .expiresAt(expiresAt)
                .tokenAmount(tokenAmount)
                .tokenPaid(false)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        Booking saved = bookingRepository.save(booking);
        auditLogService.log(customer.getId(), "CREATE_BOOKING", "Booking", saved.getId(),
                "PC #" + assignedPc.getPcNumber() + " reserved for " + bookingTime, null);
        return saved;
    }

    private void validateOperatingHours(LocalDateTime bookingTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime open = LocalTime.parse(openTimeStr, fmt);
        LocalTime close = LocalTime.parse(closeTimeStr, fmt);
        LocalTime requested = bookingTime.toLocalTime();

        if (requested.isBefore(open) || requested.isAfter(close)) {
            throw new BadRequestException(
                    "Bookings can only be made between " + openTimeStr + " and " + closeTimeStr);
        }
    }

    @Transactional
    public Booking confirmBookingAfterTokenPayment(Long bookingId) {
        Booking booking = getById(bookingId);
        booking.setTokenPaid(true);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        broadcaster.notifyDashboardChanged();
        return saved;
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public Page<Booking> getCustomerHistory(Long customerId, Pageable pageable) {
        return bookingRepository.findByCustomerIdOrderByBookingTimeDesc(customerId, pageable);
    }

    public List<Booking> getPendingAndConfirmed() {
        return bookingRepository.findByStatusIn(List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED));
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long requestingUserId, boolean isStaffOrOwner) {
        Booking booking = getById(bookingId);

        if (!isStaffOrOwner && !booking.getCustomer().getId().equals(requestingUserId)) {
            throw new BadRequestException("You can only cancel your own bookings");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("This booking cannot be cancelled in its current state: " + booking.getStatus());
        }

        boolean forfeit = booking.isTokenPaid();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setTokenForfeited(forfeit);
        bookingRepository.save(booking);

        if (booking.getPc() != null) {
            pcService.releasePc(booking.getPc());
        }

        auditLogService.log(requestingUserId, "CANCEL_BOOKING", "Booking", booking.getId(),
                "Token forfeited: " + forfeit, null);
        return booking;
    }

    /** Marks a booking as checked-in once the session has started. Called by SessionService. */
    @Transactional
    public void markCheckedIn(Booking booking) {
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional
    public void markCompleted(Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }
}
