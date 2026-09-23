# 🎮 Gaming Cafe Management System

A full-stack gaming cafe management system for managing PC reservations, customers, bookings, pricing, and cafe operations.

## Features

- PC availability management
- Customer booking and reservation management
- Booking duration and pricing calculation
- Admin/staff management
- Backend REST APIs
- Database integration
- Responsive web interface
- Git-based version control

##  Tech Stack

### Frontend
- React.js
- HTML
- CSS
- JavaScript

### Backend
- Spring Boot
- Java
- REST APIs

### Database
- MySQL
- MongoDB

### Tools
- Git
- GitHub
- VS Code

##  REST API

The application uses REST APIs to communicate between the frontend and Spring Boot backend.

### Booking APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create a new booking |
| GET | `/api/bookings/my-history` | Get customer's booking history |
| GET | `/api/bookings/{id}` | Get booking details |
| GET | `/api/bookings/active` | Get active bookings |
| POST | `/api/bookings/{id}/cancel` | Cancel a booking |

### Authorization

The backend uses role-based access control:

- `CUSTOMER` — create bookings and view booking history
- `STAFF` / `OWNER` — access active booking information
- Protected endpoints are secured using Spring Security

### API Response

The backend uses a common `ApiResponse` structure for API responses.

##  Screenshots

### Home Page
![Home Page](docs/LoginPage)

### Booking Page
![Booking Page](docs/booking.png)

### Dashboard
![Dashboard](docs/dashboard.png)

##  Architecture

```text
React Frontend
      ↓
REST APIs
      ↓
Spring Boot Backend
      ↓
Database
