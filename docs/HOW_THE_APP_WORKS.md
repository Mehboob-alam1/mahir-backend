# How the Find Mahir App Works

This document describes the end-to-end flow of the Find Mahir application: how users post jobs, how Mahirs find and respond to jobs (Apply or WhatsApp contact), how hiring and status updates work, and how chat and reviews fit in.

---

## 1. Roles

- **USER (Customer)** – Someone who needs a service. They post jobs and hire Mahirs.
- **MAHIR (Service provider)** – A professional who finds relevant jobs and can either **Apply** (free) or **Contact via WhatsApp** (costs 1 credit). Free Mahirs get **3 credits** when they sign up.
- **ADMIN** – Administrator. Can authenticate (sign up and sign in) like USER and MAHIR. Use this role for app management; admin-only endpoints can be added later.

---

## 2. User Flow: Posting a Job

1. User signs up or signs in as **USER** (Find Mahir / customer).
2. User **creates and posts a job** (e.g. “Need maths tutor, 2 hours, budget 1000 Rs/hr”).
   - API: `POST /api/jobs` with title, description, categoryId, location, schedule, budget, duration.
3. The job is **OPEN** and visible to Mahirs. No Mahir is chosen yet.

---

## 3. Mahir Flow: Finding and Viewing Jobs

1. Mahir signs up or signs in as **MAHIR**.
2. Mahir **browses open jobs** (filter by category, etc.).
   - API: `GET /api/jobs` (without `filter=my`) returns open jobs.
3. Mahir **clicks a job** → job detail opens.
   - API: `GET /api/jobs/{id}`.

On the job detail screen, the Mahir has **two options**:

- **Apply** – Sends a **request** (bid) to the user who posted the job. Free; no credits used.
- **WhatsApp contact** – Uses **1 credit** to get the job poster’s phone number so the Mahir can contact them on WhatsApp. If the Mahir has **no credits left**, they can only **Apply**.

**Credits**

- Free Mahirs get **3 credits** at signup.
- Each **WhatsApp contact** on a job costs **1 credit**.
- If credits = 0, only **Apply** is allowed.
- API: `POST /api/jobs/{jobId}/whatsapp-contact` (Mahir only). Returns poster’s phone and remaining credits. Fails with an error if no credits.

---

## 4. Apply (Bid / Request)

1. Mahir clicks **Apply** on a job and submits a bid (message, proposed price, time, etc.).
   - API: `POST /api/jobs/{jobId}/bids`.
2. A **request** is sent to the **User who posted the job**. The user sees all requests (bids) for that job.
   - API (User): `GET /api/jobs/{jobId}/bids` – list of bids with **Mahir profile** (name, avatar, rating, review count, etc.).
3. User can open **Mahir’s full profile** before deciding.
   - API: `GET /api/users/{mahirId}/public` – public profile (no email/phone).

---

## 5. Accept or Reject Request

1. On the user side, for each job they posted, they see **job details** and **who requested / applied**.
   - APIs: `GET /api/jobs?filter=my`, `GET /api/jobs/{id}`, `GET /api/jobs/{jobId}/bids`.
2. User can **Accept** or **Reject** each request (bid).
   - Accept: `POST /api/jobs/{jobId}/bids/{bidId}/accept`.
   - Reject: `POST /api/jobs/{jobId}/bids/{bidId}/reject`.
3. When the user **accepts** one Mahir’s request:
   - That Mahir is **Hired**.
   - A **booking** is created (job + customer + Mahir + agreed price).
   - A **chat room** is created for that booking.
   - Other bids for the same job are automatically rejected.

---

## 6. After Hire: Booking Statuses (Updated by User Who Posted the Job)

Only the **User who posted the job** can move the booking through these statuses:

1. **ACCEPTED** – Mahir is hired (set when user accepts the bid).
2. **REACHED** – User marks that the Mahir has **reached** or they have **agreed via chat** (e.g. time/place confirmed).
3. **IN_PROGRESS (Working)** – User marks that the job is **in progress**.
4. **COMPLETED** – User marks that the job is **done**.

- API: `PATCH /api/bookings/{id}/status?status=REACHED` (or `IN_PROGRESS`, `COMPLETED`).
- Either party can **cancel** the booking: `PATCH .../status?status=CANCELLED` or `POST .../cancel?reason=...`.

---

## 7. Chat

- There is a **chat room per booking**. It is created when the user accepts a bid.
- User and Mahir can **chat** to agree on time, place, “reached”, etc.
- APIs:
  - Get or create thread: `GET /api/bookings/{bookingId}/chat` → returns `threadId`.
  - List my chats: `GET /api/chats`.
  - Get messages: `GET /api/chats/{threadId}/messages`.
  - Send message: `POST /api/chats/{threadId}/messages` with `content`.

---

## 8. Reviews

- **After the job is COMPLETED**, the **User (customer)** can add a **review** for the Mahir (rating 1–5, comment).
- API: `POST /api/reviews` with `bookingId`, `rating`, `comment`. Only for completed bookings; one review per booking.
- Mahir’s **public profile** and **bids** show **average rating** and **review count** so users can decide before accepting.

---

## 9. Summary Diagram

```
USER (Customer)                          MAHIR (Provider)
     |                                          |
     | 1. Post job (OPEN)                        |
     |----------------------------------------->|
     |                   2. Browse open jobs     |
     |                   3. Job detail:         |
     |                      - Apply (free)      |
     |                      - WhatsApp (1 credit)
     | 4. Receive request + Mahir profile        |
     | 5. Accept or Reject                      |
     | 6. If Accept → Booking + Chat created    |
     | 7. Update status: REACHED → WORKING → COMPLETED
     | 8. Chat (agree on “reached”, etc.)       |
     | 9. After COMPLETED: User reviews Mahir   |
```

---

## 10. Key APIs Quick Reference

| Action | API |
|--------|-----|
| User: Post job | `POST /api/jobs` |
| Mahir: List open jobs | `GET /api/jobs` |
| Mahir: Job detail | `GET /api/jobs/{id}` |
| Mahir: Apply (bid) | `POST /api/jobs/{jobId}/bids` |
| Mahir: WhatsApp contact (1 credit) | `POST /api/jobs/{jobId}/whatsapp-contact` |
| User: List my jobs | `GET /api/jobs?filter=my` |
| User: List bids for job | `GET /api/jobs/{jobId}/bids` |
| User: Mahir public profile | `GET /api/users/{mahirId}/public` |
| User: Accept bid (Hire) | `POST /api/jobs/{jobId}/bids/{bidId}/accept` |
| User: Reject bid | `POST /api/jobs/{jobId}/bids/{bidId}/reject` |
| Both: My bookings | `GET /api/bookings` |
| User: Update status (REACHED / IN_PROGRESS / COMPLETED) | `PATCH /api/bookings/{id}/status?status=...` |
| Both: Chat thread for booking | `GET /api/bookings/{bookingId}/chat` |
| Both: Send / get messages | `GET/POST /api/chats/{threadId}/messages` |
| User: Review Mahir (after completed) | `POST /api/reviews` |
| Mahir: My credits | In `GET /api/users/me` (field `credits`) |

---

## 11. Credits and WhatsApp Contact (Recap)

- **Credits** are only for **MAHIR**.
- New MAHIR accounts get **3 credits**.
- **1 credit** is used each time a Mahir uses **WhatsApp contact** on a job to get the poster’s phone number.
- If **credits = 0**, the Mahir can only **Apply** (bid); they cannot use WhatsApp contact until they have more credits (e.g. via a future purchase or promo).
