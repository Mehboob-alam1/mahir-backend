# FCM / Firebase on a VPS (Docker `docker run --env-file`)

The backend behaves the same as on Railway: if Firebase initializes, **every notification also sends FCM push** (when the user has registered a token).

On the VM, push often **fails** for one reason:

## Docker `.env` files break multiline JSON

`docker run --env-file .env.production` reads **one `KEY=value` per line**. A Firebase JSON pasted on multiple lines is **not** read as one value — you get a **truncated** or invalid JSON → Firebase does not start → no push (DB notifications still work).

### Fix A — Base64 (recommended on VM)

On your Mac (or the server), with your service account JSON file:

```bash
base64 -i firebase-service-account.json | tr -d '\n'
```

Copy the **single line** output into `.env.production`:

```env
APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64=PASTE_THE_LONG_BASE64_LINE_HERE
```

Remove or comment out `APP_FIREBASE_SERVICE_ACCOUNT_JSON` if you use base64.

Rebuild/restart the container:

```bash
docker rm -f mahir-api
docker run -d --name mahir-api --restart unless-stopped -p 8080:8080 --env-file .env.production mahir-api
```

### Fix B — One-line JSON

Minify JSON to **one line** (no line breaks), then:

```env
APP_FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}
```

Tools: `jq -c . < firebase-service-account.json` or an online JSON minifier.

### Fix C — Mount file into container

1. Copy `firebase-service-account.json` to the server (e.g. `/opt/mahir-backend/secrets/firebase.json`), **chmod 600**, do not commit.

2. Run with mount + env:

```bash
docker run -d --name mahir-api --restart unless-stopped -p 8080:8080 \
  -v /opt/mahir-backend/secrets/firebase.json:/run/firebase.json:ro \
  -e APP_FIREBASE_SERVICE_ACCOUNT_PATH=/run/firebase.json \
  --env-file .env.production \
  mahir-api
```

Ensure `.env.production` does **not** set a conflicting path unless you use this mount path.

---

## Verify

1. **Logs** after startup:

   `Firebase initialized for FCM push (source: APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64).`

2. **Authenticated user** (Bearer token):

   `GET /api/notifications/push-status`  
   → `firebaseInitialized: true`, `hasFcmToken: true` after the app calls `POST /api/users/me/fcm-token`.

3. **Test push:**

   `POST /api/test-notification` with a real FCM token.

---

## Same as Railway

- Same Firebase **project** as the mobile app (`google-services.json`).
- Same logic: `NotificationService.create` → DB row + `PushNotificationService.sendToUser`.
- Railway worked because the JSON was stored as a **multiline secret** in the platform UI; plain `.env` files on Docker need **base64** or **one line** or a **mounted file**.
