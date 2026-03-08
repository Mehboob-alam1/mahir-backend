# Authentication API Documentation

Authentication uses **email and password** (no OTP). Two roles: **USER** (Find Mahir – customer) and **MAHIR** (Become Mahir – professional). Login is the same for both.

---

## Roles

| Role  | Description |
|-------|-------------|
| `USER` | Find Mahir – customer looking for professionals |
| `MAHIR` | Become Mahir – professional offering services |

---

## Sign Up

**POST** `/auth/signup`

Register as **Find Mahir (USER)** or **Become Mahir (MAHIR)**. Request body depends on `role`.

### Common fields (both roles)

| Field        | Type   | Required | Description |
|-------------|--------|----------|-------------|
| `role`      | string | Yes      | `"USER"` or `"MAHIR"` |
| `fullName`  | string | Yes      | Full name (1–100 chars) |
| `email`     | string | Yes      | Valid email, unique |
| `password`  | string | Yes      | 6–100 characters |
| `phoneNumber` | string | Yes   | Phone number (max 20 chars) |
| `dateOfBirth` | string | Yes   | ISO date, e.g. `"1990-05-15"` |
| `location`  | object | Yes      | See [Location object](#location-object) |
| `accountType` | string | Yes   | `"FREEMIUM"` or `"PREMIUM"` |

### Location object

```json
{
  "streetAddress": "123 Main St, City",
  "latitude": 24.8607,
  "longitude": 67.0011
}
```

- `latitude`, `longitude`: required (number).
- `streetAddress`: optional (string).

### MAHIR-only fields

| Field                | Type    | Required | Description |
|----------------------|---------|----------|-------------|
| `serviceCategoryIds` | long[]  | No*      | IDs from `GET /api/categories`. Can be empty if using `customServiceName`. |
| `customServiceName`  | string  | No*      | Custom service name (max 200 chars). Use when not selecting from categories. |

\* For MAHIR, either provide at least one of `serviceCategoryIds` or `customServiceName`, or both.

### Example: Sign up as USER (Find Mahir)

```json
{
  "role": "USER",
  "fullName": "Ali Khan",
  "email": "ali@example.com",
  "password": "secret123",
  "phoneNumber": "+923001234567",
  "dateOfBirth": "1995-01-20",
  "location": {
    "streetAddress": "Block 5, Clifton, Karachi",
    "latitude": 24.8000,
    "longitude": 67.0500
  },
  "accountType": "FREEMIUM"
}
```

### Example: Sign up as MAHIR (Become Mahir)

```json
{
  "role": "MAHIR",
  "fullName": "Sara Ahmed",
  "email": "sara@example.com",
  "password": "secret123",
  "phoneNumber": "+923009876543",
  "dateOfBirth": "1988-11-10",
  "location": {
    "streetAddress": "Gulberg III, Lahore",
    "latitude": 31.5204,
    "longitude": 74.3587
  },
  "accountType": "PREMIUM",
  "serviceCategoryIds": [1, 2],
  "customServiceName": "Custom Home Repairs"
}
```

### Response (201 Created)

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
    "location": { "streetAddress": "...", "latitude": 24.8, "longitude": 67.05 },
    "accountType": "FREEMIUM",
    "serviceCategories": null,
    "customServiceName": null,
    "createdAt": "2025-02-19T12:00:00"
  }
}
```

---

## Sign In (Login)

**POST** `/auth/signin`

Same for both USER and MAHIR.

### Request body

```json
{
  "email": "ali@example.com",
  "password": "secret123"
}
```

### Response (200 OK)

Same shape as signup response: `accessToken`, `refreshToken`, `expiresIn`, `user`.

---

## Forgot Password

**POST** `/auth/forgot-password`

Request a password reset. If the email exists, a reset link is sent to that email (or logged if SMTP is not configured).

### Request body

```json
{
  "email": "ali@example.com"
}
```

### Response (200 OK)

```json
{
  "success": true,
  "message": "If an account exists with this email, a reset link has been sent."
}
```

(For security, the message is the same whether the email exists or not.)

### Reset link

- If SMTP is configured: user receives an email with a link like  
  `{app.reset-password.base-url}/reset-password?token=...`
- If SMTP is not configured: the same link is written to server logs (for development).

The link should point to your **frontend** reset-password page. The frontend then calls **Reset Password** with the `token` from the URL and the new password.

---

## Reset Password

**POST** `/auth/reset-password`

Set a new password using the token from the forgot-password email/link.

### Request body

```json
{
  "token": "abc123...",
  "newPassword": "newSecret456"
}
```

- `token`: from the reset link query parameter (e.g. `?token=abc123...`).
- `newPassword`: 6–100 characters.

### Response (200 OK)

```json
{
  "success": true,
  "message": "Password has been reset. You can now sign in."
}
```

### Errors

- **401** – Invalid or expired token.

---

## Other auth endpoints

| Method | Endpoint           | Auth  | Description |
|--------|--------------------|-------|-------------|
| POST   | `/auth/refresh`    | No    | Body: `{"refreshToken":"..."}`. Returns new access and refresh tokens. |
| POST   | `/auth/logout`     | No    | Client should discard tokens. |
| GET    | `/auth/check-session` | Bearer | Validates token and returns current user. |

---

## Categories (for MAHIR signup)

**GET** `/api/categories`

No authentication required. Returns the list of service categories (e.g. Plumbing, Electrical, Cleaning) that MAHIR can select in signup.

### Response (200 OK)

```json
[
  { "id": 1, "name": "Plumbing", "description": "Plumbing and pipe work" },
  { "id": 2, "name": "Electrical", "description": "Electrical repairs and installations" }
]
```

Use `id` in `serviceCategoryIds` when signing up as MAHIR. Users can also send a custom service name in `customServiceName` if they don’t want to use only predefined categories.

---

## Using the access token

Send the access token in the **Authorization** header for protected endpoints:

```
Authorization: Bearer <accessToken>
```

Example: `GET /api/users/me`, `GET /api/bookings`, etc.

---

## Mahirs (search professionals)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/mahirs` | No | Search Mahirs. Query: `page`, `size`, `categoryId` (optional). Returns paginated list with `averageRating` and `reviewCount`. |
| GET | `/api/mahirs/{id}` | No | Get one Mahir by ID. |
| GET | `/api/mahirs/{mahirId}/reviews` | No | List reviews for a Mahir. Query: `page`, `size`. |

