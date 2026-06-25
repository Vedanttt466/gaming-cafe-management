package com.gamingcafe.controller;

import com.gamingcafe.dto.booking.BookingResponse;
import com.gamingcafe.dto.booking.CreateBookingRequest;
import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.entity.Booking;
import com.gamingcafe.entity.User;
import com.gamingcafe.security.CustomUserDetails;
import com.gamingcafe.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUser currentUser;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @Valid @RequestBody CreateBookingRequest request, Authentication auth) {
        User customer = currentUser.resolve(auth);
        Booking booking = bookingService.createBooking(customer, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Reservation created. Pay the Rs.50 token to confirm your PC.",
                BookingResponse.fromEntity(booking)));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> myHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User customer = currentUser.resolve(auth);
        Page<BookingResponse> response = bookingService
                .getCustomerHistory(customer.getId(), PageRequest.of(page, size))
                .map(BookingResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getOne(@PathVariable Long id) {
        Booking booking = bookingService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(BookingResponse.fromEntity(booking)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<java.util.List<BookingResponse>>> activeBookings() {
        var response = bookingService.getPendingAndConfirmed().stream()
                .map(BookingResponse::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable Long id, Authentication auth) {
        User user = currentUser.resolve(auth);
        boolean isStaffOrOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_OWNER"));
        Booking booking = bookingService.cancelBooking(id, user.getId(), isStaffOrOwner);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", BookingResponse.fromEntity(booking)));
    }
}
