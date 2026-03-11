# Backend checklist – Job module & related (Find Mahir app)

Use this to ensure the backend supports everything the app needs for jobs, bookings, bids, chat, and notifications.

---

## 1. JOBS

| Requirement | Status |
|-------------|--------|
| **POST /api/jobs** – Body: title, description, categoryId, location { streetAddress, latitude, longitude }, optional: scheduledAt, budgetMin, budgetMax, durationHours. Auth: USER only. Return full JobModel. | ✅ |
| **GET /api/jobs** – Query: page, size, optional: filter, status, categoryId. filter=my → current user's posted jobs; no filter → open jobs. Paginated JobModel[] with bidCount. | ✅ |
| **GET /api/jobs/{id}** – Single job (detail), includes bidCount. | ✅ |
| **PATCH /api/jobs/{id}** – Partial update. Auth: poster only. | ✅ |
| **POST /api/jobs/{id}/cancel** – Cancel job. Auth: poster only. | ✅ |
| **GET /api/categories** – List categories for create-job dropdown. | ✅ |

**JobModel fields:** id, customerId/postedById, customerName/posterName, title, description, categoryId, categoryName, location, scheduledAt, budgetMin, budgetMax, durationHours, status, **bidCount**, createdAt, updatedAt. status: OPEN \| ASSIGNED \| CANCELLED \| COMPLETED.

---

## 2. BIDS (APPLY / ACCEPT / REJECT)

| Requirement | Status |
|-------------|--------|
| **POST /api/jobs/{jobId}/bids** – Body: message (required), optional: proposedPrice, proposedAt (ISO-8601), estimatedDurationHours. Auth: MAHIR. Return BidModel with mahir details. | ✅ |
| **GET /api/jobs/{jobId}/bids** – Query: page, size. Auth: poster or MAHIR. Paginated bids for job. | ✅ |
| **GET /api/bids** – Query: page, size, optional: status. Auth: MAHIR. Current Mahir's bids. | ✅ |
| **POST /api/jobs/{jobId}/bids/{bidId}/accept** – Auth: USER (poster). Create Booking, create/link chat thread, return { booking, chatThreadId }. Create BID_ACCEPTED notification for Mahir. | ✅ |
| **POST /api/jobs/{jobId}/bids/{bidId}/reject** – Auth: USER (poster). Mark rejected, return 200/204. | ✅ |

**BookingModel (from accept):** id, customerId, customerName, customerEmail, mahirId, mahirName, mahirEmail, status=ACCEPTED, scheduledAt, message, createdAt, updatedAt, **chatThreadId**.

---

## 3. BOOKINGS

| Requirement | Status |
|-------------|--------|
| **GET /api/bookings** – Query: page, size, optional: status. USER → bookings where customer = current user; MAHIR → bookings where mahir = current user. No 403 for USER; empty list if none. | ✅ |
| **GET /api/bookings/{id}** – Auth: participant (customer or Mahir) only. | ✅ |
| **PATCH /api/bookings/{id}/status** – Query: status (REACHED, IN_PROGRESS, COMPLETED, CANCELLED). Auth: customer or Mahir per business rules (customer sets REACHED/IN_PROGRESS/COMPLETED; both can cancel). | ✅ |
| **POST /api/bookings/{id}/cancel** – Query optional: reason. Auth: customer or Mahir. | ✅ |

---

## 4. WHATSAPP CONTACT

| Requirement | Status |
|-------------|--------|
| **POST /api/jobs/{jobId}/whatsapp-contact** – Auth: MAHIR. Deduct 1 credit; return { posterPhoneNumber?, remainingCredits? }. | ✅ |

---

## 5. CHAT

| Requirement | Status |
|-------------|--------|
| **GET /api/bookings/{bookingId}/chat** – Get or create thread. Auth: customer or Mahir of booking. Response: { threadId }. | ✅ |
| **GET /api/chats** – All threads for current user. Each: threadId, otherPartyName, lastMessagePreview/content, lastMessageAt, unreadCount. After accept bid, thread appears for both. | ✅ |
| **GET /api/chats/{threadId}/messages** – Query: page, size. Paginated messages. Auth: participant. | ✅ |
| **POST /api/chats/{threadId}/messages** – Body: { content }. Auth: participant. Return message object. | ✅ |

---

## 6. NOTIFICATIONS

| Requirement | Status |
|-------------|--------|
| **GET /api/notifications** – Query: page, size. title, body, type, referenceId, read, createdAt. | ✅ |
| **GET /api/notifications/unread-count** – Response: { count }. | ✅ |
| **PATCH /api/notifications/{id}/read** – Mark read. | ✅ |
| Create notification when user accepts bid → BID_ACCEPTED for Mahir (referenceId = bookingId). | ✅ |

---

## 7. AUTH & USERS

| Requirement | Status |
|-------------|--------|
| /auth/signin, /auth/signup, /auth/refresh, /auth/check-session, /auth/logout. | ✅ |
| User model: role (USER \| MAHIR \| ADMIN). MAHIR: credits for WhatsApp. | ✅ |
| **GET /api/users/me** – Current user with role and credits. | ✅ |

---

## 8. Quick verification checklist

- [x] GET /api/jobs?filter=my returns current user's posted jobs
- [x] GET /api/jobs (no filter) returns open jobs for Mahir
- [x] POST /api/jobs/{jobId}/bids accepts proposedAt as ISO date-time
- [x] POST .../bids/{bidId}/accept creates Booking and returns booking + chatThreadId
- [x] POST .../accept creates a Notification for the Mahir
- [x] GET /api/bookings returns Mahir's bookings when called with Mahir token
- [x] GET /api/bookings returns User's bookings when called with User token (or empty, not 403)
- [x] GET /api/bookings/{bookingId}/chat returns or creates thread; GET /api/chats lists it for both sides
- [x] POST /api/jobs/{jobId}/whatsapp-contact returns poster phone and decrements Mahir credits
