# Mahir public profile API (map & detail sheet)

## Endpoints

- `GET /api/mahirs/{id}` — single Mahir (public; no auth required unless PII policy below applies)
- `GET /api/mahirs` — paged search (same response shape per item)

## JSON shape (camelCase; snake_case aliases accepted on **input**)

```json
{
  "id": 42,
  "fullName": "Ali Khan",
  "email": "ali@example.com",
  "phoneNumber": "+923001234567",
  "accountType": "PREMIUM",
  "customServiceName": "AC repair",
  "averageRating": 4.7,
  "reviewCount": 12,
  "role": "MAHIR",
  "serviceCategories": [
    { "id": 1, "name": "Electrical", "description": "Electrical repairs" }
  ],
  "location": {
    "streetAddress": "Gulberg III, Lahore",
    "latitude": 31.5204,
    "longitude": 74.3587
  }
}
```

### Field notes

| Field | Notes |
|--------|--------|
| `accountType` | Backend enum: **`FREEMIUM`** or **`PREMIUM`** (not `INDIVIDUAL`). Map in the client if your UI uses other labels. |
| `location` | Omitted if the Mahir has no saved address. If present, **`streetAddress`** may be set without coordinates; **`latitude`/`longitude`** may be null (client may geocode). Prefer non-null lat/lng when the Mahir set a map pin in profile. |
| `averageRating` / `reviewCount` | Based on **public** reviews only (same logic as `GET /api/mahirs/{id}/reviews`). |
| `role` | Always **`"MAHIR"`** for this resource. |

### Snake_case aliases (deserialization / client flexibility)

Jackson accepts alternate names on requests and some clients: `full_name`, `phone_number`, `account_type`, `custom_service_name`, `average_rating`, `review_count`, `service_categories`, `location.street_address`. **Responses** are emitted in **camelCase** by default.

## PII policy (email & phone)

- **Default (`app.mahir-profile.expose-pii-without-auth=true`, env `APP_MAHIR_PROFILE_EXPOSE_PII_WITHOUT_AUTH`):** `email` and `phoneNumber` are returned to **everyone**, including unauthenticated callers (matches legacy behavior; suitable when the product treats Mahir contact as discoverable).
- **When set to `false`:** `email` and `phoneNumber` are **omitted** unless the request includes a **valid JWT** (any role: USER, MAHIR, or ADMIN). Anonymous map/profile calls then get `null`/omitted fields for those properties.

Configure per environment (e.g. production stricter, staging open).

## Registration & jobs (coordinates)

- **Sign-up** and **job create/update** still require **latitude and longitude** in the location payload so stored profiles and jobs can drive maps reliably. Mahir **responses** may still expose address-only or partial coords if legacy data exists.

## Related

- Job map / poster flow: job DTOs and `GET /api/users/{id}/public` — keep `location` naming consistent (`streetAddress`, `latitude`, `longitude`).
