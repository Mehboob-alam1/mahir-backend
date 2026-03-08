# API Reference – Request, Response, Success & Failure

Base URL: `https://your-app.up.railway.app` or `http://localhost:8080`

Protected endpoints require header: `Authorization: Bearer <accessToken>`

---

## Error response (all failures)

All error responses use this structure (except validation, which adds `fieldErrors`):

```json
{
  "timestamp": "2025-02-19T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error description",
  "path": "/auth/signup"
}
```

**Validation errors (400)** include a list of field errors:

```json
{
  "timestamp": "2025-02-19T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Validation errors",
  "path": "/auth/signup",
  "fieldErrors": [
    { "field": "email", "message": "Invalid email format" },
    { "field": "password", "message": "Password must be 6-100 characters" }
  ]
}
```

---

# Auth

## POST /auth/signup

**Auth:** None

**Request body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| role | string | Yes | `"USER"` or `"MAHIR"` |
| fullName | string | Yes | 1–100 chars |
| email | string | Yes | Valid email, unique |
| password | string | Yes | 6–100 chars |
| phoneNumber | string | Yes | Max 20 chars |
| dateOfBirth | string | Yes | ISO date `"1990-05-15"` |
| location | object | Yes | `{ streetAddress?, latitude, longitude }` |
| accountType | string | Yes | `"FREEMIUM"` or `"PREMIUM"` |
| serviceCategoryIds | long[] | No* | MAHIR: IDs from GET /api/categories |
| customServiceName | string | No* | MAHIR: max 200 chars |

\* MAHIR: at least one of `serviceCategoryIds` or `customServiceName` (or both).

**Success (201 Created):**

```json
{
  "success": true,
  "message": "Registration successful",
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "role": "USER",
    "fullName": "Ali Khan",
    "email": "ali@example.com",
    "phoneNumber": "+923001234567",
    "dateOfBirth": "1995-01-20",
    "location": { "streetAddress": "Block 5", "latitude": 24.8, "longitude": 67.05 },
    "accountType": "FREEMIUM",
    "serviceCategories": null,
    "customServiceName": null,
    "createdAt": "2025-02-19T12:00:00"
  }
}
```

**Failure:**

| Status | When | Example body |
|--------|------|--------------|
| 400 | Validation (invalid email, short password, etc.) | `fieldErrors` array |
| 409 | Email already registered | `{ "message": "Email already registered: ali@example.com" }` |

---

## POST /auth/signin

**Auth:** None

**Request body:**

| Field | Type | Required |
|-------|------|----------|
| email | string | Yes |
| password | string | Yes |

**Success (200 OK):** Same structure as signup response (`accessToken`, `refreshToken`, `expiresIn`, `user`).

**Failure:**

| Status | When | Example body |
|--------|------|--------------|
| 400 | Validation | `fieldErrors` |
| 401 | Invalid email or password | `{ "message": "Invalid email or password" }` |

---

## POST /auth/refresh

**Auth:** None

**Request body:**

```json
{ "refreshToken": "eyJhbGc..." }
```

**Success (200 OK):**

```json
{
  "success": true,
  "message": "Token refreshed",
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 900
}
```

**Failure:**

| Status | When |
|--------|------|
| 401 | Missing/invalid refresh token |

---

## POST /auth/logout

**Auth:** None  
**Request body:** None (client discards tokens).

**Success (200 OK):** Empty or minimal body.

---

## GET /auth/check-session

**Auth:** Bearer token required.

**Request:** Header `Authorization: Bearer <accessToken>`

**Success (200 OK):**

```json
{
  "success": true,
  "user": {
    "id": 1,
    "role": "USER",
    "fullName": "Ali Khan",
    "email": "ali@example.com",
    "phoneNumber": "+923001234567",
    "dateOfBirth": "1995-01-20",
    "location": { "streetAddress": "...", "latitude": 24.8, "longitude": 67.05 },
    "accountType": "FREEMIUM",
    "serviceCategories": null,
    "customServiceName": null,
    "createdAt": "2025-02-19T12:00:00"
  }
}
```

**Failure:**

| Status | When |
|--------|------|
| 401 | No token, invalid or expired token |

---

## POST /auth/forgot-password

**Auth:** None

**Request body:**

```json
{ "email": "ali@example.com" }
```

**Success (200 OK):** (Same message whether email exists or not.)

```json
{
  "success": true,
  "message": "If an account exists with this email, a reset link has been sent."
}
```

**Failure:** 400 if validation fails (e.g. invalid email).

---

## POST /auth/reset-password

**Auth:** None

**Request body:**

