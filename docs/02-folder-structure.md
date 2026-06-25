# Folder Structure

```
gaming-cafe-management/
├── backend/                          Spring Boot application (Java 17, Maven)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── resources/
│       │   ├── application.yml       Default config, env-var driven
│       │   └── application-prod.yml  Production overrides
│       └── java/com/gamingcafe/
│           ├── config/               Security + WebSocket configuration
│           ├── controller/           REST endpoints (thin, delegate to services)
│           ├── dto/                  Request/response objects, grouped by feature
│           ├── entity/                JPA entities + enums
│           ├── exception/             Custom exceptions + global handler
│           ├── repository/            Spring Data JPA repositories
│           ├── scheduler/             ReservationExpiryScheduler (runs every 60s)
│           ├── security/              JWT + Spring Security plumbing
│           ├── service/               All business logic lives here
│           └── websocket/             PcStatusBroadcaster (STOMP push)
│
├── frontend/                          React + Vite + Tailwind application
│   ├── package.json
│   ├── Dockerfile / nginx.conf
│   └── src/
│       ├── api/                       One file per backend resource (axios wrappers)
│       ├── components/common/         Navbar, ProtectedRoute, StatusBadge, etc.
│       ├── components/customer/       Shared widgets (PcStatusGrid)
│       ├── context/AuthContext.jsx     Login/register/logout + token storage
│       ├── hooks/usePcStatusSocket.js  STOMP/SockJS subscription hook
│       └── pages/
│           ├── auth/                   Login, Register
│           ├── public/                 Pricing (no auth required)
│           ├── customer/               Dashboard, CreateReservation, BookingHistory
│           ├── staff/                  Dashboard, WalkInForm, ActiveSessions
│           └── owner/                  Dashboard, RevenueReports, StaffManagement,
│                                        CustomerHistory, AuditLogs
│
├── database/
│   ├── schema.sql                      Full DDL — all 6 tables, indexes, FKs
│   └── seed.sql                        10 PCs + 3 demo accounts (owner/staff/customer)
│
├── docs/                               This documentation set
└── docker-compose.yml                  mysql + backend + frontend, one command up
```

## Backend package design notes

Controllers stay thin: request validation via `@Valid`, role checks via `@PreAuthorize`, and a single call into a service method. All business rules (PC assignment, billing math, expiry logic) live in `service/`, which makes the rules unit-testable independent of HTTP and easy to audit against the original requirements.

DTOs are grouped by feature folder (`dto/booking`, `dto/session`, etc.) rather than dumped in one package, since the project has eight largely-independent modules and this keeps each one's request/response shapes easy to find.
