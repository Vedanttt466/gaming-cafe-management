package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.dto.payment.CashPaymentRequest;
import com.gamingcafe.dto.payment.CreateOrderResponse;
import com.gamingcafe.dto.payment.PaymentResponse;
import com.gamingcafe.dto.payment.VerifyPaymentRequest;
import com.gamingcafe.entity.Payment;
import com.gamingcafe.entity.Session;
import com.gamingcafe.entity.User;
import com.gamingcafe.service.PaymentService;
import com.gamingcafe.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SessionService sessionService;
    private final CurrentUser currentUser;

    @PostMapping("/booking/{bookingId}/create-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @PathVariable Long bookingId, Authentication auth) {
        User customer = currentUser.resolve(auth);
        CreateOrderResponse response = paymentService.createBookingTokenOrder(bookingId, customer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> verify(
            @Valid @RequestBody VerifyPaymentRequest request, Authentication auth) {
        User customer = currentUser.resolve(auth);
        paymentService.verifyBookingTokenPayment(request, customer);
        return ResponseEntity.ok(ApiResponse.success("Payment verified. Your PC is reserved!", "OK"));
    }

    @PostMapping("/cash")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordCash(
            @Valid @RequestBody CashPaymentRequest request, Authentication auth) {
        User staff = currentUser.resolve(auth);
        Session session = sessionService.getById(request.getSessionId());
        Payment payment = paymentService.recordCashPayment(session, staff);
        return ResponseEntity.ok(ApiResponse.success("Cash payment recorded", PaymentResponse.fromEntity(payment)));
    }
}