| Field | Type | Required |
|-------|------|----------|
| token | string | Yes | From reset link |
| newPassword | string | Yes | 6–100 chars |

**Success (200 OK):**

```json
{
  "success": true,
  "message": "Password has been reset. You can now sign in."
}
```

**Failure:**

| Status | When |
|--------|------|
| 401 | Invalid or expired token |

---

# Categories

## GET /api/categories

**Auth:** None

**Request:** No body. No required query params.

**Success (200 OK):**

```json
[
  { "id": 1, "name": "Plumbing", "description": "Plumbing and pipe work" },
  { "id": 2, "name": "Electrical", "description": "Electrical repairs and installations" }
]
```

**Failure:** No expected client errors (500 on server error).

---

# Mahirs

## GET /api/mahirs

**Auth:** None

**Query params:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| page | int | No | 0-based, default 0 |
| size | int | No | Page size, default 20 |
| categoryId | long | No | Filter by category ID |

**Success (200 OK):** Paginated list.

```json
{
  "content": [
    {
      "id": 2,
      "fullName": "Sara Ahmed",
      "email": "sara@example.com",
      "phoneNumber": "+923009876543",
      "location": { "streetAddress": "Gulberg III", "latitude": 31.52, "longitude": 74.36 },
      "accountType": "PREMIUM",
      "serviceCategories": [
        { "id": 1, "name": "Plumbing", "description": "..." },
        { "id": 2, "name": "Electrical", "description": "..." }
      ],
      "customServiceName": "Custom Home Repairs",
      "averageRating": 4.5,
      "reviewCount": 10
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

**Failure:** 500 on server error.

---

## GET /api/mahirs/{id}

**Auth:** None

**Path:** `id` – Mahir user ID.

**Success (200 OK):** Single object, same shape as one element in `GET /api/mahirs` response (includes `averageRating`, `reviewCount`).

**Failure:**

| Status | When |
|--------|------|
| 404 | Mahir not found or ID is not a MAHIR user |

---

## GET /api/mahirs/{mahirId}/reviews

**Auth:** None

**Query params:** `page`, `size` (optional, default pagination).

**Success (200 OK):** Paginated list.

```json
{
  "content": [
    {
      "id": 1,
      "bookingId": 5,
      "reviewerId": 1,
      "reviewerName": "Ali Khan",
      "mahirId": 2,
      "rating": 5,
      "comment": "Excellent service.",
      "createdAt": "2025-02-19T14:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

**Failure:**

| Status | When |
|--------|------|
| 404 | Mahir not found |

---

# Users

## GET /api/users/me

**Auth:** Bearer required.

**Success (200 OK):** Same user object as in signin/check-session (id, role, fullName, email, phoneNumber, dateOfBirth, location, accountType, serviceCategories, customServiceName, createdAt).

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated |

---

## PUT /api/users/me

**Auth:** Bearer required.

**Request body (all fields optional):**

| Field | Type | Description |
|-------|------|-------------|
| fullName | string | 1–100 chars |
| phoneNumber | string | Max 20 |
| dateOfBirth | string | ISO date |
| location | object | `{ streetAddress?, latitude, longitude }` |
| accountType | string | FREEMIUM / PREMIUM |
| serviceCategoryIds | long[] | MAHIR only |
| customServiceName | string | MAHIR only, max 200 |
| password | string | 6–100 chars |

**Success (200 OK):** Updated user object (same shape as GET /api/users/me).

**Failure:**

| Status | When |
|--------|------|
| 400 | Validation (e.g. password too short) |
| 401 | Not authenticated |

---

## GET /api/users

**Auth:** Bearer required.

**Query params:** `page`, `size` (optional).

**Success (200 OK):** Paginated list of users (same user object shape; no `averageRating`).

```json
{
  "content": [
    {
      "id": 1,
      "role": "USER",
      "fullName": "Ali Khan",
      "email": "ali@example.com",
      "phoneNumber": "+923001234567",
      "dateOfBirth": "1995-01-20",
      "location": { "streetAddress": "...", "latitude": 24.8, "longitude": 67.05 },
      "accountType": "FREEMIUM",
      "serviceCategories": null,
      "customServiceName": null,
      "createdAt": "2025-02-19T12:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

**Failure:** 401 if not authenticated.

---

## GET /api/users/{id}

**Auth:** Bearer required.

**Success (200 OK):** Single user object.

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated |
| 404 | User not found |

---

## PUT /api/users/{id}

**Auth:** Bearer required. Allowed only when `id` is the current user.

**Request body:**

| Field | Type | Required |
|-------|------|----------|
| name | string | Yes |
| email | string | Yes |
| password | string | No (optional) |

**Success (200 OK):** Updated user object.

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated or id ≠ current user |
| 404 | User not found |
| 409 | New email already registered |

---

## DELETE /api/users/{id}

**Auth:** Bearer required. Allowed only when `id` is the current user.

**Success (204 No Content):** No body.

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated or id ≠ current user |
| 404 | User not found |

---

## POST /api/users

**Auth:** Bearer required.

**Request body:**

| Field | Type | Required |
|-------|------|----------|
| name | string | Yes |
| email | string | Yes |
| password | string | Yes (for create) |

**Success (201 Created):** User object (same shape as GET /api/users/{id}).

**Failure:**

| Status | When |
|--------|------|
| 400 | Validation or missing password |
| 401 | Not authenticated |
| 409 | Email already registered |

---

# Bookings

## POST /api/bookings

**Auth:** Bearer required. Only USER (customer) can create.

**Request body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| mahirId | long | Yes | MAHIR user ID |
| scheduledAt | string | No | ISO date-time |
| message | string | No | Max 1000 chars |

**Success (201 Created):**

```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Ali Khan",
  "customerEmail": "ali@example.com",
  "mahirId": 2,
  "mahirName": "Sara Ahmed",
  "mahirEmail": "sara@example.com",
  "status": "PENDING",
  "scheduledAt": "2025-03-01T10:00:00",
  "message": "I need plumbing repair.",
  "createdAt": "2025-02-19T12:00:00Z",
  "updatedAt": "2025-02-19T12:00:00Z"
}
```

**Failure:**

| Status | When |
|--------|------|
| 400 | Validation (e.g. missing mahirId) |
| 401 | Not authenticated or not USER role |
| 404 | Mahir not found |
| 401 | Target is not a Mahir / Cannot book yourself |

---

## GET /api/bookings

**Auth:** Bearer required.

**Query params:** `page`, `size` (optional).

**Success (200 OK):** Paginated list of booking objects (same shape as POST response).

**Failure:** 401 if not authenticated.

---

## GET /api/bookings/{id}

**Auth:** Bearer required. Caller must be the customer or the assigned Mahir of this booking.

**Success (200 OK):** Single booking object (same shape as above).

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated |
| 403/401 | Not the customer or Mahir for this booking |
| 404 | Booking not found |

---

## PATCH /api/bookings/{id}/status

**Auth:** Bearer required.

**Query param:** `status` – one of: `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `CANCELLED`.

- **Mahir** can set: `ACCEPTED`, `REJECTED`, `COMPLETED`.
- **Customer** can set: `CANCELLED`.

**Success (200 OK):** Updated booking object (same shape as POST /api/bookings response).

**Failure:**

| Status | When |
|--------|------|
| 401 | Not authenticated / not allowed to change status for this booking |
| 404 | Booking not found |

---

# Reviews

## POST /api/reviews

**Auth:** Bearer required. Only USER (customer) who made the booking can create.

**Request body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| bookingId | long | Yes | Must be COMPLETED and not already reviewed |
| rating | int | Yes | 1–5 |
| comment | string | No | Max 2000 chars |

**Success (201 Created):**

```json
{
  "id": 1,
  "bookingId": 5,
  "reviewerId": 1,
  "reviewerName": "Ali Khan",
  "mahirId": 2,
  "rating": 5,
  "comment": "Excellent service.",
  "createdAt": "2025-02-19T14:00:00Z"
}
```

**Failure:**

| Status | When |
|--------|------|
| 400 | Validation (rating not 1–5, etc.) |
| 401 | Not authenticated / not the customer for this booking |
| 404 | Booking not found |
| 401 | Booking not COMPLETED or already reviewed |

---

## GET /api/mahirs/{mahirId}/reviews

Documented under **Mahirs** above.

---

# Health

## GET / or GET /health

**Auth:** None

**Success (200 OK):**

```json
{
  "status": "UP",
  "service": "mahir-backend"
}
```

---

## Summary: HTTP status codes

| Code | Meaning |
|------|---------|
| 200 | OK – success (GET, PUT, PATCH) |
| 201 | Created – success (POST signup, POST booking, POST review, POST user) |
| 204 | No Content – success (DELETE user) |
| 400 | Bad Request – validation or invalid input |
| 401 | Unauthorized – missing/invalid token or not allowed |
| 403 | Forbidden – (if used) not allowed for this resource |
| 404 | Not Found – resource does not exist |
| 409 | Conflict – e.g. email already registered |
| 500 | Internal Server Error – server failure |
