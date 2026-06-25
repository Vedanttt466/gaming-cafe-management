package com.gamingcafe.service;

import com.gamingcafe.dto.session.WalkInSessionRequest;
import com.gamingcafe.entity.*;
import com.gamingcafe.exception.BadRequestException;
import com.gamingcafe.exception.ResourceNotFoundException;
import com.gamingcafe.repository.SessionRepository;
import com.gamingcafe.websocket.PcStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PcService pcService;
    private final BookingService bookingService;
    private final BillingService billingService;
    private final PcStatusBroadcaster broadcaster;
    private final AuditLogService auditLogService;

    /** Staff creates a walk-in session: system auto-assigns any AVAILABLE PC. */
    @Transactional
    public Session startWalkIn(WalkInSessionRequest request, User staff) {
        Pc pc = pcService.assignPcForWalkIn(); // sets pc to OCCUPIED

        Session session = Session.builder()
                .pc(pc)
                .customer(null)
                .walkInName(request.getName())
                .walkInPhone(request.getPhone())
                .startedBy(staff)
                .startTime(LocalDateTime.now())
                .status(SessionStatus.ACTIVE)
                .build();

        Session saved = sessionRepository.save(session);
        broadcaster.notifySessionsChanged();
        auditLogService.log(staff.getId(), "START_WALK_IN_SESSION", "Session", saved.getId(),
                "PC #" + pc.getPcNumber() + " assigned to walk-in: " + request.getName(), null);
        return saved;
    }

    /** Staff (or the customer) checks in a CONFIRMED reservation and starts the session. */
    @Transactional
    public Session checkInBooking(Long bookingId, User staff) {
        Booking booking = bookingService.getById(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking is not in a confirmed state (current: " + booking.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(booking.getExpiresAt())) {
            throw new BadRequestException("This reservation has already expired");
        }

        pcService.markOccupied(booking.getPc());
        bookingService.markCheckedIn(booking);

        Session session = Session.builder()
                .booking(booking)
                .pc(booking.getPc())
                .customer(booking.getCustomer())
                .startedBy(staff)
                .startTime(LocalDateTime.now())
                .status(SessionStatus.ACTIVE)
                .build();

        Session saved = sessionRepository.save(session);
        broadcaster.notifySessionsChanged();
        auditLogService.log(staff.getId(), "CHECK_IN", "Session", saved.getId(),
                "Checked in booking #" + bookingId, null);
        return saved;
    }

    /** Ends an active session, computes the bill via BillingService, and frees the PC. */
    @Transactional
    public Session endSession(Long sessionId, User staff) {
        Session session = getById(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Session is already completed");
        }

        LocalDateTime endTime = LocalDateTime.now();
        BillingService.BillingResult result = billingService.calculate(session.getStartTime(), endTime);

        BigDecimal tokenAdjustment = BigDecimal.ZERO;
        if (session.getBooking() != null && session.getBooking().isTokenPaid()) {
            tokenAdjustment = session.getBooking().getTokenAmount();
        }

        BigDecimal netDue = result.getGrossAmount().subtract(tokenAdjustment);
        if (netDue.compareTo(BigDecimal.ZERO) < 0) {
            netDue = BigDecimal.ZERO; // token value exceeding the bill is absorbed, not refunded
        }

        session.setEndTime(endTime);
        session.setBillingType(result.getBillingType());
        session.setDurationMinutes(result.getDurationMinutes());
        session.setGrossAmount(result.getGrossAmount());
        session.setTokenAdjusted(tokenAdjustment);
        session.setNetAmountDue(netDue);
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedBy(staff);

        Session saved = sessionRepository.save(session);

        pcService.releasePc(session.getPc());
        if (session.getBooking() != null) {
            bookingService.markCompleted(session.getBooking());
        }

        broadcaster.notifySessionsChanged();
        broadcaster.notifyDashboardChanged();
        auditLogService.log(staff.getId(), "END_SESSION", "Session", saved.getId(),
                "Billed Rs." + result.getGrossAmount() + " (" + result.getBillingType() + "), net due Rs." + netDue, null);
        return saved;
    }

    public Session getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    public List<Session> getActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.ACTIVE);
    }

    public Page<Session> getCustomerHistory(Long customerId, Pageable pageable) {
        return sessionRepository.findByCustomerIdOrderByStartTimeDesc(customerId, pageable);
    }
}
