-- ============================================================
-- Gaming Cafe Management System - MySQL Schema
-- ============================================================
CREATE DATABASE IF NOT EXISTS gaming_cafe_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gaming_cafe_db;

-- ------------------------------------------------------------
-- USERS  (Customer / Staff / Owner)
-- ------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)        NOT NULL,
    email           VARCHAR(150)        NOT NULL UNIQUE,
    phone           VARCHAR(15)         NOT NULL UNIQUE,
    password        VARCHAR(255)        NOT NULL,
    role            ENUM('CUSTOMER','STAFF','OWNER') NOT NULL DEFAULT 'CUSTOMER',
    enabled         BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- PCs  (10 identical gaming PCs)
-- ------------------------------------------------------------
CREATE TABLE pcs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pc_number       INT                 NOT NULL UNIQUE,
    specifications  VARCHAR(255)        NOT NULL DEFAULT 'i5 12th Gen / RTX 3060 / 16GB RAM',
    status          ENUM('AVAILABLE','RESERVED','OCCUPIED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    created_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pcs_status (status)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- BOOKINGS  (Online reservations)
-- ------------------------------------------------------------
CREATE TABLE bookings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id         BIGINT          NOT NULL,
    pc_id               BIGINT          NULL,
    booking_time        DATETIME        NOT NULL,           -- requested arrival time
    expires_at          DATETIME        NOT NULL,           -- booking_time + 30 minutes
    checked_in_at       DATETIME        NULL,
    token_amount        DECIMAL(10,2)   NOT NULL DEFAULT 50.00,
    token_paid          BOOLEAN         NOT NULL DEFAULT FALSE,
    token_forfeited     BOOLEAN         NOT NULL DEFAULT FALSE,
    status              ENUM('PENDING_PAYMENT','CONFIRMED','CHECKED_IN','EXPIRED','CANCELLED','NO_SHOW','COMPLETED')
                                         NOT NULL DEFAULT 'PENDING_PAYMENT',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_booking_pc FOREIGN KEY (pc_id) REFERENCES pcs(id),
    INDEX idx_booking_status (status),
    INDEX idx_booking_time (booking_time),
    INDEX idx_booking_customer (customer_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- SESSIONS  (Actual PC usage - walk-in or from a checked-in booking)
-- ------------------------------------------------------------
CREATE TABLE sessions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id          BIGINT          NULL,               -- null for walk-in
    pc_id               BIGINT          NOT NULL,
    customer_id         BIGINT          NULL,                -- null for anonymous walk-in
    walk_in_name        VARCHAR(100)    NULL,                -- used when customer_id is null
    walk_in_phone       VARCHAR(15)     NULL,
    started_by          BIGINT          NOT NULL,            -- staff user id who started it
    ended_by            BIGINT          NULL,
    start_time          DATETIME        NOT NULL,
    end_time            DATETIME        NULL,
    billing_type        ENUM('HAPPY_HOUR','STANDARD','MIXED') NULL,
    duration_minutes    INT             NULL,
    gross_amount        DECIMAL(10,2)   NULL,
    token_adjusted       DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    net_amount_due       DECIMAL(10,2)  NULL,
    status              ENUM('ACTIVE','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_session_pc FOREIGN KEY (pc_id) REFERENCES pcs(id),
    CONSTRAINT fk_session_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_session_started_by FOREIGN KEY (started_by) REFERENCES users(id),
    CONSTRAINT fk_session_ended_by FOREIGN KEY (ended_by) REFERENCES users(id),
    INDEX idx_session_status (status),
    INDEX idx_session_pc (pc_id),
    INDEX idx_session_customer (customer_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- PAYMENTS  (Token payments + final bill payments)
-- ------------------------------------------------------------
CREATE TABLE payments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    booking_id          BIGINT          NULL,
    session_id          BIGINT          NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    type                ENUM('BOOKING_TOKEN','FINAL_BILL') NOT NULL,
    method              ENUM('CASH','RAZORPAY') NOT NULL DEFAULT 'RAZORPAY',
    status              ENUM('CREATED','SUCCESS','FAILED','REFUNDED','FORFEITED') NOT NULL DEFAULT 'CREATED',
    razorpay_order_id   VARCHAR(100)    NULL,
    razorpay_payment_id VARCHAR(100)    NULL,
    razorpay_signature  VARCHAR(255)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payment_session FOREIGN KEY (session_id) REFERENCES sessions(id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_user (user_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- AUDIT LOGS
-- ------------------------------------------------------------
CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NULL,
    action          VARCHAR(100)    NOT NULL,
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       BIGINT          NULL,
    details         TEXT            NULL,
    ip_address      VARCHAR(45)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Seed: 10 identical PCs
-- ------------------------------------------------------------
INSERT INTO pcs (pc_number, specifications, status) VALUES
(1,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(2,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(3,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(4,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(5,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(6,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(7,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(8,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(9,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE'),
(10,'i5 12th Gen / RTX 3060 / 16GB RAM','AVAILABLE');
