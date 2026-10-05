# Resource Booking System

A production-style **Backend Developer Assignment** project: a RESTful API for booking
bookable **Resources** (Rooms, Vehicles, Equipment) with JWT-based authentication and
role-based authorization (ADMIN / USER).

---

## 1. Project Description

Users can log in, browse available resources, and create reservations. Each user can
only see and manage their own reservations. Admins have full CRUD control over
resources and can view/manage every reservation in the system. The API prevents
double-booking of a resource for overlapping time windows.

## 2. Features

- JWT authentication (stateless, no server-side sessions)
- Role-based access control (ADMIN, USER)
- Full CRUD for Resources (ADMIN only for write operations)
- Full CRUD for Reservations, with strict **ownership enforcement**
- User identity always derived from the JWT — never from client-supplied `userId`
- Reservation overlap / double-booking prevention (409 CONFLICT)
- Filtering (`status`, `minPrice`, `maxPrice`) via JPA Specifications
- Pagination and sorting on list endpoints
- Centralized validation (Jakarta Bean Validation) and exception handling
- Swagger / OpenAPI UI with a "Bearer token" Authorize button
- BCrypt password hashing, seeded ADMIN + USER accounts
- JUnit 5 + Mockito + MockMvc test suite (H2 in-memory DB — no MySQL needed to run tests)
- Postman collection with auto-saving login tokens

## 3. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build tool | Maven |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL (H2 for tests) |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| Utilities | Lombok, Jakarta Bean Validation |

## 4. Architecture

Classic layered architecture, kept intentionally simple:

```
Controller  ->  Service  ->  Repository  ->  Database
     |             |
     |             +--> business rules, ownership checks, overlap checks
     |
     +--> HTTP mapping only, no business logic
```

Security flow:

```
Request
  |
  v
JwtAuthenticationFilter (OncePerRequestFilter)
  |  - reads "Authorization: Bearer <token>"
  |  - validates signature + expiry
  |  - extracts username + role
  v
SecurityContextHolder (Authentication set)
  |
  v
@PreAuthorize checks on controller methods
  |
  v
Controller -> Service -> Repository
```

## 5. Project Structure

```
resource-booking-system/
├── pom.xml
├── database.sql
├── README.md
├── .gitignore
├── Resource-Booking-System.postman_collection.json
├── src/main/java/com/example/resourcebooking/
│   ├── ResourceBookingApplication.java
│   ├── config/          (DataInitializer, OpenApiConfig)
│   ├── controller/      (Auth, Resource, Reservation, User controllers)
│   ├── dto/             (request/response DTOs)
│   ├── entity/          (User, Resource, Reservation)
│   ├── enums/           (Role, ReservationStatus)
│   ├── repository/      (Spring Data JPA repositories)
│   ├── service/         (business logic)
│   ├── security/        (JWT filter/service, SecurityConfig, UserDetailsService)
│   ├── exception/       (custom exceptions + GlobalExceptionHandler)
│   └── specification/   (ReservationSpecification for dynamic filtering)
├── src/main/resources/application.properties
└── src/test/java/...    (MockMvc integration tests, H2-backed)
```

## 6. Database Setup

1. Install/start MySQL locally.
2. Run `database.sql` (or just let Hibernate auto-create everything — see below):

```sql
CREATE DATABASE IF NOT EXISTS resource_booking_db;
```

3. `spring.jpa.hibernate.ddl-auto=update` means Hibernate will create/update the
   `users`, `resources`, and `reservations` tables automatically on first run — you do
   not need to write `CREATE TABLE` statements yourself.

## 7. MySQL Configuration

Default connection (edit in `application.properties` or override with environment
variables — see below):

```
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

## 8. Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/resource_booking_db...` | JDBC URL |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `JWT_SECRET` | `change-this-secret...` | HMAC signing secret — **override this in any real deployment** |
| `JWT_EXPIRATION` | `86400000` (24h, ms) | Token lifetime |

