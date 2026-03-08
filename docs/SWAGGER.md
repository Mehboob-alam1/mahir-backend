# Swagger / OpenAPI Documentation

The API is documented with **OpenAPI 3** (Swagger) using [springdoc-openapi](https://springdoc.org/).

## URLs (when the app is running)

| Purpose        | URL (local)                          |
|----------------|--------------------------------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs   |

On Railway, use your app domain, e.g. `https://mahir-backend-production.up.railway.app/swagger-ui.html`.

## Using Swagger UI

1. Open **Swagger UI** in your browser.
2. Expand a section (Auth, Categories, Mahirs, Users, Bookings, Reviews).
3. Click **Try it out** on an endpoint, fill parameters/body, then **Execute**.
4. For **protected endpoints** (e.g. Get My Profile, Create Booking):
   - Click **Authorize** at the top.
   - Enter your **access token** (from signin/signup). Do not add the word "Bearer" — the UI adds it.
   - Click **Authorize**, then **Close**. Subsequent requests will send the token.

## Disabling in production

To hide Swagger UI and API docs in production, set in your environment (e.g. Railway variables):

```
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```
