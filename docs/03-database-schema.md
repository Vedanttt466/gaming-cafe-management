# Database Schema

MySQL 8, InnoDB, `utf8mb4`. Full DDL is in `database/schema.sql`; seed data (10 PCs + 3 demo accounts) is in `database/seed.sql`.

## Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "makes"
    USERS ||--o{ SESSIONS : "plays (nullable for walk-ins)"
    USERS ||--o{ PAYMENTS : "pays"
    USERS ||--o{ AUDIT_LOGS : "performs"
    PCS ||--o{ BOOKINGS : "reserved on"
    PCS ||--o{ SESSIONS : "used in"
    BOOKINGS ||--o| SESSIONS : "checked into"
    BOOKINGS ||--o{ PAYMENTS : "token payment for"
    SESSIONS ||--o{ PAYMENTS : "final bill for"

    USERS {
        bigint id PK
        varchar name
        varchar email
        varchar phone
        varchar password
        enum role
        boolean enabled
    }

    PCS {
        bigint id PK
        int pc_number
        varchar specifications
        enum status
    }

    BOOKINGS {
        bigint id PK
        bigint customer_id FK
        bigint pc_id FK
        datetime booking_time
        datetime expires_at
        datetime checked_in_at
        decimal token_amount
        boolean token_paid
        boolean token_forfeited
        enum status
    }

    SESSIONS {
        bigint id PK
        bigint booking_id FK
        bigint pc_id FK
        bigint customer_id FK
        varchar walk_in_name
        varchar walk_in_phone
        bigint started_by FK
        bigint ended_by FK
        datetime start_time
        datetime end_time
        enum billing_type
        int duration_minutes
        decimal gross_amount
        decimal token_adjusted
        decimal net_amount_due
        enum status
    }

    PAYMENTS {
        bigint id PK
        bigint user_id FK
        bigint booking_id FK
        bigint session_id FK
        decimal amount
        enum type
        enum method
        enum status
        varchar razorpay_order_id
    }

    AUDIT_LOGS {
        bigint id PK
        bigint user_id FK
        varchar action
        varchar entity_type
        bigint entity_id
        text details
    }
```

## Design notes

`sessions.booking_id` is nullable because a session can originate from either a checked-in online reservation or a staff-initiated walk-in; `sessions.customer_id` is likewise nullable, with `walk_in_name`/`walk_in_phone` populated instead for anonymous walk-ins.

`bookings.pc_id` is nullable in the schema (though always populated in practice once auto-assignment succeeds) so the column can safely hold historical bookings if PC auto-assignment logic ever changes to support pre-assignment-then-confirm flows.

`payments` has independent nullable FKs to both `bookings` and `sessions` because a single customer can have two distinct payment events: the ₹50 token at booking time, and the final bill settlement at session end — these can use different payment methods (Razorpay for the token, cash for the final bill).

Indexes are placed on every status column (`pcs.status`, `bookings.status`, `sessions.status`, `payments.status`) since the scheduler, dashboards, and staff views all filter heavily by status, plus on `bookings.booking_time` and `audit_logs.created_at` for time-range queries.
