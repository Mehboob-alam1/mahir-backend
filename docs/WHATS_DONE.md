# What’s Done – Find Mahir Backend

One-page summary of implemented features (for repo and stakeholders).

---

## Auth & users
- **Sign up / Sign in / Refresh / Check session / Logout** at `/auth/...`
- **Roles:** USER (customer), MAHIR (professional), ADMIN
- **GET /api/users/me** – current user with **role** and **credits** (MAHIR: 3 free credits)
- **GET /api/users/{id}/public** – public profile (e.g. Mahir before accepting bid)

---

## Jobs
- **POST /api/jobs** – USER creates job (title, description, categoryId, location, optional scheduledAt, budgetMin, budgetMax, durationHours)
- **GET /api/jobs?filter=my** – current user’s posted jobs
- **GET /api/jobs** (no filter) – open jobs for Mahir (optional categoryId)
- **GET /api/jobs/{id}** – job detail including **bidCount**
- **PATCH /api/jobs/{id}** – update job (poster only)
- **POST /api/jobs/{id}/cancel** – cancel job (poster only)
- **GET /api/categories** – list categories for dropdown

---

## Bids (Apply / Accept / Reject)
- **POST /api/jobs/{jobId}/bids** – MAHIR applies: **message** required; proposedPrice, proposedAt (ISO-8601), estimatedDurationHours optional. Response includes mahir name, avatar, rating, reviewCount
- **GET /api/jobs/{jobId}/bids** – poster: all bids; Mahir: only their bid (others 403)
- **GET /api/bids** – **MAHIR only** – “My bids” (optional status)
- **POST .../bids/{bidId}/accept** – poster accepts → creates **Booking** + **chat thread**, returns **booking** with **chatThreadId**, sends **BID_ACCEPTED** notification to Mahir
- **POST .../bids/{bidId}/reject** – poster rejects; Mahir gets BID_REJECTED notification

---

## Bookings
- **GET /api/bookings** – “My bookings”: USER as customer, MAHIR as mahir (optional status). No 403 for USER
- **GET /api/bookings/{id}** – detail (participant only)
- **PATCH /api/bookings/{id}/status** – Customer: REACHED, IN_PROGRESS, COMPLETED, CANCELLED; Mahir: CANCELLED only
- **POST /api/bookings/{id}/cancel** – cancel with optional reason

---

## WhatsApp contact
- **POST /api/jobs/{jobId}/whatsapp-contact** – MAHIR only. Uses **1 credit**; returns posterPhoneNumber and remainingCredits. Fails if no credits (use Apply)

---

## Chat
- **GET /api/bookings/{bookingId}/chat** – get or create thread → **{ threadId }**
- **GET /api/chats** – list threads (threadId, otherPartyName, lastMessagePreview, lastMessageAt, unreadCount). Available to both sides after bid accept
- **GET /api/chats/{threadId}/messages** – paginated messages
- **POST /api/chats/{threadId}/messages** – send message `{ "content": "..." }`

---

## Notifications
- **GET /api/notifications** – paginated (title, body, type, **referenceId**, read, createdAt)
- **GET /api/notifications/unread-count** – `{ "count": number }`
- **PATCH /api/notifications/{id}/read** – mark read
- Backend creates notifications: BID_ACCEPTED (referenceId = bookingId), BID_REJECTED, BID_RECEIVED, BOOKING_CANCELLED, CHAT_MESSAGE

---

## Reviews
- **POST /api/reviews** – customer reviews completed booking (bookingId, rating, comment). One per booking
- Mahir profile shows average rating and review count

---

## Docs & tools
- **HOW_THE_APP_WORKS.md** – full flow (roles, jobs, apply vs WhatsApp, accept/reject, statuses, chat, reviews)
- **BACKEND_CHECKLIST.md** – backend checklist with all items marked done
- **Postman collection** – all above endpoints with examples and variables

---

*Last updated: Feb 2025*
