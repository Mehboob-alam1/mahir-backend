# FCM Test Notification API (Spring Boot)

This document describes the **test push notification** flow: a simple REST API that sends a notification to a specific FCM device token using the Firebase Admin SDK.

---

## 1. Maven dependency (Firebase Admin SDK)

In `pom.xml`:

```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

This is already present in the project.

---

## 2. Firebase initialization configuration

Firebase is initialized at startup by **`FirebaseConfig`** (`config/FirebaseConfig.java`). It tries, in order:

1. **`APP_FIREBASE_SERVICE_ACCOUNT_JSON`** (env) or **`app.firebase.service-account-json`** – raw JSON string (best for production, e.g. Railway).
2. **`APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64`** (env) or **`app.firebase.service-account-json-base64`** – base64-encoded JSON.
3. **`app.firebase.service-account-path`** – path to a JSON file (default: **`./firebase-service-account.json`**).

If none are set or the file is missing, the app still runs; push is simply skipped (the test endpoint will return 503 with a hint).

---

## 3. Where to place `firebase-service-account.json` and how to obtain it

### Where to place the file

- **Recommended (local/dev):** Put the file in the **project root** (same directory as `pom.xml`), named exactly:
  ```text
  firebase-service-account.json
  ```
  The default path is `./firebase-service-account.json`, so no extra configuration is needed when you run the app from the project root.

- **Custom path:** Set `app.firebase.service-account-path` (or env `APP_FIREBASE_SERVICE_ACCOUNT_PATH`) to the full path of the file.

- **Production:** Do **not** commit the key. Use the env var **`APP_FIREBASE_SERVICE_ACCOUNT_JSON`** and paste the full JSON (see `docs/FIREBASE_PUSH_SETUP.md`).

The file is **gitignored** so it is never committed.

### How to obtain the file from Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Select your project (or create one).
3. Open **Project settings** (gear icon) → **Service accounts**.
4. Click **Generate new private key** and confirm. A JSON file will download.
5. Rename/copy it to `firebase-service-account.json` and place it in the project root (or set the path as above).

Use the **same Firebase project** as your mobile app so that FCM tokens from the app are valid for this backend.

---

## 4. Application structure

| Component | Role |
|----------|------|
| **`FirebaseConfig`** | Initializes Firebase Admin SDK at startup (from JSON env, base64, or file path). |
| **`PushNotificationService`** | Uses `FirebaseMessaging` to send messages: `sendToUser(userId, title, body)` (for app events) and `sendToToken(token, title, body)` (for the test API). |
| **`NotificationService`** | In-app notifications (DB list, unread count, mark read) and `sendPushToToken(token, title, body)` which delegates to `PushNotificationService.sendToToken`. |
| **`TestNotificationController`** | Exposes **`POST /api/test-notification`**; accepts `token`, `title`, `body` and calls `NotificationService.sendPushToToken`. |

The notification payload contains **title** and **body** only; `FirebaseMessaging.getInstance().send(message)` is used to send it.

---

## 5. Test API

**Endpoint:** `POST /api/test-notification`  
**Auth:** None (endpoint is permitted for testing).  
**Request body (JSON):**

```json
{
  "token": "FCM_DEVICE_TOKEN",
  "title": "Test Notification",
  "body": "This is a test notification"
}
```

- **`token`** (required): FCM device token. You can use the token already stored by the app (the app pushes it via **`POST /api/users/me/fcm-token`**); to get it for testing you can read it from the app logs or from your backend (e.g. user profile) and paste it here.
- **`title`** (optional): defaults to `"Test Notification"` if omitted.
- **`body`** (optional): defaults to `"This is a test notification"` if omitted.

**Responses:**

- **200 OK:** `{ "messageId": "<FCM message id>", "status": "sent" }` – push was sent; the phone should receive it.
- **400 Bad Request:** Invalid token or FCM error (e.g. expired token).
- **503 Service Unavailable:** Firebase not initialized (missing or invalid service account config).

---

## 6. How to test with Postman

1. **Run the Spring Boot server**  
   From the project root: `./mvnw spring-boot:run` (or run the main class). Ensure Firebase is initialized (e.g. `firebase-service-account.json` in project root, or env var set).

2. **Call the test endpoint**  
   - Method: **POST**  
   - URL: **`http://localhost:8080/api/test-notification`**  
   - Headers: **`Content-Type: application/json`**  
   - Body (raw JSON):
     ```json
     {
       "token": "YOUR_REAL_FCM_DEVICE_TOKEN",
       "title": "Test Notification",
       "body": "This is a test notification"
     }
     ```
   Replace `YOUR_REAL_FCM_DEVICE_TOKEN` with a real FCM token from your mobile device (e.g. from the app after it has registered with FCM and optionally after calling `POST /api/users/me/fcm-token`).

3. **Check the result**  
   - If you get **200** and a `messageId`, the backend has sent the message to FCM; the **phone should receive the push notification** (foreground, background, or when the app is closed, depending on the app’s handling).  
   - If you get **503**, Firebase is not initialized: add the service account JSON (file or env).  
   - If you get **400**, the token may be invalid or expired; ensure the app uses the same Firebase project and send the token again from the app.

---

## Summary

- **Firebase Admin SDK** is used with initialization from a **service account JSON** (file or env).
- **`NotificationService`** sends to a given FCM token via **`PushNotificationService`** and **`FirebaseMessaging`**.
- **`POST /api/test-notification`** sends a **title** and **body** to the provided **token** for easy testing.
- The **FCM token** can be the one the app already pushes with **`POST /api/users/me/fcm-token`**; use that token in the request body to test. Keep the implementation simple and production-ready: no key in the repo, env var for production, and a single test endpoint for verification.
