# Deployment Strategy

## Local / single-server (docker-compose)

The included `docker-compose.yml` runs MySQL, the Spring Boot backend, and an Nginx-served frontend build as three containers on one host — a good fit for a single physical cafe location. Copy `.env.example` to `.env`, fill in a real `JWT_SECRET` (a random base64 256-bit string), your Razorpay live or test keys, and run `docker compose up -d --build`. The frontend will be on port 80, the API on 8080.

Generate a JWT secret on Linux/macOS with `openssl rand -base64 32`.

## Production hardening checklist before going live

Replace the default JWT secret and Razorpay test keys with real production values; never commit `.env` to version control. Put the MySQL data volume on a disk that's included in your backup strategy, and take at least daily logical backups (`mysqldump`) given this database holds financial transaction records. Terminate TLS in front of both the frontend and backend — either via a reverse proxy (Caddy/Traefik) or a managed load balancer — since JWTs and Razorpay payment confirmations must never travel over plain HTTP. Set `CORS_ALLOWED_ORIGINS` to the exact production domain, not a wildcard.

## Scaling beyond one cafe location

If the business expands to multiple locations, the cleanest path is one backend+database deployment per location (PCs, bookings, and sessions are inherently location-scoped) rather than adding a `location_id` column everywhere — this keeps the auto-assignment and billing logic untouched. A managed MySQL service (RDS, Cloud SQL) removes the operational burden of running the database yourself once there's more than one location to keep backed up.

## Monitoring

`/actuator/health` is exposed and permitted without auth in `SecurityConfig` for use by container orchestrators or uptime checks. Application logs are written to stdout in the container images, ready to be picked up by `docker logs`, a log aggregator, or a sidecar — the production logging profile in `application-prod.yml` reduces framework log noise to `WARN` while keeping `com.gamingcafe` at `INFO` so business events (bookings created, sessions billed, tokens forfeited) remain visible.
