# Implementation Roadmap

This describes a sensible build/rollout order if a team were picking this codebase up; it does not imply any part is unfinished — all modules below are fully implemented in this delivery.

## Phase 1 — Foundation (Week 1)
Set up the MySQL schema, get the Spring Boot project booting against it, implement registration/login with JWT, and seed the 3 demo accounts. Verify role-based access control with a few manual Postman calls before building anything else — every other module depends on auth being solid.

## Phase 2 — Core operations (Week 2)
Build PC auto-assignment and the walk-in flow end-to-end (start session → end session → see a bill), since this is the simplest path through the billing engine and validates the happy-hour/standard/mixed math against the four example cases in the spec before reservations add complexity.

## Phase 3 — Reservations & payments (Week 3)
Add the booking flow, Razorpay token integration, and the check-in step that converts a CONFIRMED booking into a session. Build the `ReservationExpiryScheduler` immediately alongside this — it is the safety net that makes the reservation feature trustworthy, and should never ship without it.

## Phase 4 — Real-time & dashboards (Week 4)
Wire up the WebSocket broadcaster and the staff/owner dashboards. This is also when the polling fallback should be added and tested against a network that blocks WebSocket upgrades, since gaming cafes often run on consumer-grade routers.

## Phase 5 — Owner analytics & hardening (Week 5)
Revenue trend / peak-hour charts, customer history, staff management, and the audit log viewer. Use this phase to also do a security pass: rotate the default JWT secret, confirm CORS is locked to the real frontend origin, and load-test the scheduler under a full house of 10 simultaneous sessions.

## Suggested next features (not in this delivery)
A loyalty/membership tier system, SMS/WhatsApp reminders before a reservation expires, and a kiosk-mode read-only display for the cafe's front window would be natural next additions once the core system has been running in production for a few weeks and real usage patterns are understood.