Set them in your shell, IDE run configuration, or a `.env`/OS-level mechanism before
starting the app.

## 9. How to Import into Eclipse

1. `File` → `Import…` → `Maven` → `Existing Maven Projects`
2. Browse to the `resource-booking-system` folder and select it.
3. Click `Finish`. Eclipse will download dependencies and configure the project.
4. Right-click the project → `Maven` → `Update Project…` (check "Force Update") if
   anything looks out of sync.
5. Ensure the project's Java build path uses **JDK 17** (Right-click project →
   `Properties` → `Java Build Path` / `Java Compiler`).

## 10. How to Run the Application

**From Eclipse:**
Right-click `ResourceBookingApplication.java` → `Run As` → `Spring Boot App`
(or `Java Application`).

**From the command line:**
```bash
mvn clean install
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

On first startup, `DataInitializer` seeds the ADMIN/USER accounts and sample
resources/reservations automatically — no manual data entry needed.

## 11. Seed Users

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

## 12. API Endpoints

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/login` | Public |

### Resources
| Method | Endpoint | Access |
|---|---|---|
| POST | `/resources` | ADMIN |
| GET | `/resources` | ADMIN, USER |
| GET | `/resources/{id}` | ADMIN, USER |
| PUT | `/resources/{id}` | ADMIN |
| DELETE | `/resources/{id}` | ADMIN |

### Reservations
| Method | Endpoint | Access |
|---|---|---|
| POST | `/reservations` | ADMIN, USER |
| GET | `/reservations` | ADMIN (all), USER (own only) |
| GET | `/reservations/{id}` | ADMIN (any), USER (own only) |
| PUT | `/reservations/{id}` | ADMIN (any), USER (own only) |
| DELETE | `/reservations/{id}` | ADMIN (any), USER (own only) |

### Users
| Method | Endpoint | Access |
|---|---|---|
| GET | `/users` | ADMIN |
| GET | `/users/{id}` | ADMIN |

## 13. Authentication Flow

1. `POST /auth/login` with `{ "username": "...", "password": "..." }`.
2. Server verifies credentials with `AuthenticationManager` + `BCryptPasswordEncoder`.
3. On success, a JWT is issued containing `sub` (username), `role`, `iat`, `exp`.
4. Client sends `Authorization: Bearer <token>` on every subsequent request.
5. `JwtAuthenticationFilter` validates the token and populates the
   `SecurityContext` for that request only (stateless — nothing stored server-side).

## 14. Authorization Rules

- Enforced with `@PreAuthorize("hasRole('ADMIN')")` /
  `@PreAuthorize("hasAnyRole('ADMIN','USER')")` on controller methods.
- Additionally enforced **again in the service layer** for reservation ownership
  (defense in depth — never trust only endpoint-level annotations).

## 15. Reservation Ownership Logic

- `ReservationRequest` deliberately has **no `userId` field**.
- The authenticated user is resolved from `Authentication` (JWT principal) via
  `UserService.getCurrentUser(...)` in every controller method that touches
  reservations.
- `ReservationService.assertOwnershipOrAdmin(...)` throws `AccessDeniedException`
  (→ 403) whenever a non-owner, non-admin tries to read/update/delete a reservation.
- List endpoints (`GET /reservations`) silently scope the query to
  `WHERE user_id = <current user>` for regular users — there is no way to pass a
  query parameter to see someone else's data.

## 16. Filtering

`GET /reservations` supports optional query parameters, combinable freely:

```
GET /reservations?status=CONFIRMED
GET /reservations?minPrice=1000
GET /reservations?maxPrice=5000
GET /reservations?status=CONFIRMED&minPrice=1000&maxPrice=5000
```

Implemented with a single `ReservationSpecification` (JPA Criteria API) instead of
one repository method per filter combination.

## 17. Pagination

```
GET /reservations?page=0&size=10
```
Response shape:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

## 18. Sorting

