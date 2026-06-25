package com.gamingcafe.service;

import com.gamingcafe.dto.payment.CreateOrderResponse;
import com.gamingcafe.dto.payment.VerifyPaymentRequest;
import com.gamingcafe.entity.*;
import com.gamingcafe.exception.BadRequestException;
import com.gamingcafe.exception.PaymentVerificationException;
import com.gamingcafe.exception.ResourceNotFoundException;
import com.gamingcafe.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final AuditLogService auditLogService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    /** Creates a Razorpay order for the Rs.50 booking token. */
    @Transactional
    public CreateOrderResponse createBookingTokenOrder(Long bookingId, User customer) {
        Booking booking = bookingService.getById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("This booking does not belong to you");
        }
        if (booking.isTokenPaid()) {
            throw new BadRequestException("Token has already been paid for this booking");
        }

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            int amountInPaise = booking.getTokenAmount().multiply(BigDecimal.valueOf(100)).intValue();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId());

            com.razorpay.Order order = client.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .user(customer)
                    .booking(booking)
                    .amount(booking.getTokenAmount())
                    .type(PaymentType.BOOKING_TOKEN)
                    .method(PaymentMethod.RAZORPAY)
                    .status(PaymentStatus.CREATED)
                    .razorpayOrderId(order.get("id"))
                    .build();
            Payment saved = paymentRepository.save(payment);

            return CreateOrderResponse.builder()
                    .paymentId(saved.getId())
                    .razorpayOrderId(order.get("id"))
                    .razorpayKeyId(razorpayKeyId)
                    .amount(booking.getTokenAmount())
                    .currency("INR")
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Unable to create payment order: " + e.getMessage());
        }
    }

    /** Verifies the Razorpay signature returned by the checkout widget and confirms the booking. */
    @Transactional
    public void verifyBookingTokenPayment(VerifyPaymentRequest request, User customer) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        if (!payment.getUser().getId().equals(customer.getId())) {
            throw new BadRequestException("This payment does not belong to you");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            if (!isValid) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new PaymentVerificationException("Payment signature verification failed");
            }
        } catch (PaymentVerificationException pve) {
            throw pve;
        } catch (Exception e) {
            throw new PaymentVerificationException("Unable to verify payment: " + e.getMessage());
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        if (payment.getType() == PaymentType.BOOKING_TOKEN && payment.getBooking() != null) {
            bookingService.confirmBookingAfterTokenPayment(payment.getBooking().getId());
        }

        auditLogService.log(customer.getId(), "PAYMENT_SUCCESS", "Payment", payment.getId(),
                "Rs." + payment.getAmount() + " via Razorpay", null);
    }

    /** Staff records a cash settlement for a completed session's final bill. */
    @Transactional
    public Payment recordCashPayment(Session session, User staff) {
        Payment payment = Payment.builder()
                .user(session.getCustomer() != null ? session.getCustomer() : staff)
                .session(session)
                .amount(session.getNetAmountDue())
                .type(PaymentType.FINAL_BILL)
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .build();
        Payment saved = paymentRepository.save(payment);
        auditLogService.log(staff.getId(), "CASH_PAYMENT", "Session", session.getId(),
                "Cash settlement of Rs." + session.getNetAmountDue(), null);
        return saved;
    }

    /** Marks a booking token payment as forfeited (used by the auto-expiry scheduler). */
    @Transactional
    public void forfeitTokenIfPaid(Booking booking) {
        if (!booking.isTokenPaid()) return;
        paymentRepository.findByBookingIdAndType(booking.getId(), PaymentType.BOOKING_TOKEN)
                .forEach(p -> {
                    p.setStatus(PaymentStatus.FORFEITED);
                    paymentRepository.save(p);
                });
    }
}
