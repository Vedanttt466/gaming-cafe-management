# Architecture Overview

## System Context

The Gaming Cafe Management System digitizes the day-to-day operations of a 10-PC gaming cafe: walk-in sessions, online reservations, billing, payments, and owner-facing analytics. It replaces a pen-and-paper tracking process that was prone to lost revenue from unbooked no-shows and manual billing mistakes.

## High-Level Components

The system is a three-tier application:

**Client tier** — a React single-page application served as static assets, used by customers (booking and history), staff (walk-ins, check-ins, billing), and owners (dashboards, reports, staff management).

**Application tier** — a Spring Boot REST API that owns all business logic: PC auto-assignment, the billing engine, reservation lifecycle and auto-expiry, payment verification, and audit logging. It also runs a STOMP-over-WebSocket endpoint so PC status changes propagate to every connected dashboard in real time, and a scheduled job that sweeps expired reservations every 60 seconds.

**Data tier** — a single MySQL database holding users, PCs, bookings, sessions, payments, and an audit trail.

## Component Diagram

```
┌─────────────────┐        HTTPS / REST         ┌──────────────────────┐
│   React SPA      │ ───────────────────────────▶│   Spring Boot API     │
│ (customer/staff/  │◀─────────────────────────── │  (Security, JWT,      │
│  owner views)     │      WebSocket (STOMP)       │   Billing, Scheduler) │
└─────────────────┘ ◀───────────────────────────  └──────────┬───────────┘
                                                                │ JDBC
                                                     ┌──────────▼───────────┐
                                                     │      MySQL 8          │
                                                     └────────────────────────┘
                                                                │
                                                     ┌──────────▼───────────┐
                                                     │   Razorpay (payments) │
                                                     └────────────────────────┘
```

## Why this shape

Real-time PC status is core to the product: staff and customers both need to see, within seconds, when a PC frees up. A WebSocket broadcast channel was chosen over polling so that this works without hammering the API; a 30-second polling fallback is still used on dashboards as a safety net for restrictive networks.

The reservation auto-expiry problem (the original "PCs blocked because staff forgot to release them" pain point) is solved with a server-side scheduled job rather than relying on staff action — this is the single most important reliability guarantee in the system, since it directly protects revenue.

Billing logic is centralized in one `BillingService` class so that the happy-hour / standard / mixed pricing rules have exactly one implementation, used identically whether a session was a walk-in or a checked-in reservation.

## Key Cross-Cutting Concerns

Authentication is stateless JWT (1-hour access tokens), validated on every request via a servlet filter; there are no server-side sessions, which keeps the API horizontally scalable. Authorization is role-based (CUSTOMER / STAFF / OWNER) enforced both at the URL level (`SecurityConfig`) and at the method level (`@PreAuthorize`) for defense in depth.

All state-changing actions (bookings, check-ins, cancellations, payments, staff changes) are written to an `audit_logs` table, giving the owner a tamper-evident trail for dispute resolution.