```
GET /reservations?page=0&size=10&sort=price,desc
GET /reservations?sort=createdAt,desc
GET /reservations?sort=startTime,asc
```
Default sort (when omitted) is `createdAt,desc`.

## 19. Swagger Usage

1. Start the app.
2. Open `http://localhost:8080/swagger-ui/index.html`.
3. Call `POST /auth/login`, copy the `token` value from the response.
4. Click **Authorize** (top right), enter `Bearer <token>`, click **Authorize**, then
   **Close**.
5. All protected endpoints can now be called directly from Swagger UI.

## 20. Postman Usage

1. Import `Resource-Booking-System.postman_collection.json` into Postman.
2. Run **Auth → Login Admin** and **Auth → Login User** once each — their test
   scripts automatically save the returned JWTs into the `adminToken` /
   `userToken` collection variables.
3. Every other request already references `{{adminToken}}` / `{{userToken}}` in its
   `Authorization` header, so no manual copy-pasting is required.

## 21. Testing

```bash
mvn test
```

Tests run against an in-memory H2 database (`src/test/resources/application-test.properties`)
so no MySQL instance is required to run the test suite. Coverage includes login
success/failure, RBAC on resources, reservation ownership, overlap rejection,
validation, filtering, pagination, sorting, and 401/403 behavior.

## 22. Sample API Requests/Responses

**Login**
```http
POST /auth/login
{ "username": "admin", "password": "admin123" }
```
```json
{ "token": "eyJhbGciOi...", "username": "admin", "role": "ADMIN" }
```

**Create Reservation**
```http
POST /reservations
Authorization: Bearer <userToken>
{
  "resourceId": 1,
  "startTime": "2026-10-05T10:00:00",
  "endTime": "2026-10-05T12:00:00",
  "price": 3000.00
}
```
```json
{
  "id": 5,
  "resourceId": 1,
  "resourceName": "Conference Room A",
  "userId": 2,
  "username": "user",
  "startTime": "2026-10-05T10:00:00",
  "endTime": "2026-10-05T12:00:00",
  "price": 3000.00,
  "status": "PENDING",
  "createdAt": "2026-09-17T14:00:00"
}
```

## 23. Error Handling

All errors return a consistent JSON body via `GlobalExceptionHandler`:
```json
{
  "timestamp": "2026-09-17T14:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 10",
  "path": "/resources/10"
}
```

## 24. Troubleshooting

| Problem | Likely Cause | Fix |
|---|---|---|
| `Communications link failure` | MySQL not running / wrong port | Start MySQL, check `DB_URL` |
| `Access denied for user` | Wrong DB credentials | Check `DB_USERNAME` / `DB_PASSWORD` |
| `401 Unauthorized` on every request | Missing/expired/invalid JWT | Re-login, check `Authorization: Bearer <token>` header |
| `403 Forbidden` | Wrong role for the endpoint, or accessing another user's reservation | Use the correct account/role |
| Swagger shows only public endpoints working | Not authorized in Swagger UI | Click **Authorize** and paste `Bearer <token>` |
| Port 8080 already in use | Another app on that port | `server.port=8081` in `application.properties` |

## 25. Future Improvements

- Refresh tokens / token revocation list
- Pagination-aware `/users` endpoint
- Soft-delete for resources/reservations
- Rate limiting on `/auth/login`
- Dockerfile + docker-compose for MySQL + app
- Role: MANAGER with resource-scoped permissions

---

## How to Explain This Project in an Interview

**Objective:** A REST API that lets employees book shared resources (rooms,
vehicles, equipment) while preventing double-booking, with two roles: a regular
user who manages only their own bookings, and an admin who manages everything.

**Why Spring Boot:** Convention-over-configuration, mature ecosystem for REST +
security + JPA, fast to build a clean layered service.

**Why JWT:** Stateless auth scales horizontally (no server-side session store),
works naturally for a REST API consumed by SPAs/mobile clients, and easily carries
the user's role as a claim so authorization decisions don't need a DB round-trip
per request.

