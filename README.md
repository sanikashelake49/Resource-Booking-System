# 🚀 Resource Booking System

A secure, production-style **RESTful Resource Booking System** built with **Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, and MySQL**.

The application provides APIs for managing bookable resources such as **meeting rooms, vehicles, and equipment**. Users can authenticate, view available resources, create reservations, and manage their own bookings. Administrators have additional privileges to manage resources, view users, and manage all reservations.

The project focuses on important backend development concepts including **JWT authentication, role-based authorization, reservation ownership, double-booking prevention, dynamic filtering, pagination, sorting, validation, centralized exception handling, Swagger/OpenAPI documentation, and automated testing**.

---

# 📌 Table of Contents

* [Project Overview](#-project-overview)
* [Problem Statement](#-problem-statement)
* [Key Features](#-key-features)
* [User Roles](#-user-roles)
* [Technology Stack](#-technology-stack)
* [Architecture](#-architecture)
* [Project Structure](#-project-structure)
* [Application Flow](#-application-flow)
* [Authentication Flow](#-authentication-flow)
* [Authorization](#-authorization)
* [Reservation Ownership Security](#-reservation-ownership-security)
* [Double-Booking Prevention](#-double-booking-prevention)
* [Database Design](#-database-design)
* [Entity Relationships](#-entity-relationships)
* [API Endpoints](#-api-endpoints)
* [Authentication API](#-authentication-api)
* [Resource APIs](#-resource-apis)
* [Reservation APIs](#-reservation-apis)
* [User APIs](#-user-apis)
* [Filtering](#-filtering)
* [Pagination](#-pagination)
* [Sorting](#-sorting)
* [Validation](#-validation)
* [Exception Handling](#-exception-handling)
* [Swagger / OpenAPI](#-swagger--openapi)
* [Postman Collection](#-postman-collection)
* [Sample Data](#-sample-data)
* [Database Setup](#-database-setup)
* [Configuration](#-configuration)
* [Running the Application](#-running-the-application)
* [Testing](#-testing)
* [HTTP Status Codes](#-http-status-codes)
* [Security Highlights](#-security-highlights)
* [Important Business Rules](#-important-business-rules)
* [Future Improvements](#-future-improvements)
* [Resume Description](#-resume-description)
* [Interview Explanation](#-interview-explanation)
* [Author](#-author)

---

# 📖 Project Overview

The **Resource Booking System** is a backend REST API designed to manage shared resources and their reservations.

Examples of resources include:

* Conference rooms
* Company vehicles
* Projectors
* Other shared equipment

The system provides two types of users:

### ADMIN

Administrators can:

* View all users
* View individual users
* Create resources
* View resources
* Update resources
* Delete resources
* Create reservations
* View all reservations
* View any reservation
* Update any reservation
* Delete any reservation
* Change reservation status

### USER

Regular users can:

* Login
* View resources
* View individual resources
* Create reservations
* View their own reservations
* Update their own reservations
* Delete their own reservations

Users cannot access or modify another user's reservation.

---

# 🎯 Problem Statement

Organizations often have shared resources such as meeting rooms, vehicles, projectors, and other equipment.

Without a centralized booking system, several problems can occur:

* Multiple users may book the same resource at the same time.
* Users may access other users' bookings.
* There may be no proper authentication.
* Resource management may be difficult.
* Reservation information may be inconsistent.
* APIs may not have proper validation or error handling.

This project solves these problems by providing a secure backend API with:

* JWT authentication
* Role-based access control
* Reservation ownership validation
* Double-booking prevention
* Resource management
* Filtering
* Pagination
* Sorting
* Validation
* Centralized exception handling

---

# ✨ Key Features

## 🔐 Authentication

* JWT-based authentication
* Stateless authentication
* Spring Security 6
* BCrypt password hashing
* Username/password login
* JWT expiration
* Bearer token authentication

---

## 👥 Role-Based Authorization

Two roles are supported:

```text
ADMIN
USER
```

Access to API endpoints is controlled using Spring Security method-level authorization.

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
```

and:

```java
@PreAuthorize("hasAnyRole('ADMIN','USER')")
```

---

## 📦 Resource Management

Administrators can perform complete CRUD operations on resources.

A resource contains:

* ID
* Name
* Description
* Type
* Location
* Price
* Availability
* Created timestamp

Examples:

```text
Conference Room A
Conference Room B
Company Car
Projector
```

---

## 📅 Reservation Management

Users can create reservations for resources.

Each reservation contains:

* Reservation ID
* Resource
* User
* Start time
* End time
* Price
* Status
* Created timestamp

Supported reservation statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

---

## 🚫 Double-Booking Prevention

The system prevents overlapping active reservations for the same resource.

Active reservation statuses are:

```text
PENDING
CONFIRMED
```

Cancelled reservations do not block a resource.

The overlap condition used by the application is:

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

If an overlap is detected:

```text
409 CONFLICT
```

is returned.

---

## 🔎 Dynamic Filtering

Reservations can be filtered using:

* Status
* Minimum price
* Maximum price

Filters can also be combined.

Example:

```http
GET /reservations?status=CONFIRMED&minPrice=1000&maxPrice=5000
```

Filtering is implemented using:

```text
Spring Data JPA Specification
```

This avoids creating separate repository methods for every possible filter combination.

---

## 📄 Pagination

Resources and reservations support pagination.

Example:

```http
GET /resources?page=0&size=10
```

```http
GET /reservations?page=0&size=10
```

The application returns a custom `PageResponse` containing:

```text
content
page
size
totalElements
totalPages
```

---

## ↕️ Sorting

Spring Data `Pageable` is used for sorting.

Examples:

```http
GET /reservations?sort=price,desc
```

```http
GET /reservations?sort=startTime,asc
```

```http
GET /resources?sort=createdAt,desc
```

Default pagination:

```text
page = 0
size = 10
sort = createdAt
```

---

# 👤 User Roles

| Feature                           | ADMIN | USER |
| --------------------------------- | :---: | :--: |
| Login                             |   ✅   |   ✅  |
| View resources                    |   ✅   |   ✅  |
| View resource by ID               |   ✅   |   ✅  |
| Create resource                   |   ✅   |   ❌  |
| Update resource                   |   ✅   |   ❌  |
| Delete resource                   |   ✅   |   ❌  |
| Create reservation                |   ✅   |   ✅  |
| View all reservations             |   ✅   |   ❌  |
| View own reservations             |   ✅   |   ✅  |
| View another user's reservation   |   ✅   |   ❌  |
| Update own reservation            |   ✅   |   ✅  |
| Update another user's reservation |   ✅   |   ❌  |
| Delete own reservation            |   ✅   |   ✅  |
| Delete another user's reservation |   ✅   |   ❌  |
| View all users                    |   ✅   |   ❌  |
| View user by ID                   |   ✅   |   ❌  |
| Change reservation status         |   ✅   |   ❌  |

---

# 🛠️ Technology Stack

| Category             | Technology              |
| -------------------- | ----------------------- |
| Programming Language | Java 17                 |
| Framework            | Spring Boot 3.3.4       |
| Build Tool           | Maven                   |
| Web Layer            | Spring Web / REST       |
| Security             | Spring Security 6       |
| Authentication       | JWT                     |
| JWT Library          | JJWT 0.12.6             |
| Password Hashing     | BCrypt                  |
| ORM                  | Hibernate               |
| Persistence          | Spring Data JPA         |
| Database             | MySQL                   |
| Test Database        | H2                      |
| Validation           | Jakarta Bean Validation |
| API Documentation    | Swagger / OpenAPI       |
| Testing              | JUnit 5                 |
| Mocking              | Mockito                 |
| Web Testing          | MockMvc                 |
| Utility              | Lombok                  |

---

# 🏗️ Architecture

The project follows a layered backend architecture.

```text
                    Client
                      |
                      v
              ┌───────────────┐
              │   Controller  │
              └───────┬───────┘
                      |
                      v
              ┌───────────────┐
              │    Service    │
              │ Business Logic│
              └───────┬───────┘
                      |
                      v
              ┌───────────────┐
              │   Repository  │
              │  Spring Data  │
              │      JPA      │
              └───────┬───────┘
                      |
                      v
              ┌───────────────┐
              │     MySQL     │
              └───────────────┘
```

---

# 📂 Project Structure

```text
resource-booking-system/
│
├── pom.xml
├── database.sql
├── README.md
├── .gitignore
├── Resource-Booking-System.postman_collection.json
│
└── src/
    │
    ├── main/
    │   │
    │   ├── java/
    │   │   └── com/example/resourcebooking/
    │   │
    │   │       ├── ResourceBookingApplication.java
    │   │
    │   │       ├── config/
    │   │       │   ├── DataInitializer.java
    │   │       │   └── OpenApiConfig.java
    │   │
    │   │       ├── controller/
    │   │       │   ├── AuthController.java
    │   │       │   ├── ResourceController.java
    │   │       │   ├── ReservationController.java
    │   │       │   └── UserController.java
    │   │
    │   │       ├── dto/
    │   │       │   ├── ErrorResponse.java
    │   │       │   ├── LoginRequest.java
    │   │       │   ├── LoginResponse.java
    │   │       │   ├── PageResponse.java
    │   │       │   ├── ReservationRequest.java
    │   │       │   ├── ReservationResponse.java
    │   │       │   ├── ResourceRequest.java
    │   │       │   ├── ResourceResponse.java
    │   │       │   └── UserResponse.java
    │   │
    │   │       ├── entity/
    │   │       │   ├── User.java
    │   │       │   ├── Resource.java
    │   │       │   └── Reservation.java
    │   │
    │   │       ├── enums/
    │   │       │   ├── Role.java
    │   │       │   └── ReservationStatus.java
    │   │
    │   │       ├── repository/
    │   │       │   ├── UserRepository.java
    │   │       │   ├── ResourceRepository.java
    │   │       │   └── ReservationRepository.java
    │   │
    │   │       ├── service/
    │   │       │   ├── AuthService.java
    │   │       │   ├── UserService.java
    │   │       │   ├── ResourceService.java
    │   │       │   └── ReservationService.java
    │   │
    │   │       ├── security/
    │   │       │   ├── JwtService.java
    │   │       │   ├── JwtAuthenticationFilter.java
    │   │       │   ├── SecurityConfig.java
    │   │       │   └── CustomUserDetailsService.java
    │   │
    │   │       ├── exception/
    │   │       │   ├── BadRequestException.java
    │   │       │   ├── BookingConflictException.java
    │   │       │   ├── ResourceNotFoundException.java
    │   │       │   ├── ReservationNotFoundException.java
    │   │       │   ├── UserNotFoundException.java
    │   │       │   ├── UnauthorizedException.java
    │   │       │   └── GlobalExceptionHandler.java
    │   │
    │   │       └── specification/
    │   │           └── ReservationSpecification.java
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        │
        ├── java/
        │   └── com/example/resourcebooking/
        │       └── ResourceBookingApplicationTests.java
        │
        └── resources/
            └── application-test.properties
```

---

# 🔄 Application Flow

The general application flow is:

```text
Client
  |
  | POST /auth/login
  v
Authentication
  |
  | JWT Token
  v
Authenticated Request
  |
  | Authorization: Bearer <JWT>
  v
JWT Filter
  |
  v
SecurityContext
  |
  v
Role Authorization
  |
  v
Controller
  |
  v
Service
  |
  v
Repository
  |
  v
MySQL
```

---

# 🔐 Authentication Flow

The application uses stateless JWT authentication.

## Step 1 — Login

Client sends:

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "username": "user",
  "password": "user123"
}
```

---

## Step 2 — Credential Verification

`AuthService` uses:

```text
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
CustomUserDetailsService
        ↓
UserRepository
```

The password is verified using:

```text
BCryptPasswordEncoder
```

---

## Step 3 — JWT Generation

After successful authentication, `JwtService` creates a JWT containing:

```text
subject = username
role
issuedAt
expiration
```

The response contains:

```json
{
  "token": "JWT_TOKEN",
  "username": "user",
  "role": "USER"
}
```

---

# 🔎 JWT Request Flow

For every protected request, the client sends:

```http
Authorization: Bearer <JWT_TOKEN>
```

The request passes through:

```text
JwtAuthenticationFilter
        |
        v
Extract Bearer Token
        |
        v
Extract Username
        |
        v
Validate Signature
        |
        v
Validate Expiration
        |
        v
Load User
        |
        v
Create Authentication
        |
        v
SecurityContextHolder
```

The request then reaches the controller.

---

# 🛡️ Authorization

The application uses Spring Security method-level authorization.

For example:

```java
@PreAuthorize("hasRole('ADMIN')")
```

allows only administrators.

For both roles:

```java
@PreAuthorize("hasAnyRole('ADMIN','USER')")
```

is used.

The application also uses:

```java
@EnableMethodSecurity
```

to enable method-level authorization.

---

# 🔒 Stateless Security

The application does not use server-side HTTP sessions.

Security configuration uses:

```java
SessionCreationPolicy.STATELESS
```

Therefore:

```text
Login
  ↓
JWT generated
  ↓
Client stores JWT
  ↓
JWT sent with every protected request
```

The server validates the token for each request.

---

# 👤 Reservation Ownership Security

This is one of the most important security features of the project.

The `ReservationRequest` intentionally does **not** contain a `userId`.

The request contains:

```json
{
  "resourceId": 1,
  "startTime": "2026-10-10T10:00:00",
  "endTime": "2026-10-10T12:00:00",
  "price": 1500
}
```

There is no:

```json
"userId": 2
```

The server determines the user from the authenticated JWT.

The flow is:

```text
JWT
 ↓
Username
 ↓
Authentication.getName()
 ↓
UserRepository
 ↓
Current User
 ↓
Reservation Owner
```

This prevents users from spoofing another user's identity.

---

# 🔐 Defense-in-Depth Authorization

Authorization is enforced at two levels.

### Controller Level

```java
@PreAuthorize("hasAnyRole('ADMIN','USER')")
```

### Service Level

The service checks:

```text
Is current user the reservation owner?
OR
Is current user an ADMIN?
```

If neither condition is true:

```text
403 FORBIDDEN
```

This is important because business-level ownership should not depend only on controller annotations.

---

# 🚫 Double-Booking Prevention

Before creating a reservation, the service validates:

### 1. Start time

```text
startTime < endTime
```

If not:

```text
400 BAD REQUEST
```

---

### 2. Resource existence

The resource must exist.

If not:

```text
404 NOT FOUND
```

---

### 3. Existing overlapping reservations

The repository checks:

```text
resource ID
+
requested start time
+
requested end time
+
active reservation statuses
```

Active statuses:

```text
PENDING
CONFIRMED
```

The query uses:

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

If a matching reservation exists:

```text
409 CONFLICT
```

with:

```text
Resource is already booked for an overlapping time range
```

---

# 🔄 Reservation Update Conflict Check

The same overlap logic is also applied when updating an existing reservation.

During an update, the current reservation ID is excluded from the overlap query.

This prevents a reservation from conflicting with itself.

---

# 📊 Reservation Filtering

The endpoint:

```http
GET /reservations
```

supports:

### Status

```http
GET /reservations?status=PENDING
```

```http
GET /reservations?status=CONFIRMED
```

```http
GET /reservations?status=CANCELLED
```

### Minimum Price

```http
GET /reservations?minPrice=1000
```

### Maximum Price

```http
GET /reservations?maxPrice=5000
```

### Combined

```http
GET /reservations?status=CONFIRMED&minPrice=1000&maxPrice=5000
```

---

# ⚙️ JPA Specification

Dynamic filtering is implemented using:

```java
JpaSpecificationExecutor<Reservation>
```

and:

```text
ReservationSpecification
```

The specification dynamically creates conditions for:

```text
status
minPrice
maxPrice
ownerUserId
```

For ADMIN:

```text
ownerUserId = null
```

Therefore the admin can view all reservations.

For USER:

```text
ownerUserId = currentUser.id
```

Therefore only that user's reservations are returned.

---

# 📄 Pagination

The application uses Spring Data `Pageable`.

Example:

```http
GET /reservations?page=0&size=10
```

Response:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

The custom `PageResponse<T>` provides a clean and predictable response format.

---

# ↕️ Sorting

Sorting is supported using Spring Data `Pageable`.

Examples:

```http
GET /reservations?sort=price,desc
```

```http
GET /reservations?sort=startTime,asc
```

```http
GET /reservations?sort=createdAt,desc
```

Resources also support pagination and sorting.

---

# 🗃️ Database Design

The project uses MySQL.

Three main tables are represented by JPA entities:

```text
users
resources
reservations
```

---

# 👤 Users Table

The `User` entity contains:

| Field     | Description                |
| --------- | -------------------------- |
| id        | Primary key                |
| username  | Unique username            |
| email     | Unique email               |
| password  | BCrypt hashed password     |
| role      | ADMIN / USER               |
| createdAt | Account creation timestamp |

Passwords are never stored as plain text.

---

# 📦 Resources Table

The `Resource` entity contains:

| Field       | Description          |
| ----------- | -------------------- |
| id          | Primary key          |
| name        | Resource name        |
| description | Resource description |
| type        | Resource type        |
| location    | Resource location    |
| price       | Resource price       |
| available   | Availability flag    |
| createdAt   | Creation timestamp   |

---

# 📅 Reservations Table

The `Reservation` entity contains:

| Field     | Description                     |
| --------- | ------------------------------- |
| id        | Primary key                     |
| resource  | Booked resource                 |
| user      | Reservation owner               |
| startTime | Booking start                   |
| endTime   | Booking end                     |
| price     | Reservation price               |
| status    | PENDING / CONFIRMED / CANCELLED |
| createdAt | Creation timestamp              |

---

# 🔗 Entity Relationships

The database relationship is:

```text
User
 |
 | 1
 |
 | *
 v
Reservation
 ^
 |
 | *
 |
 | 1
Resource
```

### User → Reservation

One user can have multiple reservations.

```java
@OneToMany(mappedBy = "user")
```

### Resource → Reservation

One resource can have multiple reservations at different times.

```java
@OneToMany(mappedBy = "resource")
```

### Reservation → User

Each reservation belongs to one user.

```java
@ManyToOne
```

### Reservation → Resource

Each reservation belongs to one resource.

```java
@ManyToOne
```

---

# 📡 API Endpoints

## Authentication

| Method | Endpoint      | Access |
| ------ | ------------- | ------ |
| POST   | `/auth/login` | Public |

---

# 📦 Resource APIs

| Method | Endpoint          | Access       |
| ------ | ----------------- | ------------ |
| POST   | `/resources`      | ADMIN        |
| GET    | `/resources`      | ADMIN / USER |
| GET    | `/resources/{id}` | ADMIN / USER |
| PUT    | `/resources/{id}` | ADMIN        |
| DELETE | `/resources/{id}` | ADMIN        |

---

# 📅 Reservation APIs

| Method | Endpoint             | Access       |
| ------ | -------------------- | ------------ |
| POST   | `/reservations`      | ADMIN / USER |
| GET    | `/reservations`      | ADMIN / USER |
| GET    | `/reservations/{id}` | ADMIN / USER |
| PUT    | `/reservations/{id}` | ADMIN / USER |
| DELETE | `/reservations/{id}` | ADMIN / USER |

Ownership rules are applied by the service layer.

---

# 👥 User APIs

| Method | Endpoint      | Access |
| ------ | ------------- | ------ |
| GET    | `/users`      | ADMIN  |
| GET    | `/users/{id}` | ADMIN  |

---

# 🔑 Authentication API

## Login

### Request

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Successful Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

---

# 📦 Resource API

## Create Resource

```http
POST /resources
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

Request:

```json
{
  "name": "Conference Room C",
  "description": "Meeting room with projector",
  "type": "ROOM",
  "location": "Pune HQ - 4th Floor",
  "price": 1200.00,
  "available": true
}
```

Successful response:

```text
201 CREATED
```

---

## Get Resources

```http
GET /resources
Authorization: Bearer <TOKEN>
```

With pagination:

```http
GET /resources?page=0&size=10
```

---

## Get Resource by ID

```http
GET /resources/1
Authorization: Bearer <TOKEN>
```

---

## Update Resource

```http
PUT /resources/1
Authorization: Bearer <ADMIN_TOKEN>
```

```json
{
  "name": "Conference Room A Updated",
  "description": "Updated meeting room",
  "type": "ROOM",
  "location": "Pune HQ - 2nd Floor",
  "price": 1800.00,
  "available": true
}
```

---

## Delete Resource

```http
DELETE /resources/1
Authorization: Bearer <ADMIN_TOKEN>
```

Response:

```text
204 NO CONTENT
```

---

# 📅 Reservation API

## Create Reservation

```http
POST /reservations
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

Request:

```json
{
  "resourceId": 1,
  "startTime": "2026-10-10T10:00:00",
  "endTime": "2026-10-10T12:00:00",
  "price": 3000.00
}
```

The authenticated user automatically becomes the reservation owner.

A USER cannot assign the reservation to another user.

---

## Admin Reservation Creation

An ADMIN may also provide a status:

```json
{
  "resourceId": 1,
  "startTime": "2026-10-10T10:00:00",
  "endTime": "2026-10-10T12:00:00",
  "price": 3000.00,
  "status": "CONFIRMED"
}
```

For a regular USER, the system forces a newly created reservation to:

```text
PENDING
```

---

# 📋 Get Reservations

```http
GET /reservations
Authorization: Bearer <TOKEN>
```

### ADMIN

ADMIN receives reservations for all users.

### USER

USER receives only their own reservations.

The application does not expose a `userId` query parameter that can be used to bypass this restriction.

---

# 🔍 Get Reservation by ID

```http
GET /reservations/1
Authorization: Bearer <TOKEN>
```

ADMIN can access any reservation.

USER can access only their own reservation.

---

# ✏️ Update Reservation

```http
PUT /reservations/1
Authorization: Bearer <TOKEN>
```

```json
{
  "resourceId": 2,
  "startTime": "2026-10-11T11:00:00",
  "endTime": "2026-10-11T13:00:00",
  "price": 2500.00
}
```

The system checks:

1. Reservation exists.
2. Current user owns the reservation or is ADMIN.
3. Start time is before end time.
4. Resource exists.
5. Requested time does not overlap another active reservation.

---

# 🗑️ Delete Reservation

```http
DELETE /reservations/1
Authorization: Bearer <TOKEN>
```

ADMIN can delete any reservation.

USER can delete only their own reservation.

Successful response:

```text
204 NO CONTENT
```

---

# ✅ Validation

The project uses Jakarta Bean Validation.

## Login Validation

Username:

```java
@NotBlank
```

Password:

```java
@NotBlank
```

---

## Resource Validation

Resource name:

```java
@NotBlank
```

Price:

```java
@NotNull
@PositiveOrZero
```

---

## Reservation Validation

Resource ID:

```java
@NotNull
```

Start time:

```java
@NotNull
```

End time:

```java
@NotNull
```

Price:

```java
@NotNull
@PositiveOrZero
```

Additionally, business logic verifies:

```text
startTime < endTime
```

---

# ⚠️ Exception Handling

The application uses custom exceptions and a centralized:

```java
@RestControllerAdvice
```

implemented through:

```text
GlobalExceptionHandler
```

Custom exceptions include:

```text
BadRequestException
BookingConflictException
ResourceNotFoundException
ReservationNotFoundException
UserNotFoundException
UnauthorizedException
```

Spring Security also handles access-denied scenarios.

---

# 📄 Standard Error Response

Errors use the following structure:

```json
{
  "timestamp": "2026-10-06T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 10",
  "path": "/resources/10"
}
```

This gives API consumers a consistent error format.

---

# 📊 HTTP Status Codes

| Status | Meaning                        |
| ------ | ------------------------------ |
| 200    | Successful request             |
| 201    | Resource created               |
| 204    | Successful deletion            |
| 400    | Bad request / validation error |
| 401    | Unauthorized                   |
| 403    | Forbidden                      |
| 404    | Resource not found             |
| 409    | Booking conflict               |

---

# 📖 Swagger / OpenAPI

The project includes Swagger UI using:

```text
springdoc-openapi
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI documentation:

```text
http://localhost:8080/v3/api-docs
```

---

# 🔐 Using Swagger Authentication

1. Start the application.
2. Open Swagger UI.
3. Execute:

```text
POST /auth/login
```

4. Copy the returned JWT.
5. Click **Authorize**.
6. Enter:

```text
Bearer <YOUR_JWT_TOKEN>
```

7. Click **Authorize**.
8. Protected endpoints can now be executed.

---

# 📮 Postman Collection

The project includes:

```text
Resource-Booking-System.postman_collection.json
```

The collection contains requests for:

* Admin login
* User login
* Resource APIs
* Reservation APIs
* User APIs

The login requests automatically save JWT tokens into:

```text
adminToken
userToken
```

Other requests use these collection variables:

```text
Authorization: Bearer {{adminToken}}
```

or:

```text
Authorization: Bearer {{userToken}}
```

Therefore, the token does not have to be manually copied into every request.

---

# 🌱 Sample Data

`DataInitializer` automatically creates sample users and resources when the application starts.

## Admin

```text
Username: admin
Password: admin123
Role: ADMIN
Email: admin@resourcebooking.com
```

## User

```text
Username: user
Password: user123
Role: USER
Email: user@resourcebooking.com
```

---

# 📦 Sample Resources

The application creates:

### Conference Room A

```text
Type: ROOM
Location: Pune HQ - 2nd Floor
Price: 1500.00
Available: true
```

### Conference Room B

```text
Type: ROOM
Location: Pune HQ - 3rd Floor
Price: 800.00
Available: true
```

### Company Car

```text
Type: VEHICLE
Location: Pune HQ - Parking
Price: 2500.00
Available: true
```

### Projector

```text
Type: EQUIPMENT
Location: Pune HQ - Store Room
Price: 300.00
Available: true
```

---

# 🗓️ Sample Reservations

The initializer also creates sample reservations.

One reservation is associated with:

```text
User
Conference Room A
PENDING
```

Another is associated with:

```text
Admin
Company Car
CONFIRMED
```

The sample reservations are intentionally created with non-overlapping time ranges.

---

# 🗄️ Database Setup

## Step 1 — Install MySQL

Install and start MySQL Server.

---

## Step 2 — Create Database

Run:

```sql
CREATE DATABASE IF NOT EXISTS resource_booking_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

The project also contains:

```text
database.sql
```

which can be used for database setup and verification queries.

---

# ⚙️ Hibernate Configuration

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Therefore, Hibernate automatically creates or updates the required tables.

The main tables are:

```text
users
resources
reservations
```

You do not need to manually create all tables before starting the application.

---

# 🔧 Application Configuration

Default MySQL configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking_db
spring.datasource.username=root
spring.datasource.password=root
```

The project also supports environment variables.

| Environment Variable | Purpose             |
| -------------------- | ------------------- |
| `DB_URL`             | MySQL JDBC URL      |
| `DB_USERNAME`        | Database username   |
| `DB_PASSWORD`        | Database password   |
| `JWT_SECRET`         | JWT signing secret  |
| `JWT_EXPIRATION`     | JWT expiration time |

---

# 🔑 JWT Configuration

Default expiration:

```text
86400000 milliseconds
```

which equals:

```text
24 hours
```

The application uses an HMAC signing key for JWT.

For production deployment, the default JWT secret must be replaced with a secure secret.

Example:

```text
JWT_SECRET=<strong-secret>
JWT_EXPIRATION=86400000
```

---

# 💻 Prerequisites

Before running the project, install:

* Java 17
* Maven
* MySQL
* Git
* Postman (optional)
* IntelliJ IDEA / Eclipse / Spring Tool Suite (optional)

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

---

# 📥 Clone the Repository

```bash
git clone https://github.com/sanikashelake49/Resource-Booking-System.git
```

Navigate into the project:

```bash
cd Resource-Booking-System/resource-booking-system
```

---

# ▶️ Run the Application

## Maven

Build:

```bash
mvn clean install
```

Run:

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

---

# 🖥️ Running from Eclipse

1. Open Eclipse.
2. Select:

```text
File → Import
```

3. Select:

```text
Maven → Existing Maven Projects
```

4. Select the `resource-booking-system` directory.
5. Click **Finish**.
6. Configure JDK 17.
7. Wait for Maven dependencies to download.
8. Open:

```text
ResourceBookingApplication.java
```

9. Select:

```text
Run As → Spring Boot App
```

---

# 🧪 Testing

The project includes automated testing using:

```text
JUnit 5
Mockito
Spring Boot Test
MockMvc
Spring Security Test
H2
```

Run tests:

```bash
mvn test
```

Tests use an H2 in-memory database through:

```text
src/test/resources/application-test.properties
```

Therefore, MySQL is not required to execute the test suite.

---

# 🧪 Testing Areas

The test setup is designed around important backend scenarios including:

* Application context loading
* Authentication
* Invalid credentials
* Authorization
* Resource access
* Reservation ownership
* Booking conflicts
* Validation
* Filtering
* Pagination
* Sorting
* Unauthorized requests
* Forbidden requests

---

# 🔄 Complete Booking Workflow

```text
                 ┌──────────────┐
                 │    Client    │
                 └──────┬───────┘
                        │
                        ▼
                 POST /auth/login
                        │
                        ▼
              Username + Password
                        │
                        ▼
                Authentication
                        │
                        ▼
                    JWT Token
                        │
                        ▼
             Authorization Header
             Bearer <JWT_TOKEN>
                        │
                        ▼
            JwtAuthenticationFilter
                        │
                        ▼
              SecurityContext
                        │
                        ▼
              Role Authorization
                        │
                        ▼
                  Controller
                        │
                        ▼
                   Service
                        │
             ┌──────────┴──────────┐
             │                     │
             ▼                     ▼
      Validation              Business Rules
                                   │
                          ┌────────┴────────┐
                          │                 │
                          ▼                 ▼
                  Resource Exists?   Booking Conflict?
                          │                 │
                          └────────┬────────┘
                                   │
                                   ▼
                              Repository
                                   │
                                   ▼
                                MySQL
```

---

# 🧠 Important Business Rules

## Rule 1 — User Identity

User identity always comes from the authenticated JWT.

```text
Never trust userId from client input.
```

---

## Rule 2 — Reservation Ownership

A USER can access only their own reservations.

```text
USER + Own Reservation → Allowed
USER + Other Reservation → Forbidden
ADMIN + Any Reservation → Allowed
```

---

## Rule 3 — Reservation Status

New USER reservations are:

```text
PENDING
```

Only ADMIN can explicitly change reservation status through the request.

---

## Rule 4 — Time Validation

The system requires:

```text
startTime < endTime
```

Otherwise:

```text
400 BAD REQUEST
```

---

## Rule 5 — Double Booking

A resource cannot have overlapping:

```text
PENDING
```

or:

```text
CONFIRMED
```

reservations.

---

## Rule 6 — Cancelled Reservations

A:

```text
CANCELLED
```

reservation does not block another reservation for the same resource.

---

## Rule 7 — Resource Management

Only ADMIN can:

```text
CREATE
UPDATE
DELETE
```

resources.

---

# 🧩 Design Patterns / Concepts Used

The project demonstrates several practical backend patterns.

### Layered Architecture

```text
Controller
Service
Repository
```

### DTO Pattern

Separate request and response DTOs are used instead of exposing entities directly.

Examples:

```text
LoginRequest
LoginResponse
ResourceRequest
ResourceResponse
ReservationRequest
ReservationResponse
UserResponse
```

### Repository Pattern

Spring Data JPA repositories abstract database operations.

### Specification Pattern

`ReservationSpecification` provides dynamic filtering.

### Global Exception Handling

`GlobalExceptionHandler` centralizes API error handling.

### Dependency Injection

Spring constructor injection is used through Lombok:

```java
@RequiredArgsConstructor
```

### Stateless Authentication

JWT is used instead of server-side sessions.

---

# 🔍 Important Classes

## `AuthController`

Handles:

```text
POST /auth/login
```

---

## `AuthService`

Responsible for:

* Authentication
* User lookup
* JWT generation
* Login response

---

## `JwtService`

Responsible for:

* JWT generation
* JWT parsing
* Username extraction
* Role extraction
* Expiration validation
* Signature verification

---

## `JwtAuthenticationFilter`

Responsible for:

* Reading Authorization header
* Extracting Bearer token
* Validating JWT
* Loading user details
* Setting authentication in SecurityContext

---

## `SecurityConfig`

Responsible for:

* Security filter chain
* Stateless sessions
* Public endpoints
* Authentication provider
* BCrypt password encoder
* JWT filter registration

---

## `ResourceService`

Responsible for:

* Resource creation
* Resource retrieval
* Resource update
* Resource deletion
* Resource pagination
* Resource response mapping

---

## `ReservationService`

Contains the main reservation business logic:

* Reservation creation
* Reservation ownership
* Double-booking prevention
* Reservation update
* Reservation deletion
* Reservation filtering
* Pagination
* Sorting
* Status management

---

## `ReservationSpecification`

Creates dynamic database filtering conditions.

Supported conditions:

```text
status
minPrice
maxPrice
ownerUserId
```

---

## `GlobalExceptionHandler`

Provides centralized handling for:

* Validation errors
* Resource not found
* Reservation not found
* User not found
* Booking conflicts
* Unauthorized requests
* Access denied

---

# 🧱 DTO Design

The project uses DTOs to separate API contracts from database entities.

### LoginRequest

```text
username
password
```

### LoginResponse

```text
token
username
role
```

### ResourceRequest

```text
name
description
type
location
price
available
```

### ResourceResponse

```text
id
name
description
type
location
price
available
createdAt
```

### ReservationRequest

```text
resourceId
startTime
endTime
price
status
```

Notice that:

```text
userId
```

is intentionally absent.

### ReservationResponse

```text
id
resourceId
resourceName
userId
username
startTime
endTime
price
status
createdAt
```

---

# 📈 Scalability Considerations

The current architecture separates:

```text
Controller
Service
Repository
```

which makes it easier to extend the application.

Possible future additions can be introduced without heavily modifying existing layers.

For example:

```text
NotificationService
PaymentService
AvailabilityService
ReportingService
```

could be added later.

---

# 🔮 Future Improvements

The current application is focused on the core booking backend.

Possible future improvements include:

* Refresh tokens
* JWT token revocation
* Email notifications
* Booking reminders
* Admin dashboard
* Resource availability calendar
* Docker
* Docker Compose
* CI/CD pipeline
* Redis caching
* Rate limiting
* Soft deletion
* Advanced reporting
* Booking history
* Search by resource name/type/location
* Manager-level authorization
* Production deployment

---

# 💼 Resume Description

### Short Version

**Resource Booking System | Java, Spring Boot, Spring Security, JWT, MySQL, JPA/Hibernate**

Developed a secure RESTful resource booking API with JWT authentication and ADMIN/USER role-based authorization. Implemented resource and reservation management with ownership validation, double-booking prevention, dynamic filtering, pagination, sorting, validation, centralized exception handling, Swagger/OpenAPI documentation, and automated testing.

---

# 📌 Strong Resume Bullet Points

* Developed a secure RESTful Resource Booking System using **Java 17, Spring Boot, Spring Security, JWT, JPA/Hibernate, and MySQL** with ADMIN/USER role-based authorization.
* Implemented **reservation ownership validation and JWT-based user identification**, preventing users from accessing or modifying other users' reservations.
* Designed **overlap detection for PENDING and CONFIRMED reservations** to prevent double-booking, returning `409 Conflict` for conflicting time ranges.
* Implemented **dynamic reservation filtering using JPA Specifications**, along with pagination, sorting, Jakarta validation, centralized exception handling, Swagger/OpenAPI, and JUnit/MockMvc testing.

---

# 🎤 Interview Explanation

## What is your project?

> Resource Booking System is a Spring Boot REST API for managing shared resources such as meeting rooms, vehicles, and equipment. It provides JWT-based authentication, role-based authorization, resource management, and reservation management. Users can manage their own reservations, while admins can manage resources and access all reservations.

---

## How does authentication work?

> The user logs in using username and password. Spring Security's AuthenticationManager verifies the credentials using BCrypt. After successful authentication, the application generates a JWT containing the username, role, issue time, and expiration time. The client sends this token in the Authorization header for subsequent requests. A JWT filter validates the token and sets the authenticated user in the SecurityContext.

---

## How do you prevent double booking?

> Before creating or updating a reservation, I check whether another active reservation exists for the same resource with an overlapping time range. I consider PENDING and CONFIRMED reservations as active. The overlap condition is `existing.startTime < requested.endTime` and `existing.endTime > requested.startTime`. If an overlap exists, the API returns 409 Conflict.

---

## How do you prevent users from accessing other users' reservations?

> I don't accept `userId` from the reservation request. Instead, I get the authenticated username from Spring Security's Authentication object, find the corresponding user, and use that user as the reservation owner. For read, update, and delete operations, the service checks whether the current user owns the reservation or has the ADMIN role.

---

## Why did you use JWT?

> I used JWT because it provides stateless authentication for REST APIs. The server doesn't need to maintain an HTTP session. Each protected request contains the JWT, which the server validates before allowing access.

---

## Why did you use JPA Specification?

> I used JPA Specification because reservation filtering can involve multiple optional parameters such as status, minimum price, maximum price, and ownership. Specification allows these conditions to be dynamically combined without creating a separate repository method for every combination.

---

## How did you handle exceptions?

> I created custom exceptions for cases such as resource not found, reservation not found, booking conflict, bad requests, and unauthorized access. A centralized GlobalExceptionHandler using `@RestControllerAdvice` converts these exceptions into consistent JSON error responses.

---

## Why did you use DTOs?

> I used separate request and response DTOs to control the data exposed through the API and keep API contracts separate from the database entities. It also allows me to prevent sensitive or client-controlled fields such as the reservation owner from being directly supplied by the client.

---

# 🌐 Repository

GitHub:

https://github.com/sanikashelake49/Resource-Booking-System

Project path:

```text
resource-booking-system/
```

---

# 👩‍💻 Author

**Sanika Shelake**

GitHub:

https://github.com/sanikashelake49

---

# 📄 License

This project is developed for educational, portfolio, and backend development demonstration purposes.