---

## My profile

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/me` | Bearer | Get current user profile. |
| PUT | `/api/users/me` | Bearer | Update current user. Body: `fullName`, `phoneNumber`, `dateOfBirth`, `location`, `accountType`, `serviceCategoryIds`, `customServiceName`, `password` (all optional). MAHIR can update categories and customServiceName. |

---

## Bookings

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/bookings` | Bearer | Create booking (USER only). Body: `mahirId`, `scheduledAt`, `message`. |
| GET | `/api/bookings` | Bearer | List my bookings (as customer or Mahir). Query: `page`, `size`. |
| GET | `/api/bookings/{id}` | Bearer | Get booking by ID (customer or assigned Mahir only). |
| PATCH | `/api/bookings/{id}/status?status=` | Bearer | Update status. Mahir: `ACCEPTED`, `REJECTED`, `COMPLETED`. Customer: `CANCELLED`. |

Status values: `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `CANCELLED`.

---

## Reviews

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/reviews` | Bearer | Create review (USER/customer only). Body: `bookingId`, `rating` (1–5), `comment`. Booking must be `COMPLETED`; one review per booking. |
| GET | `/api/mahirs/{mahirId}/reviews` | No | List reviews for a Mahir. Query: `page`, `size`. |

---

## Users (list, get by ID, update/delete own)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | Bearer | List all users (paginated). Query: `page`, `size`. |
| GET | `/api/users/{id}` | Bearer | Get user by ID. |
| PUT | `/api/users/{id}` | Bearer | Update user. **Only own profile** (id must match current user). |
| DELETE | `/api/users/{id}` | Bearer | Delete user. **Only own account** allowed. |
