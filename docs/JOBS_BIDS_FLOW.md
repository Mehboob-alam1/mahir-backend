# Jobs, Bids, Bookings & Chat Flow

## High-level flow

1. **USER (customer)** posts a **job** (e.g. "Need maths tutor, 2 hours, budget 1000 Rs/hr") via `POST /api/jobs`. No Mahir is chosen yet.
2. **MAHIR (service provider)** sees open jobs via `GET /api/jobs` (no `filter=my`) and **bids** via `POST /api/jobs/{jobId}/bids` with message, proposed price, time, etc.
3. **USER** sees all bids for their job via `GET /api/jobs/{jobId}/bids` and **accepts one** via `POST /api/jobs/{jobId}/bids/{bidId}/accept`. The backend creates a **booking**, sets the job to ASSIGNED, rejects other bids, and creates a **chat thread** for the booking.
4. Both can **view bookings** via `GET /api/bookings`, **update status** (e.g. IN_PROGRESS, COMPLETED, CANCELLED), and **chat** via `GET/POST /api/chats/{threadId}/messages`.
5. **Public profile**: `GET /api/users/{id}/public` (no email/phone) for viewing the other party.

## APIs added

| Area | Endpoints |
|------|-----------|
| **Jobs** | `POST /api/jobs`, `GET /api/jobs?filter=my\|open`, `GET /api/jobs/{id}`, `PATCH /api/jobs/{id}`, `POST /api/jobs/{id}/cancel` |
| **Bids** | `POST /api/jobs/{jobId}/bids`, `GET /api/jobs/{jobId}/bids`, `GET /api/bids`, `POST /api/jobs/{jobId}/bids/{bidId}/accept`, `POST .../reject` |
| **Bookings** | (existing) + `GET /api/bookings?status=`, `POST /api/bookings/{id}/cancel?reason=`; booking created automatically on accept bid |
| **Chat** | `GET /api/bookings/{bookingId}/chat`, `GET /api/chats`, `GET /api/chats/{threadId}/messages`, `POST /api/chats/{threadId}/messages` |
| **Notifications** | `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read` |
| **Profiles** | `GET /api/users/{id}/public` (avatarUrl, bio, rating, reviewCount; no email/phone), `PUT /api/users/me` (includes avatarUrl, bio) |

## Rules

- **Job**: Only USER can create/update/cancel. Update/cancel only if status = OPEN and no accepted bid.
- **Bid**: Only MAHIR can create. One bid per Mahir per job. USER can accept one bid per job (creates booking, job → ASSIGNED, other bids → REJECTED).
- **Booking**: Created by system when USER accepts a bid. Both can view; Mahir can set IN_PROGRESS, COMPLETED; either can cancel (with optional reason).
- **Chat**: One thread per booking; only customer and Mahir of that booking can read/send.
- **Profile**: Own profile full; public profile no email/phone.
