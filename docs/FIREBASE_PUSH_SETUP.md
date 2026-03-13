# Firebase push notifications setup (Find Mahir backend)

This guide explains how to configure Firebase Cloud Messaging (FCM) so the backend can send **push notifications** to the app (device). When a notification is created (e.g. “Your bid was accepted”), it is saved in the DB **and** sent to the user’s device via FCM so it appears on the phone (app in foreground, background, or closed).

---

## Will it work?

Yes, if you:

1. Create a Firebase project and enable Cloud Messaging.
2. In the **backend**: add the Firebase service account JSON and set `app.firebase.service-account-path`.
3. In the **app**: get an FCM token (using Firebase SDK) and send it to the backend with `POST /api/users/me/fcm-token`.
4. When the backend creates a notification (e.g. on bid accept), it will:
   - Save the notification in the DB (so `GET /api/notifications` and unread count work).
   - Look up the user’s stored FCM token and call FCM to deliver the push to the device.

Then the device receives the push and can show a system notification (and/or update in-app state).

---

## Step 1 – Firebase project and Cloud Messaging

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Create a project (or use an existing one).
3. Enable **Cloud Messaging**:
   - Project settings (gear) → **Cloud Messaging** tab.
   - If you use the **FCM HTTP v1 API** (recommended), you’ll use a **service account** (Step 2). Legacy “Server key” is deprecated.

---

## Step 2 – Service account JSON (for the backend)

The backend uses a **service account** to authenticate with FCM. It checks **in order**:

1. **Raw JSON** – `app.firebase.service-account-json` or env **`APP_FIREBASE_SERVICE_ACCOUNT_JSON`**
2. **Base64 JSON** – `app.firebase.service-account-json-base64` or env **`APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64`**
3. **File path** – `app.firebase.service-account-path` or env **`APP_FIREBASE_SERVICE_ACCOUNT_PATH`** (default: **`./firebase-service-account.json`**)

### Option A – Host the key file in the backend (works all the time, local & dev)

1. In Firebase Console: **Project settings** → **Service accounts** → **Generate new private key** (download the JSON).
2. Copy that file into your **project root** (same folder as `pom.xml`) and name it exactly:
   ```text
   firebase-service-account.json
   ```
   This path is **gitignored**, so the key is never committed. The backend **defaults** to `./firebase-service-account.json`, so no extra config is needed – just run the app and push will work.

### Option B – Production (Railway / Render / etc.) – **recommended for deployed backend**

**In-app notifications** already work (they’re in the DB). **Push notifications** need the key only on the server, not in the repo. Do this on your host (e.g. Railway):

1. Open your Firebase JSON key file on your Mac (e.g. `mahir-37ddd-firebase-adminsdk-fbsvc-5dadcecde4.json`).
2. Copy its **entire contents** (the whole JSON – from `{` to `}`).
3. In **Railway**: go to your project → **Variables** (or **Settings** → **Environment**).
4. Click **New variable** (or **Add variable**).
5. **Name:** `APP_FIREBASE_SERVICE_ACCOUNT_JSON`  
   **Value:** paste the full JSON (Railway allows multiline values).
6. Save, then **redeploy** the backend.

The backend already reads this env var. No file in the repo, no commit of the key – push notifications will work after the app sends the FCM token via `POST /api/users/me/fcm-token`.

**Alternative (single-line):** If your host doesn’t support multiline env vars, use **`APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64`** and set the value to the **base64-encoded** JSON (e.g. run `base64 -i your-key.json` on your Mac and paste the output).

### Option C – Custom file path

Set **`app.firebase.service-account-path`** (or **`APP_FIREBASE_SERVICE_ACCOUNT_PATH`**) to the full path of your key file, e.g. `/Users/me/Downloads/mahir-37ddd-firebase-adminsdk-fbsvc-5dadcecde4.json`.

If **none** of the above are set or the file is missing, the backend still runs; it just **won’t send** FCM pushes (in-app notifications and unread count still work).

---

## Step 3 – Backend: store FCM token

The app must send the device FCM token to the backend when the user is logged in:

- **Endpoint:** `POST /api/users/me/fcm-token`
- **Body:** `{ "fcmToken": "<device FCM token>" }`
- **Auth:** Bearer token (required).

