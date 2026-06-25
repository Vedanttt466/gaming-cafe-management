# API Design

All endpoints are prefixed `/api`. Responses are wrapped in a consistent envelope:

```json
{ "success": true, "message": "OK", "data": { ... }, "timestamp": "2026-06-18T10:00:00" }
```

Authenticated endpoints require `Authorization: Bearer <jwt>`. Roles are `CUSTOMER`, `STAFF`, `OWNER` (STAFF endpoints are also accessible to OWNER).

## Auth — public

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Customer self-registration |
| POST | `/api/auth/login` | Returns JWT + user info for any role |

## Pricing & PC status — public

| Method | Path | Description |
|---|---|---|
| GET | `/api/pricing` | Current happy-hour/standard rates, token amount, operating hours |
| GET | `/api/pcs/status` | Live status of all 10 PCs |

## Bookings — CUSTOMER (create/view own), STAFF/OWNER (manage)

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/bookings` | CUSTOMER | Create reservation; auto-assigns a PC, status `PENDING_PAYMENT` |
| GET | `/api/bookings/my-history` | CUSTOMER | Paginated own booking history |
| GET | `/api/bookings/{id}` | any | Booking detail |
| GET | `/api/bookings/active` | STAFF/OWNER | All PENDING_PAYMENT/CONFIRMED bookings |
| POST | `/api/bookings/{id}/cancel` | owner of booking or STAFF/OWNER | Cancel; forfeits token if already paid |

## Sessions — STAFF/OWNER manage, CUSTOMER views own

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/sessions/walk-in` | STAFF/OWNER | Start walk-in session, auto-assigns a PC |
| POST | `/api/sessions/check-in` | STAFF/OWNER | Convert a CONFIRMED booking into an active session |
| POST | `/api/sessions/{id}/end` | STAFF/OWNER | End session, runs billing engine, frees PC |
| GET | `/api/sessions/active` | STAFF/OWNER | All sessions currently in progress |
| GET | `/api/sessions/my-history` | CUSTOMER | Paginated own session history |

## Payments

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/payments/booking/{bookingId}/create-order` | CUSTOMER | Creates a Razorpay order for the ₹50 token |
| POST | `/api/payments/verify` | CUSTOMER | Verifies Razorpay signature, confirms booking |
| POST | `/api/payments/cash` | STAFF/OWNER | Records a cash settlement for a session's final bill |

## Dashboard & analytics — STAFF/OWNER (OWNER only for analytics)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | STAFF/OWNER | PC counts by status, today/week/month revenue, no-shows |
| GET | `/api/analytics/revenue-trend?days=14` | OWNER | Daily revenue series for charting |
| GET | `/api/analytics/peak-hours` | OWNER | Session counts by hour-of-day, last 30 days |
| GET | `/api/analytics/customer-history` | OWNER | Per-customer visits, no-shows, total spend |

## Staff & audit management — OWNER only

| Method | Path | Description |
|---|---|---|
| POST | `/api/owner/staff` | Create a STAFF account |
| GET | `/api/owner/staff` | Paginated staff list |
| PATCH | `/api/owner/staff/{id}/enabled?enabled=true\|false` | Enable/disable a staff login |
| GET | `/api/owner/audit-logs` | Paginated audit trail |

## WebSocket

STOMP over SockJS at `/ws`. Clients subscribe to:

- `/topic/pc-status` — full PC list, pushed on every status change
- `/topic/sessions` — `"REFRESH"` ping whenever a session starts/ends (clients then re-fetch)
- `/topic/dashboard` — `"REFRESH"` ping whenever revenue-affecting state changes

## Error format

Non-2xx responses use the same envelope with `success: false` and a human-readable `message`. Validation errors additionally populate `data` with a field-name → error-message map. HTTP status codes follow convention: 404 for not-found, 400 for bad input, 409 for "no PC available", 403 for role/ownership violations, 401 for missing/invalid auth.
