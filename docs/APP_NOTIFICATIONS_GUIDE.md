# App guide: notifications and push

Short guide for the **mobile app** (Android / iOS / Flutter) to work with the backend notifications and FCM push.

---

## 1. Use the same Firebase project

- Backend uses Firebase project **mahir-37ddd** (see `firebase-service-account.json` / Railway env).
- The app must use the **same project**: same `google-services.json` (Android) / `GoogleService-Info.plist` (iOS) from Firebase Console for **mahir-37ddd**.
- FCM tokens from a different project will not work with this backend.

---

## 2. Send FCM token to the backend after login

- When the user logs in (and whenever the FCM token refreshes), get the device token from Firebase Messaging and call:
  - **POST /api/users/me/fcm-token**
  - **Headers:** `Authorization: Bearer <accessToken>`
  - **Body:** `{ "fcmToken": "<device FCM token>" }`
- The backend stores this token and uses it to send push for all events (new bid, booking update, chat message, etc.). If you don’t call this, the user will not get push notifications.

---

## 3. Push is automatic

- You do **not** call any “send notification” API from the app.
- When the app (or another user) calls normal APIs (create job, place bid, accept bid, update booking status, send chat message, etc.), the backend **automatically** creates a notification and sends FCM push to the right user(s).
- No extra call from the app is needed for push to be sent.

---

## 4. In-app notification list (optional)

- To show an in-app list and unread badge, call:
  - **GET /api/notifications?page=0&size=20** – list (paginated). Each item has `type`, `title`, `body`, `referenceId`, `read`, `createdAt`.
  - **GET /api/notifications/unread-count** – `{ "count": number }` for badge.
  - **PATCH /api/notifications/{id}/read** – mark as read when user opens the notification.
- All require **Authorization: Bearer &lt;accessToken&gt;**.

---

## 5. Deep linking from a notification

- Use `type` and `referenceId` from the notification payload (or from GET /api/notifications) to open the right screen:

| type | referenceId | Open |
|------|-------------|------|
| NEW_JOB | jobId | Job detail |
| BID_RECEIVED | jobId | Job / bids |
| BID_ACCEPTED, BOOKING_CONFIRMED | bookingId | Booking detail |
| BID_REJECTED, JOB_CANCELLED | jobId | Job |
| BOOKING_STATUS_*, BOOKING_COMPLETED, JOB_COMPLETED, BOOKING_CANCELLED | bookingId | Booking |
| CHAT_MESSAGE | threadId | Chat thread |
| NEW_REVIEW | bookingId | Booking / Mahir profile |

---

## 6. Summary

1. Same Firebase project as backend (mahir-37ddd).
2. After login (and on token refresh): **POST /api/users/me/fcm-token** with `{ "fcmToken": "..." }`.
3. Push is sent by the backend when events happen; no “send notification” API from the app.
4. Optional: use GET notifications and unread-count for in-app list and badge; use type + referenceId for deep links.
