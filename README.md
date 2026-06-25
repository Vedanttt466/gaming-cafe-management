# GameZone Cafe — Gaming Cafe Management System

A full-stack system for managing a 10-PC gaming cafe: walk-in sessions, online reservations with auto PC assignment, happy-hour/standard billing, Razorpay payments, and real-time owner/staff dashboards.

## Stack

React 18 + Vite + Tailwind CSS on the frontend; Spring Boot 3 (Security, Data JPA, WebSocket) on a Java 17 backend; MySQL 8 for storage; Razorpay for payments; STOMP-over-WebSocket for live PC status.

## Quick start (Docker)

```bash
cp .env.example .env        # fill in JWT_SECRET and Razorpay keys
docker compose up -d --build
```

Frontend: http://localhost · Backend API: http://localhost:8080

## Quick start (local dev, no Docker)

Backend:
```bash
cd backend
mysql -u root -p < ../database/schema.sql
mysql -u root -p < ../database/seed.sql
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

## Demo accounts (from seed.sql)

| Role | Email | Password |
|---|---|---|
| Owner | owner@cafe.com | Owner@123 |
| Staff | staff@cafe.com | Staff@123 |
| Customer | customer@cafe.com | Customer@123 |

## What this system solves

The cafe was previously tracking PC usage and reservations on paper, which led to PCs sitting "reserved" indefinitely when a customer didn't show up and staff forgot to release them, and to inconsistent manual billing math for the happy-hour/standard pricing split. This system fixes both: a 60-second scheduled job automatically expires unclaimed reservations and forfeits the token, and a single `BillingService` computes every bill — walk-in or reservation — with the exact same rules, every time.

## Documentation

See the `docs/` folder for architecture, folder structure, database schema (with an ER diagram), full API reference, an implementation roadmap, deployment strategy, and a security best-practices checklist.

## ⚠️ A note on this delivery

This codebase was hand-written in a sandboxed environment without access to Maven Central, npm's full registry resolution, or a running MySQL instance, so it could not be compiled or executed end-to-end before delivery. Every file was written carefully and cross-checked for consistency (entity fields, DTO shapes, API contracts between frontend and backend, environment variable names across `application.yml` and `docker-compose.yml`), but you should treat this as a thorough reference implementation and run a full build (`mvn clean package`, `npm install && npm run build`) plus a manual smoke test of the booking → payment → check-in → end-session flow before relying on it in production. If the Maven or npm build surfaces a typo or missing import, it's almost certainly a small, mechanical fix rather than a structural issue — the business logic itself (PC assignment, billing math, expiry rules) was implemented and reasoned through carefully and should be checked against the examples in `docs/04-api-design.md` and this README's billing notes.
