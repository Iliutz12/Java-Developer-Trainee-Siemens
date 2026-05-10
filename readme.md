# Train Ticketing Application

A Spring Boot REST API for managing train schedules, bookings, and passenger notifications. Includes a JWT-based authentication.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Solution Description](#solution-description)
- [Setup](#setup)
- [Authentication](#authentication)
- [Predefined Train Data](#predefined-train-data)
- [API Reference with Examples](#api-reference-with-examples)
- [Error Responses](#error-responses)
- [Project Structure](#project-structure)

---

## Tech Stack

- Java 25 / Spring Boot 4
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Spring Mail
- Lombok

---

## Solution Description
#### 1. Finding Routes
To figure out how to get from Station A to Station B, I used a BFS algorithm in the RouteService. 
The most important part of this search is the time check. If the route requires you to change trains, the algorithm specifically checks the arrival time of your first train and the departure time of the connection. It enforces a minimum 5-minute gap for the changeover. If the next train leaves too soon, the algorithm ignores it and keeps searching for a valid route.

#### 2. Login & Security
To handle user accounts and admin privileges, I used Spring Security with JSON Web Tokens (JWT).
When you log in with your username and password, the backend verifies it and hands you back a long string of characters. For any action that requires an account (booking a ticket or adding a train), you attach this token to your request. The token also remembers if you are a CUSTOMER or an ADMINISTRATOR. The app checks this to make sure regular users can't access the admin endpoints.

### 3.Stopping Overbooking
To make sure we don't accidentally sell the same seat twice, I used a Pessimistic Write Lock on the database. It sounds super fancy, but it just means when you hit 'book', it literally locks that train's data row. If two people try to snipe the exact same last seat at the exact same millisecond, the database forces them to wait in a single-file line. Zero overbooked trains.

#### 3. Auto-Loading Data
The very first time you start the application, it reads the trains_initial_data.csv file and automatically fills the database with stations, trains, and schedules.

---

## Setup

### 1. Prerequisites

- JDK 25+
- PostgreSQL running locally (default port 5432)
- A Gmail account with an App Password enabled

### 2. Create the database

```sql
CREATE DATABASE train_db;
```

### 3. Configure environment variables

Credentials are never hardcoded. Set these environment variables before running:

| Variable        | Description                         | Dev fallback                            |
|-----------------|-------------------------------------|-----------------------------------------|
| `DB_URL`        | JDBC connection URL                 | `jdbc:postgresql://localhost:5432/train_db` |
| `DB_USERNAME`   | Postgres username                   | `postgres`                              |
| `DB_PASSWORD`   | Postgres password                   | *(empty)*                               |
| `MAIL_USERNAME` | Gmail address                       | *(empty)*                               |
| `MAIL_PASSWORD` | Gmail App Password                  | *(empty)*                               |
| `JWT_SECRET`    | Signing key, at least 32 characters | `ThisIsADefaultSecretKeyForDevOnly!!`   |

### 4. Run


The app starts on **http://localhost:8080**.

On first startup, `DataSeeder` automatically loads 7 trains and their routes from `trains_initial_data.csv`.

---

## Authentication

The API uses stateless JWT bearer tokens.

| Endpoint pattern         | Access                          |
|--------------------------|---------------------------------|
| `POST /api/home-page`    | Public (register)               |
| `POST /api/home-page/login` | Public                       |
| `POST /api/routes/search`| Public                          |
| `/api/bookings/**`       | Any authenticated user          |
| `/api/admin/**`          | `ADMINISTRATOR` role only       |

---

## Predefined Train Data

| Train  | From                 | To                      | Departs | Arrives | Capacity |
|--------|----------------------|-------------------------|---------|---------|----------|
| IR1592 | Bucuresti Nord       | Miercurea Ciuc          | 07:00   | 11:10   | 180      |
| IC513  | Bucuresti Nord       | Cluj-Napoca             | 06:30   | 14:00   | 220      |
| IR1833 | Cluj-Napoca          | Suceava                 | 08:00   | 14:30   | 160      |
| R4032  | Brasov               | Sibiu                   | 10:00   | 12:10   | 120      |
| IR1765 | Timisoara Nord       | Craiova                 | 07:15   | 13:00   | 200      |
| R8041  | Sibiu                | Alba Iulia              | 09:00   | 11:10   | 100      |
| IR1681 | Bucuresti Nord       | Constanta               | 09:00   | 13:00   | 180      |

---

## API Reference with Examples in Postman Agent

All request and response bodies are JSON.

---

### 1. Register & Login

#### Register

```
POST /api/home-page
```

Request:
```json
{
  "username": "ion_popescu",
  "email":    "ion@example.com",
  "password": "secret123",
  "role":     "CUSTOMER"
}
```

Response `201 Created`:
```json
{
  "id":       1,
  "username": "ion_popescu",
  "email":    "ion@example.com",
  "role":     "CUSTOMER"
}
```

![Register](images/Register.png)

Error `409 Conflict`:
```json
{ "error": "Username or email already exists." }
```

![Register-Conflict](images/Register-Conflict.png)

Error `401 Bad Request`:
```json
{"error": "email: Email must be valid"}
```

![Register-Conflict](images/Register-InvalidEmail.png)

---

#### Login

```
POST /api/home-page/login
```

Request:
```json
{
  "username": "ilie",
  "password": "123456"
}
```

Response `200 OK`:
```json
{
  "user": {
    "id": 5,
    "username": "ilie",
    "email": "ilieberindei0@gmail.com",
    "role": "CUSTOMER"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpbGllIiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzc4NDA2MzIyLCJleHAiOjE3Nzg0OTI3MjJ9.3h3a6B0_q43-z8C-U1aH4VvDfrEHyPE9ZytvPWKWKok"
}
```
![Login](images/Login.png)


Error `401 Unauthorized`:
```json
{ "error": "Invalid username or password." }
```

![Login](images/Login-Unauthorized.png)

---

### 2. Search Routes

Finds a route between two stations — direct or with a changeover. No token required.

```
POST /api/routes/search
```

Request (direct route):
```json
{
  "fromStation": "Bucuresti Nord",
  "toStation":   "Brasov"
}
```

Response `200 OK`:
```json
{
  "segments": [
    {
      "trainId": 3,
      "trainName": "IR1592",
      "fromStation": "Bucuresti Nord",
      "toStation": "Brasov",
      "departure": "07:00:00",
      "arrival": "09:15:00"
    }
  ]
}
```

![Login](images/Search-Route.png)

Request (changeover required):
```json
{
  "fromStation": "Sibiu",
  "toStation":   "Cluj-Napoca"
}
```

Response `200 OK`:
```json
{
  "segments": [
    {
      "trainId": 8,
      "trainName": "R8041",
      "fromStation": "Sibiu",
      "toStation": "Blaj",
      "departure": "09:00:00",
      "arrival": "10:15:00"
    },
    {
      "trainId": 4,
      "trainName": "IC513",
      "fromStation": "Blaj",
      "toStation": "Cluj-Napoca",
      "departure": "12:47:00",
      "arrival": "14:00:00"
    }
  ]
}
```

![Login](images/Search-Route-ChangeOver.png)

Request (Station not recognized):
```json
{
  "fromStation": "Bucuresti Nord",
  "toStation":   "Iasi"
}
```

Error `400` — station name not recognized:
```json
{"error": "Station not found: 'Iasi'"}
```

![Login](images/Station-Not-Found.png)

Request (No route exists):
```json
{
  "fromStation": "Cluj-Napoca",
  "toStation":   "Bucuresti"
}
```

Error `404` — no route exists:
```json
{"error": "No route found between 'Cluj-Napoca' and 'Bucuresti'"}
```

![Login](images/No-Route-Found.png)

---

### 3. Book Tickets

A confirmation email is sent to the customer after booking. Overbooking is prevented using a database-level pessimistic write lock on the train row.

```
POST /api/bookings
```

Request (User not Found):
```json
{
  "userId":           1,
  "trainId":          1,
  "numberOfTickets":  2,
  "departureStation": "Bucuresti Nord",
  "arrivalStation":   "Brasov",
  "departureTime":    "07:00:00",
  "arrivalTime":      "09:15:00"
}
```

Response `400 Bad Request`:
```json
{"error": "User not found"}
```

![Login](images/Booking-User-Not-Found.png)

Request (Train Not Found):
```json
{
  "userId":           5,
  "trainId":          1,
  "numberOfTickets":  2,
  "departureStation": "Bucuresti Nord",
  "arrivalStation":   "Brasov",
  "departureTime":    "07:00:00",
  "arrivalTime":      "09:15:00"
}
```

Response `400 Bad Request`:
```json
{"error": "Train not found"}
```

![Login](images/Booking-Train-Not-Found.png)

Request (Booking Created):
```json
{
  "userId":           5,
  "trainId":          3,
  "numberOfTickets":  2,
  "departureStation": "Bucuresti Nord",
  "arrivalStation":   "Brasov",
  "departureTime":    "07:00:00",
  "arrivalTime":      "09:15:00"
}
```

Response `201 Created`:
```json
{
  "id": 23,
  "user": {
    "id": 5,
    "username": "ilie",
    "email": "ilieberindei0@gmail.com",
    "role": "CUSTOMER"
  },
  "train": {
    "id": 3,
    "name": "IR1592"
  },
  "numberOfTickets": 2,
  "departureStation": "Bucuresti Nord",
  "arrivalStation": "Brasov",
  "departureTime": "07:00:00",
  "arrivalTime": "09:15:00"
}
```

![Login](images/Booking-Created.png)

Email-Sent for confirmation

![Login](images/Booking-Confirmation-Email.png)

Request (Not Enough Seats):
```json
{
  "userId":           5,
  "trainId":          3,
  "numberOfTickets":  2,
  "departureStation": "Bucuresti Nord",
  "arrivalStation":   "Brasov",
  "departureTime":    "07:00:00",
  "arrivalTime":      "09:15:00"
}
```

Response `400 Bad Request`:
```json
{"error": "Not enough seats. Requested: 200, Available: 198"}
```

![Login](images/Booking-Not-Enough-Seats.png)

Request (Not Enough Tickets):
```json
{
  "userId":           5,
  "trainId":          3,
  "numberOfTickets":  0,
  "departureStation": "Bucuresti Nord",
  "arrivalStation":   "Brasov",
  "departureTime":    "07:00:00",
  "arrivalTime":      "09:15:00"
}
```

Response `400 Bad Request`:
```json
{"error": "numberOfTickets: Must book at least 1 ticket"}
```

![Login](images/Booking-Not-Enough-Tickets.png)

---

#### Cancel a booking

A cancellation email is sent to the customer.

```
DELETE /api/bookings/{id}
```
Request (Delete Booking):
http://localhost:8080/api/bookings/50

Response `404 Not Found`

![Login](images/Booking-Delete-Not-Found.png)

Request (Delete Booking):
http://localhost:8080/api/bookings/22

Response `204 No Content`

![Login](images/Booking-Delete.png)

Booking Canceled Confirmation

![Login](images/Booking-Delete-Email-Confirmation.png)

---

### 4. View My Bookings

```
GET /api/bookings/user/{userId}
```

Request /api/bookings/user/5

Response `200 OK`:
```json
[
  {
    "id": 23,
    "user": {
      "id": 5,
      "username": "ilie",
      "email": "ilieberindei0@gmail.com",
      "role": "CUSTOMER"
    },
    "train": {
      "id": 3,
      "name": "IR1592"
    },
    "numberOfTickets": 2,
    "departureStation": "Bucuresti Nord",
    "arrivalStation": "Brasov",
    "departureTime": "07:00:00",
    "arrivalTime": "09:15:00"
  }
]
```

![Login](images/Bookings-Get.png)

---

### 5. Admin – Manage Trains

All `/api/admin/**` endpoints require `Authorization: Bearer <admin-token>`.

#### List all trains

```
GET /api/admin/trains
```

Response `200 OK`:
```json
[
  
  {
    "id": 3,
    "name": "IR1592",
    "totalCapacity": 200,
    "delayMinutes": 0,
    "bookedTickets": 2
  },
  {
    "id": 8,
    "name": "R8041",
    "totalCapacity": 100,
    "delayMinutes": 5,
    "bookedTickets": 0
  }
]
```

![Login](images/Admin-Get-Trains.png)

---

#### Create a train with its full route

```
POST /api/admin/trains/with-route
```

Request:
```json
{
  "name":          "RE200",
  "totalCapacity": 150,
  "stops": [
    { "stationName": "Cluj-Napoca", "arrivalTime": "",      "departureTime": "06:00", "stopOrder": 1 },
    { "stationName": "Turda",       "arrivalTime": "06:30", "departureTime": "06:32", "stopOrder": 2 },
    { "stationName": "Alba Iulia",  "arrivalTime": "07:15", "departureTime": "",      "stopOrder": 3 }
  ]
}
```

Response `201 Created`:
```json
{
  "id": 12,
  "name": "RE200",
  "totalCapacity": 150,
  "delayMinutes": 0,
  "bookedTickets": 0
}
```

![Login](images/Admin-Create-Train.png)

---

#### Update a train

```
PUT /api/admin/trains/{id}
```
Same body as create a train.

Request:
```json
{
  "name":          "RE200",
  "totalCapacity": 170,
  "stops": [
    { "stationName": "Cluj-Napoca", "arrivalTime": "",      "departureTime": "06:00", "stopOrder": 1 },
    { "stationName": "Turda",       "arrivalTime": "06:30", "departureTime": "06:32", "stopOrder": 2 },
    { "stationName": "Alba Iulia",  "arrivalTime": "07:15", "departureTime": "",      "stopOrder": 3 }
  ]
}
```

Response `201 Created`:
```json
{
  "id": 12,
  "name": "RE200",
  "totalCapacity": 170,
  "delayMinutes": 0,
  "bookedTickets": 0
}
```

![Login](images/Admin-Update-Train.png)

---

#### Delete a train

```
DELETE /api/admin/trains/{id}
```

Response `204 No Content`. All bookings for this train are deleted first.

![Login](images/Admin-Delete-Train.png)

---

#### Get a train's route

```
GET /api/admin/trains/{id}/route
```

Response `200 OK`:
```json
[
  {
    "stationName": "Timisoara Nord", "arrivalTime": null,
    "departureTime": "07:15", "stopOrder": 1
  },
  {"stationName": "Lugoj", "arrivalTime": "08:00",
    "departureTime": "08:02", "stopOrder": 2
  },
  {"stationName": "Caransebes", "arrivalTime": "08:50",
    "departureTime": "08:55", "stopOrder": 3
  },
  {"stationName": "Orsova", "arrivalTime": "10:30",
    "departureTime": "10:32", "stopOrder": 4
  },
  {"stationName": "Drobeta-Turnu Severin", "arrivalTime": "11:10",
    "departureTime": "11:15", "stopOrder": 5
  },
  {"stationName": "Craiova", "arrivalTime": "13:00",
    "departureTime": null, "stopOrder": 6
  }
]
```

![Login](images/Admin-Get-Route.png)

---

### 6. Admin – View All Bookings

```
GET /api/bookings
```

Returns every booking across all trains and all passengers.

Response `200 OK`:
```json
[
  {
    "id": 23,
    "user": {
      "id": 5,
      "username": "ilie",
      "email": "ilieberindei0@gmail.com",
      "role": "CUSTOMER"
    },
    "train": {
      "id": 3,
      "name": "IR1592"
    },
    "numberOfTickets": 2,
    "departureStation": "Bucuresti Nord",
    "arrivalStation": "Brasov",
    "departureTime": "07:00:00",
    "arrivalTime": "09:15:00"
  }
]
```
![Login](images/Admin-Get-Bookings.png)

---

### 7. Admin – Report a Delay

Every passenger with an active booking on the affected train receives an email notification automatically.

```
PUT /api/admin/trains/delay
```

Request:
```json
{
  "trainId":      1,
  "delayMinutes": 25
}
```

Response `200 OK`:
```json
{
  "id": 3,
  "name": "IR1592",
  "totalCapacity": 200,
  "delayMinutes": 25,
  "bookedTickets": 0
}
```

![Login](images/Admin-Add-Delay.png)

Each affected passenger receives:

> **Subject:** Alert Delay Train: IR1592  
> Hello ilie, we inform you that train IR1592 has a delay of 25 minutes.

![Login](images/Admin-Delay-Confirmation-Email.png)

---

## Error Responses

| Status | When |
|--------|------|
| `400`  | Invalid input, validation failure, overbooking |
| `401`  | Missing or invalid JWT token |
| `403`  | Valid token but insufficient role |
| `404`  | Resource not found |
| `409`  | Duplicate username or email |

All errors follow:
```json
{ "error": "Human-readable description" }
```

---

## Project Structure

```
src/main/java/com/example/trainapplication/
├── components/
│   └── DataSeeder.java              # Seeds initial train data from CSV on first startup
├── config/
│   ├── GlobalExceptionHandler.java  # Converts @Valid errors to clean JSON
│   └── SecurityConfig.java          # JWT filter chain and route authorization
├── contracts/                       # Validated request bodies (Java records)
├── controller/                      # REST controllers
├── dtos/                            # Response shapes (entities never exposed directly)
├── enums/
│   └── Role.java                    # ADMINISTRATOR, CUSTOMER
├── model/                           # JPA entities: User, Train, Station, Schedule, Booking
├── repositories/                    # Spring Data JPA interfaces
├── security/
│   ├── JwtUtils.java                # Token generation and validation
│   └── JwtAuthFilter.java           # Reads Bearer token on every request
└── services/                        # Business logic (interfaces + implementations)

src/main/resources/
├── application.properties           # Config — all secrets via environment variables
└── trains_initial_data.csv          # Seed data: 7 trains across Romania
```
