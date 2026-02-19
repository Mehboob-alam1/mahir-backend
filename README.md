# Demoapp - Spring Boot 3 Backend

REST API backend with Spring Boot 3, Java 17, Maven and MySQL. Layered architecture: Controller, Service, Repository, Entity.

## Requirements

- **Java 17**
- **Maven 3.6+** (or use the included `./mvnw` wrapper)
- **MySQL 8** (running on `localhost:3306`) — optional if you use the H2 profile

## Database configuration

Before starting the application with MySQL, create the database:

```sql
CREATE DATABASE IF NOT EXISTS demoapp_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Configuration in `application.properties`:

- **Database:** `demoapp_db`
- **Username:** `root`
- **Password:** `root`
- **Port:** `3306`

Change username/password in `src/main/resources/application.properties` if your MySQL uses different credentials.

## How to run the project

### From the terminal (Maven)

**With MySQL** (default; MySQL must be running and `demoapp_db` created):

```bash
cd demoapp
./mvnw spring-boot:run
```

**Without MySQL** (in-memory H2 database; useful when MySQL is not set up yet):

```bash
cd demoapp
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

The API works the same; data is lost when the app stops. H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:demoapp_db`, user: `sa`, password: empty).

**If the app exits with "Process terminated with exit code: 1"**, see the actual error with:

```bash
./mvnw spring-boot:run -e
```

Often the cause is MySQL not running or the database not created. Use the `h2` profile above to run without MySQL.

Or, if you have Maven installed globally:

```bash
cd demoapp
mvn clean spring-boot:run
```

### From an IDE

1. Open the project as a Maven project.
2. Run the main class `com.example.demoapp.DemoappApplication`.

The application will be available at **http://localhost:8080**.

## Available API endpoints

| Method | Endpoint           | Description      |
|--------|--------------------|------------------|
| POST   | `/api/users`       | Create a user     |
| GET    | `/api/users`       | List all users    |
| GET    | `/api/users/{id}`  | Get user by ID    |
| PUT    | `/api/users/{id}`  | Update a user     |
| DELETE | `/api/users/{id}`  | Delete a user     |

## Example JSON request — Create user

**POST** `http://localhost:8080/api/users`

**Headers:**
```
Content-Type: application/json
```

**Body (example):**

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

**Example response (201 Created):**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": "2025-02-19T10:30:00"
}
```

### More body examples

```json
{
  "name": "Jane Smith",
  "email": "jane.smith@example.com"
}
```

Validation rules:
- `name`: required, max 100 characters
- `email`: required, valid email format, max 255 characters

## Project structure

```
src/main/java/com/example/demoapp/
├── DemoappApplication.java
├── controller/
│   └── UserController.java
├── dto/
│   ├── UserRequest.java
│   └── UserResponse.java
├── entity/
│   └── User.java
├── exception/
│   ├── DuplicateResourceException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   └── UserRepository.java
└── service/
    └── UserService.java
```

## Error handling

- **404** — User not found
- **409** — Email already in use (duplicate)
- **400** — Validation errors (required fields, email format, etc.)
- **500** — Internal error (handled by `GlobalExceptionHandler`)

## Build JAR

```bash
./mvnw clean package
```

The runnable JAR will be at `target/demoapp-1.0.0-SNAPSHOT.jar`. Run it with:

```bash
java -jar target/demoapp-1.0.0-SNAPSHOT.jar
```

Make sure MySQL is running and the `demoapp_db` database exists before starting the application (or use the `h2` profile).