**Authentication flow:** Username/password → `AuthenticationManager` verifies via
`BCryptPasswordEncoder` → JWT issued with `sub`=username, `role` claim, and
expiry → client stores and sends it as a Bearer token on every request →
`JwtAuthenticationFilter` validates it once per request and populates Spring
Security's context.

**Authorization flow (ADMIN vs USER):** `@PreAuthorize` on each controller
method declares which role(s) may call it. For anything involving "my data" vs
"everyone's data" (reservations), the service layer adds a second check based on
resource ownership — so even a bug in the annotation wouldn't leak data.

**How JWT identifies the user:** The filter extracts the `sub` claim (username)
from a *validated* token and sets it as the Spring Security `Authentication`
principal. Every downstream service call resolves "current user" from that
`Authentication` object — the request body's JSON is never trusted for identity.

**How reservation ownership is enforced:** `ReservationRequest` has no `userId`
field at all — it can't be spoofed because it doesn't exist. Reads/updates/deletes
compare `reservation.user.id` to the JWT-resolved user's id and throw
`AccessDeniedException` (403) on mismatch, unless the caller is ADMIN.

**How JPA relationships work:** `User 1—* Reservation` and `Resource 1—*
Reservation`, modeled with `@OneToMany(mappedBy=...)` on the "one" side and
`@ManyToOne @JoinColumn` on the "many" side. `@JsonIgnore` on the collection sides
prevents circular JSON serialization; DTOs are used for all API responses anyway,
so entities never leak directly.

**How filtering works:** A single `ReservationSpecification` builds a dynamic
JPA Criteria `WHERE` clause from whichever of `status` / `minPrice` / `maxPrice`
(plus, for regular users, an implicit `userId`) are present, instead of writing a
combinatorial explosion of repository methods.

**How pagination works:** Spring Data's `Pageable`/`Page<T>` abstraction, bound
automatically from `page`/`size` query params, wrapped into a small
`PageResponse` DTO for a predictable JSON shape.

**How sorting works:** Also part of `Pageable` — Spring parses `sort=field,dir`
query params into a `Sort` object automatically; a sensible default
(`createdAt,desc`) is applied when the client omits it.

**How booking conflict is detected:** A repository query finds any active
(`PENDING`/`CONFIRMED`) reservation on the same resource whose interval overlaps
the requested interval using the classic
`existing.start < newEnd AND existing.end > newStart` check, returning `409
CONFLICT` if any match.

**How validation works:** Jakarta Bean Validation annotations
(`@NotNull`, `@NotBlank`, `@PositiveOrZero`) on DTOs, triggered by `@Valid` in
controllers; a custom `startTime < endTime` business rule lives in the service
layer since it spans two fields.

**How exception handling works:** A single `@RestControllerAdvice`
(`GlobalExceptionHandler`) maps each exception type (not found, bad request,
conflict, access denied, validation, generic) to the right HTTP status and a
consistent JSON error body.

**How BCrypt works:** A one-way, salted adaptive hash function — passwords are
never stored or compared in plain text; `BCryptPasswordEncoder.matches()` re-hashes
the input with the stored salt and compares digests.

**How MySQL is connected:** Standard Spring Data JPA/Hibernate setup via
`spring.datasource.*` properties (overridable with environment variables) and
`ddl-auto=update` for schema management during development.

**How Swagger is used:** `springdoc-openapi` auto-generates the OpenAPI spec from
the controllers/DTOs; a `SecurityScheme` bean adds a "Bearer token" Authorize
button so protected endpoints can be tested directly from the browser.

**How tests are written:** `@SpringBootTest` + `MockMvc` integration tests
against a real (H2 in-memory) database exercise the full stack — controller,
security filter, service, repository — for the scenarios that matter most:
auth, RBAC, ownership, overlap, validation, filtering, pagination, sorting, and
401/403 behavior.
