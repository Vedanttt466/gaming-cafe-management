# Security Best Practices

## Authentication & authorization

Passwords are hashed with BCrypt (Spring Security's `BCryptPasswordEncoder`) — never stored or logged in plaintext, and the `password` field is annotated `@JsonIgnore` so it can never leak through an API response even if a future change accidentally serializes the full `User` entity. JWTs are signed with HMAC-SHA256 using a server-side secret (`jwt.secret`) and expire after 1 hour; there is no server-side session store, so a compromised token is only valid until it expires — keep that expiry short rather than relying on revocation. Authorization is enforced twice: at the URL pattern level in `SecurityConfig` (coarse-grained, fails fast) and again with `@PreAuthorize` at the method level on every controller (fine-grained, catches anything a URL pattern missed).

## Secrets management

Every secret (JWT signing key, Razorpay key/secret, database password) is read from an environment variable with no committed production default — the `application.yml` defaults are placeholder/test values only, intended for local development. Never commit a real `.env` file; `.gitignore` in both `backend/` and the project root already excludes it.

## Payment integrity

The booking token payment is never trusted on the client's word — `PaymentService.verifyBookingTokenPayment` cryptographically verifies the Razorpay signature server-side via `Utils.verifyPaymentSignature` before the booking is ever marked CONFIRMED. A booking cannot be checked in by staff unless it is already CONFIRMED, which closes off any path where a customer could claim a PC without the token actually clearing.

## Input validation

Every request DTO uses `jakarta.validation` annotations (`@NotBlank`, `@Email`, `@Pattern`, `@Future`, etc.), enforced by `@Valid` in controllers and centrally translated into a structured 400 response by `GlobalExceptionHandler` — clients get a field-by-field error map rather than a raw stack trace.

## Data exposure

The global exception handler returns generic messages for unexpected (5xx-class) errors rather than leaking stack traces or internal class names; `application-prod.yml` additionally disables Spring Boot's own error stacktrace/message inclusion. CORS is restricted to an explicit allow-list of origins (`cors.allowed-origins`), not `*`, and credentials are only allowed for those listed origins.

## Operational recommendations beyond this codebase

Put the API behind a rate limiter (e.g. at the reverse-proxy layer) for the `/api/auth/**` endpoints specifically, since they're the most attractive brute-force target and are intentionally left unauthenticated. Rotate the JWT secret and Razorpay keys periodically and immediately after any suspected leak, and consider moving from a single long-lived access token to an access+refresh token pair if session lifetimes longer than an hour become a UX requirement.
