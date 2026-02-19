# Demoapp - Spring Boot 3 Backend

Backend REST API con Spring Boot 3, Java 17, Maven e MySQL. Architettura a livelli: Controller, Service, Repository, Entity.

## Requisiti

- **Java 17**
- **Maven 3.6+**
- **MySQL 8** (in esecuzione su `localhost:3306`)

## Configurazione database

Prima di avviare l'applicazione, crea il database MySQL:

```sql
CREATE DATABASE IF NOT EXISTS demoapp_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Configurazione in `application.properties`:

- **Database:** `demoapp_db`
- **Username:** `root`
- **Password:** `root`
- **Porta:** `3306`

Modifica username/password in `src/main/resources/application.properties` se il tuo MySQL usa credenziali diverse.

## Come eseguire il progetto

### Da terminale (Maven)

**With MySQL** (default; MySQL must be running and `demoapp_db` created):

```bash
cd demoapp
./mvnw spring-boot:run
```

**Without MySQL** (in-memory H2 database; useful if MySQL is not set up yet):

```bash
cd demoapp
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

The API works the same; data is lost when the app stops. H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:demoapp_db`, user: `sa`, password: empty).

**If the app exits with "Process terminated with exit code: 1"**, see the real error with:

```bash
./mvnw spring-boot:run -e
```

Often the cause is MySQL not running or the database not created. Use the `h2` profile above to run without MySQL.

Oppure, se hai Maven installato globalmente:

```bash
cd demoapp
mvn clean spring-boot:run
```

### Da IDE

1. Apri il progetto come progetto Maven.
2. Esegui la classe `com.example.demoapp.DemoappApplication`.

L'applicazione sarà disponibile su **http://localhost:8080**.

## API disponibili

| Metodo | Endpoint           | Descrizione        |
|--------|--------------------|--------------------|
| POST   | `/api/users`       | Crea un utente     |
| GET    | `/api/users`       | Elenco utenti      |
| GET    | `/api/users/{id}`  | Dettaglio utente   |
| PUT    | `/api/users/{id}`  | Aggiorna utente    |
| DELETE | `/api/users/{id}`  | Elimina utente     |

## Esempio richiesta JSON - Creazione utente

**POST** `http://localhost:8080/api/users`

**Headers:**
```
Content-Type: application/json
```

**Body (esempio):**

```json
{
  "name": "Mario Rossi",
  "email": "mario.rossi@example.com"
}
```

**Risposta di esempio (201 Created):**

```json
{
  "id": 1,
  "name": "Mario Rossi",
  "email": "mario.rossi@example.com",
  "createdAt": "2025-02-19T10:30:00"
}
```

### Altri esempi di body

```json
{
  "name": "Laura Bianchi",
  "email": "laura.bianchi@example.com"
}
```

La validazione richiede:
- `name`: non vuoto, max 100 caratteri
- `email`: non vuota, formato email valido, max 255 caratteri

## Struttura del progetto

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

## Gestione errori

- **404** – Utente non trovato
- **409** – Email già presente (duplicato)
- **400** – Errori di validazione (campi obbligatori, formato email, ecc.)
- **500** – Errore interno (gestito da `GlobalExceptionHandler`)

## Build JAR

```bash
./mvnw clean package
```

Il JAR eseguibile sarà in `target/demoapp-1.0.0-SNAPSHOT.jar`. Esecuzione:

```bash
java -jar target/demoapp-1.0.0-SNAPSHOT.jar
```

Assicurati che MySQL sia avviato e che il database `demoapp_db` esista prima di lanciare l'applicazione.