The backend stores this token on the user (one token per user; last device wins). When it creates a notification, it looks up this token and sends the push via FCM.

Call this:

- After login (and after the Firebase SDK has generated the token).
- Optionally on app resume / token refresh (e.g. when Firebase calls `onNewToken`).

---

## Step 4 – App: get FCM token and send to backend

In your **mobile or web app**:

1. Add the Firebase SDK (e.g. Flutter Firebase Messaging, React Native Firebase, or Firebase JS for web).
2. Request notification permission (required on iOS and often on web).
3. Get the FCM token (e.g. `getToken()` or `onTokenRefresh`).
4. Send it to your backend:
   ```http
   POST /api/users/me/fcm-token
   Authorization: Bearer <accessToken>
   Content-Type: application/json

   { "fcmToken": "<the FCM token string>" }
   ```

If the user logs out, you can either:

- Call the same endpoint with an empty string or omit sending the token, and the backend can clear it, or  
- Rely on the next login overwriting the token.

---

## Step 5 – When the backend sends a push

Whenever the backend **creates a notification** (e.g. bid accepted, bid rejected, new chat message, booking cancelled), it:

1. Saves the notification in the DB (so `GET /api/notifications` and unread count work).
2. Looks up the user’s stored FCM token.
3. If a token exists and Firebase is configured, calls FCM to send a message to that token with **title** and **body** (from the notification).
4. FCM delivers the message to the device → the OS can show a system notification (and your app can handle the message when in foreground).

So **notifications work** in two ways:

- **In-app:** always (list and unread count from the API).
- **On device (push):** only when Firebase is configured and the user has registered an FCM token via `POST /api/users/me/fcm-token`.

---

## Summary checklist

| Step | What to do |
|------|------------|
| 1 | Create/use Firebase project, enable Cloud Messaging. |
| 2 | Generate service account JSON. On **server**: set env var **`APP_FIREBASE_SERVICE_ACCOUNT_JSON`** to the full JSON (see Option B above). Locally: use file `firebase-service-account.json` in project root or a custom path. |
| 3 | Backend: `POST /api/users/me/fcm-token` is implemented; token is stored per user. |
| 4 | App: get FCM token (Firebase SDK), send it with `POST /api/users/me/fcm-token` after login. |
| 5 | Backend: when creating a notification, it also calls FCM to send to the user’s token (implemented in `NotificationService` + `PushNotificationService`). |

If any step is missing (e.g. no Firebase key on server, or no token sent), the backend still runs and **in-app notifications** still work; only **device push** is skipped until the setup is complete.

---

## Why push didn’t appear – checklist

1. **Backend: Firebase not initialized**  
   - **Check:** On the server (e.g. Railway), is **`APP_FIREBASE_SERVICE_ACCOUNT_JSON`** set to the full JSON of your Firebase service account?  
   - **Check:** Call **GET /api/notifications/push-status** (with Bearer token). If **`firebaseInitialized`** is `false`, the env var is missing or invalid.

2. **Backend: User has no FCM token**  
   - The backend only sends push to users who have registered a token.  
   - **Check:** Call **GET /api/notifications/push-status**. If **`hasFcmToken`** is `false`, the app has not called **POST /api/users/me/fcm-token** (with the device token) after login for that user.  
   - **Fix:** In the app, after login (and when you have the FCM token from Firebase SDK), call **POST /api/users/me/fcm-token** with body `{ "fcmToken": "<token>" }`.

3. **App and backend use different Firebase projects**  
   - The FCM token is for one Firebase project. The backend must use the **same project’s** service account JSON.  
   - **Fix:** Use the same Firebase project (e.g. mahir-37ddd) in both app and backend.

4. **Invalid or expired token**  
   - After app reinstall or token refresh, the old token may be invalid.  
   - **Fix:** App should send the new token to **POST /api/users/me/fcm-token** whenever Firebase gives a new token (e.g. `onTokenRefresh`).

5. **Server logs**  
   - On deploy (e.g. Railway), check logs when a notification is created. You should see either:  
     - `FCM push sent to user X: messageId=...` (success), or  
     - `FCM push skipped for user X: Firebase not initialized...` or `...no FCM token...`, or  
     - `FCM push failed for user X: ...` (e.g. invalid token).
