package com.gamingcafe.repository;

import com.gamingcafe.entity.Payment;
import com.gamingcafe.entity.PaymentStatus;
import com.gamingcafe.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String orderId);
    List<Payment> findByBookingIdAndType(Long bookingId, PaymentType type);
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Payment> findByStatus(PaymentStatus status);
}
