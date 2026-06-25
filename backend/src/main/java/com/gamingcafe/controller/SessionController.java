package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.dto.session.CheckInRequest;
import com.gamingcafe.dto.session.SessionResponse;
import com.gamingcafe.dto.session.WalkInSessionRequest;
import com.gamingcafe.entity.Session;
import com.gamingcafe.entity.User;
import com.gamingcafe.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final CurrentUser currentUser;

    @PostMapping("/walk-in")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<SessionResponse>> startWalkIn(
            @Valid @RequestBody WalkInSessionRequest request, Authentication auth) {
        User staff = currentUser.resolve(auth);
        Session session = sessionService.startWalkIn(request, staff);
        return ResponseEntity.ok(ApiResponse.success(
                "Walk-in session started on PC #" + session.getPc().getPcNumber(),
                SessionResponse.fromEntity(session)));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<SessionResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request, Authentication auth) {
        User staff = currentUser.resolve(auth);
        Session session = sessionService.checkInBooking(request.getBookingId(), staff);
        return ResponseEntity.ok(ApiResponse.success("Customer checked in", SessionResponse.fromEntity(session)));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<SessionResponse>> endSession(@PathVariable Long id, Authentication auth) {
        User staff = currentUser.resolve(auth);
        Session session = sessionService.endSession(id, staff);
        return ResponseEntity.ok(ApiResponse.success("Session ended and billed", SessionResponse.fromEntity(session)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> activeSessions() {
        List<SessionResponse> response = sessionService.getActiveSessions().stream()
                .map(SessionResponse::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> myHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User customer = currentUser.resolve(auth);
        Page<SessionResponse> response = sessionService
                .getCustomerHistory(customer.getId(), PageRequest.of(page, size))
                .map(SessionResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
