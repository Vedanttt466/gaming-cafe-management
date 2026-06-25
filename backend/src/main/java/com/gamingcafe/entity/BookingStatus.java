package com.gamingcafe.entity;

public enum BookingStatus {
    PENDING_PAYMENT, // token not yet paid
    CONFIRMED,       // token paid, PC reserved, waiting for customer to arrive
    CHECKED_IN,      // customer arrived, session started
    EXPIRED,         // 30 min grace period passed without check-in
    CANCELLED,       // customer/staff cancelled before expiry
    NO_SHOW,         // explicitly marked no-show (token forfeited)
    COMPLETED        // session linked to this booking has ended
}
