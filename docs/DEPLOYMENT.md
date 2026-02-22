# Deploy Backend to Railway (Free Tier)

This guide walks you through hosting this Spring Boot API on [Railway](https://railway.app) for free. Railway offers a free trial and a low-cost hobby plan; you can also use **Render** or **Fly.io** with similar steps.

---

## Prerequisites

- A [Railway](https://railway.app) account (sign up with GitHub).
- Your code pushed to a **GitHub** repository (e.g. `Mehboob-alam1/mahir-backend`).

---

## Step 1: Create a new project on Railway

1. Go to [railway.app/new](https://railway.app/new).
2. Click **“Deploy from GitHub repo”**.
3. Select your repository (e.g. `mahir-backend`). If it’s not listed, connect your GitHub account first.
4. Railway will create a new **project** and a **service** for the repo.

---

## Step 2: Add a PostgreSQL database

1. In your Railway project, click **“+ New”** → **“Database”** → **“Add PostgreSQL”**.
2. Wait until the Postgres service is running.
3. Click the **Postgres** service → **Variables** (or **Connect**). You’ll see something like `DATABASE_URL` (and possibly `PGHOST`, `PGUSER`, etc.).

---

## Step 3: Link the database to your app

1. Click your **application service** (the one from the GitHub repo), not the database.
2. Go to **Variables**.
3. Click **“Add variable”** or **“New variable”**.
4. Add a **reference** to the Postgres `DATABASE_URL`:
   - **Name:** `DATABASE_URL`
   - **Value:** Click “Add reference” / “Variable reference” and choose the Postgres service’s **`DATABASE_URL`** (Railway may show it as `${{ Postgres.DATABASE_URL }}` or similar).
5. Add the **railway** profile so the app uses the right config:
   - **Name:** `SPRING_PROFILES_ACTIVE`
   - **Value:** `railway`
6. (Recommended) Set a strong JWT secret for production:
   - **Name:** `APP_JWT_SECRET`
   - **Value:** a long random string (at least 32 characters).
7. (Optional) Set the public URL of your app for password-reset emails:
   - **Name:** `APP_RESET_PASSWORD_BASE_URL`
   - **Value:** `https://your-app-name.up.railway.app` (you’ll get this after generating a domain).

Save the variables.

---

## Step 4: Generate a public domain

1. Click your **application service**.
2. Open the **Settings** tab.
3. Under **Networking** or **Public networking**, click **“Generate domain”**.
4. Copy the URL (e.g. `https://mahir-backend-production.up.railway.app`).
5. If you use **APP_RESET_PASSWORD_BASE_URL**, set it to this URL (or your frontend URL where users reset passwords).

---

## Step 5: Deploy

1. Railway will **build and deploy** automatically when you push to GitHub, or when you first connect the repo.
2. Build is done with Maven (Nixpacks detects Java); the app runs with profile `railway` and uses `DATABASE_URL` for PostgreSQL.
3. Check **Deployments** → select the latest deployment → **View logs** to confirm the app started (e.g. “Started DemoappApplication”).

---

## Step 6: Test the API

Use the generated domain, for example:

```bash
curl https://your-app-name.up.railway.app/api/categories
```

Or open in a browser:

- `https://your-app-name.up.railway.app/api/categories`
- `https://your-app-name.up.railway.app/auth/signin` (POST with JSON body in Postman).

Update your **Postman** collection variable `baseUrl` to this URL.

---

## How it works

- **Profile `railway`**: Uses `application-railway.properties` (port from `PORT`, JWT/reset URL from env).
- **DATABASE_URL**: If set, `RailwayDatabaseUrlProcessor` parses it and sets Spring’s datasource URL, username, and password for PostgreSQL. No need to set `SPRING_DATASOURCE_*` manually.
- **Port**: Railway sets `PORT`; the app uses `server.port=${PORT:8080}` so it listens on the correct port.

---

## Troubleshooting

### "Application failed to respond"

1. **Check deploy logs**  
   Railway Dashboard → your service → **Deployments** → latest deployment → **View logs**. Look for:
   - "Started DemoappApplication" = app started.
   - Any **exception** or "Failed to configure a DataSource" = fix below.

2. **Port**  
   The app must listen on the port Railway provides. We use `server.port=${PORT:8080}` so this is automatic. If you overrode the start command, keep:  
   `java -Dspring.profiles.active=railway -jar target/demoapp-1.0.0-SNAPSHOT.jar`

3. **Database**  
   If logs show a **database/MySQL** error:
   - Add a **PostgreSQL** service in the same project.
   - In your **app** service **Variables**, add `DATABASE_URL` as a **reference** to the Postgres service’s `DATABASE_URL`.
   - Set `SPRING_PROFILES_ACTIVE` = `railway`.

4. **Repo root**  
   If your repo has the app in a **subfolder** (e.g. `demoapp/`), set **Root Directory** in Railway to that folder (Settings → Build → Root Directory), so `pom.xml` and `target/` are in the root of the build.

5. **Health check**  
   Open `https://your-app.up.railway.app/health` or `https://your-app.up.railway.app/`. If you get `{"status":"UP","service":"mahir-backend"}`, the app is running and the problem may be caching or the exact URL you’re opening.

### Other issues

| Issue | What to do |
|-------|------------|
| Build fails | Check **Deploy logs**. Ensure `./mvnw` and `pom.xml` are in the repo root (or set Root Directory). |
| App crashes at startup | Check **View logs**. Often missing `DATABASE_URL` or Postgres not linked. Set `DATABASE_URL` (reference) and `SPRING_PROFILES_ACTIVE=railway`. |
| 503 / no response | Wait 1–2 minutes after deploy. Try `/health` or `/`. Free tier may sleep; first request can be slow. |
| DB connection error | Confirm `DATABASE_URL` is referenced from the Postgres service. |

---

## Other free options

- **Render** ([render.com](https://render.com)): Connect GitHub, add a Web Service + PostgreSQL, set env vars and build command (e.g. `./mvnw package -DskipTests`), start command: `java -jar target/demoapp-1.0.0-SNAPSHOT.jar`.
- **Fly.io** ([fly.io](https://fly.io)): Use `fly launch` and a `Dockerfile` or their Java guide; add a Postgres volume or use an external DB.

The same `railway` profile and `DATABASE_URL` parsing work anywhere that provides a `DATABASE_URL` (and optionally `PORT` and `APP_JWT_SECRET`).
