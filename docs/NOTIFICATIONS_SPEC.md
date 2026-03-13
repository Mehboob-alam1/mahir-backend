# Notifications on (almost) every event

For each event below the backend:
1. **Creates** a notification row (so GET /api/notifications and unread-count work).
2. **Sends FCM** to the recipient’s stored token(s) with the same title/body (when Firebase is configured).

`referenceId` is stored as `relatedId` in the DB and returned as `referenceId` in the API. `type` is stored for deep-linking.

---

## 1. JOBS

| Event | Recipient | type | referenceId | Status |
|-------|-----------|------|-------------|--------|
| New bid on my job | USER (job poster) | BID_RECEIVED | jobId | ✅ |
| Bid accepted | MAHIR (bidder) | BID_ACCEPTED | bookingId | ✅ |
| Bid rejected | MAHIR (bidder) | BID_REJECTED | jobId | ✅ |
| Job cancelled | Each MAHIR who bid | JOB_CANCELLED | jobId | ✅ |

---

## 2. BOOKINGS

| Event | Recipient | type | referenceId | Status |
|-------|-----------|------|-------------|--------|
| Booking status REACHED | MAHIR | BOOKING_STATUS_REACHED | bookingId | ✅ |
| Booking status IN_PROGRESS | MAHIR | BOOKING_STATUS_IN_PROGRESS | bookingId | ✅ |
| Booking status COMPLETED | MAHIR | BOOKING_COMPLETED | bookingId | ✅ |
| Booking cancelled | Other party (USER or MAHIR) | BOOKING_CANCELLED | bookingId | ✅ |

---

## 3. CHAT

| Event | Recipient | type | referenceId | Status |
|-------|-----------|------|-------------|--------|
| New chat message | Other participant | CHAT_MESSAGE | threadId | ✅ (body: sender name + first 50 chars) |

---

## 4. REVIEWS

| Event | Recipient | type | referenceId | Status |
|-------|-----------|------|-------------|--------|
| New review received | MAHIR | NEW_REVIEW | bookingId | ✅ |

---

## 5. REMINDERS (optional)

| Event | Recipient | type | referenceId | Status |
|-------|-----------|------|-------------|--------|
| Booking reminder (e.g. 1 hour before) | Both USER and MAHIR | BOOKING_REMINDER | bookingId | ⏳ Not implemented (cron/scheduler) |

---

All notification types above create a DB row and trigger FCM push when the recipient has an FCM token stored (POST /api/users/me/fcm-token).
