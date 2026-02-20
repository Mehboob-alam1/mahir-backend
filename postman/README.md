# Postman collection – FindMahir Demoapp API

## Import

1. Open Postman.
2. **Import** → **Upload Files** (or drag and drop).
3. Select `FindMahir-Demoapp-API.postman_collection.json`.

## Variables

The collection uses these variables (edit via collection **Variables** tab):

| Variable       | Purpose                                      | Set automatically?     |
|----------------|----------------------------------------------|--------------------------|
| `baseUrl`      | API base URL (default: `http://localhost:8080`) | No (edit if needed)   |
| `accessToken`  | JWT for protected requests                   | Yes (after Sign Up / Sign In) |
| `refreshToken` | Used by **Refresh Token** request           | Yes (after Sign Up / Sign In) |
| `userId`       | Used in Get/Update/Delete User by ID         | Yes (after Sign Up / Sign In) |

## How to use

1. Start the app:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
   ```
2. In Postman, run **Auth → Sign Up** (or **Sign In** if the user exists).
3. The script in that request will save `accessToken`, `refreshToken`, and `userId` into the collection variables.
4. Run any request under **Users**; they use `Authorization: Bearer {{accessToken}}` automatically.

## Folders

- **Auth** – Sign Up, Sign In, Refresh Token, Check Session, Logout (no token needed for sign up/sign in/refresh/logout).
- **Users** – Create, Get All, Get by ID, Update, Delete (all require a valid `accessToken`).

## Changing base URL

If your server runs on another host/port, set the collection variable `baseUrl`, e.g. `http://localhost:8080` or `https://your-api.com`.
