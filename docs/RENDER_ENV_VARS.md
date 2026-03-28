# Environment variables for Render

Set these in your **Web Service** → **Environment** (or **Environment Variables**).

---

## Required

| Variable | Value | Notes |
|----------|--------|------|
| **SPRING_PROFILES_ACTIVE** | `railway` | Uses PostgreSQL from `DATABASE_URL` (same config as Railway). |
| **DATABASE_URL** | (from Render Postgres) | Add a **PostgreSQL** database in Render, then in the Web Service add **DATABASE_URL** and use the **Internal Database URL** (or **External** if your app runs outside Render). You can “Link” the DB to the service so Render injects it, or copy the URL and paste it as a **Secret** variable. |
| **APP_JWT_SECRET** | (long random string, ≥32 chars) | Used to sign JWTs. Generate one (e.g. `openssl rand -base64 32`) and set as **Secret**. Example: `mySuperSecretKeyForJWTThatIsAtLeast32CharactersLong`. |

---

## Optional (recommended for production)

| Variable | Value | Notes |
|----------|--------|------|
| **APP_FIREBASE_SERVICE_ACCOUNT_JSON** | (full Firebase service account JSON) | For FCM push notifications. Paste the entire JSON from Firebase Console → Project settings → Service accounts → Generate new private key. Use **Secret** and multiline value. |
| **APP_RESET_PASSWORD_BASE_URL** | `https://your-app-name.onrender.com` | Base URL of your API (for forgot-password links in emails). Replace with your actual Render service URL. |

---

## Optional (email / SMTP)

Only needed if you want forgot-password **emails** to be sent (otherwise the reset link is only in server logs).

| Variable | Value |
|----------|--------|
| **SPRING_MAIL_HOST** | e.g. `smtp.gmail.com` |
| **SPRING_MAIL_PORT** | `587` |
| **SPRING_MAIL_USERNAME** | your email |
| **SPRING_MAIL_PASSWORD** | app password or SMTP password |
| **SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH** | `true` |
| **SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE** | `true` |

---

## Summary (minimum to deploy)

1. Create a **PostgreSQL** database in Render (Dashboard → New → PostgreSQL).
2. In your **Web Service** → **Environment**:
   - **SPRING_PROFILES_ACTIVE** = `railway`
   - **DATABASE_URL** = Internal Database URL from the Postgres service (link the DB or paste the URL as a Secret).
   - **APP_JWT_SECRET** = your long random secret (Secret).
3. Optional: **APP_FIREBASE_SERVICE_ACCOUNT_JSON** (full JSON, Secret), **APP_RESET_PASSWORD_BASE_URL** (your API URL).

**PORT** is set by Render automatically; no need to add it.
