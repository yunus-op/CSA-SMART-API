# Smart Campus — Sensor & Room Management API

## Student Information

| Field | Details                                           |
|-------|---------------------------------------------------|
| Name | M.R.Yunus Ahmed                                   |
| Module | 5COSC022W — Client-Server Architectures           |
| University ID | w2120132                                          |
| GitHub Repo | https://github.com/yunus-op/CSA-SMART-API |
| Submission Date | 24 April 2026                                     |

---
A RESTful API built with JAX-RS (Jersey) and Grizzly for managing rooms and
sensors across a university smart campus.
---
## Technology Stack
- Java 11
- JAX-RS (Jersey 2.35)
- Grizzly HTTP Server
- Jackson (JSON)
- Maven

## API Base URL
```
http://localhost:8080/api/v1
```

A robust, scalable RESTful API for managing campus rooms and sensors, built as part of the "Smart Campus" initiative. The API manages thousands of Rooms and diverse Sensors (CO2, Temperature, Occupancy) via a clean hypermedia-driven interface.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Project Structure](#project-structure)
3. [Build & Run Instructions](#build--run-instructions)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Sample curl Commands](#sample-curl-commands)
6. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## API Design Overview

| Base URL | `http://localhost:8080/api/v1` |
|---|---|
| Discovery | `GET /api/v1` |
| Rooms | `GET/POST /api/v1/rooms` |
| Room Detail | `GET/DELETE /api/v1/rooms/{roomId}` |
| Sensors | `GET/POST /api/v1/sensors` |
| Sensor Detail | `GET /api/v1/sensors/{sensorId}` |
| Sensor Filtering | `GET /api/v1/sensors?type=CO2` |
| Readings History | `GET/POST /api/v1/sensors/{sensorId}/readings` |

### Key Design Decisions

- **In-memory storage** using `ConcurrentHashMap` — no database as required
- **Jersey** as the JAX-RS 2.1 implementation running on an embedded **Grizzly** HTTP server
- **Jackson** for automatic POJO ↔ JSON serialization
- **Sub-resource locator** pattern for clean delegation of reading management
- **Four custom ExceptionMappers** ensuring a "leak-proof" API (never exposes stack traces)
- **Combined request/response logging filter** for full observability

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                          # Entry point (Grizzly server)
    ├── SmartCampusApplication.java        # @ApplicationPath("/api/v1")
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── storage/
    │   └── InMemoryStore.java             # Thread-safe ConcurrentHashMap store
    ├── resource/
    │   ├── DiscoveryResource.java         # GET /api/v1
    │   ├── RoomResource.java              # /api/v1/rooms
    │   ├── SensorResource.java            # /api/v1/sensors
    │   └── SensorReadingResource.java     # Sub-resource: /readings
    ├── exception/
    │   ├── RoomNotEmptyException.java     # 409 Conflict
    │   ├── LinkedResourceNotFoundException.java  # 422 Unprocessable
    │   └── SensorUnavailableException.java       # 403 Forbidden
    ├── mapper/
    │   ├── RoomNotEmptyExceptionMapper.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   ├── SensorUnavailableExceptionMapper.java
    │   └── GenericExceptionMapper.java    # 500 catch-all
    └── filter/
        └── ApiLoggingFilter.java          # Request + Response logging
```

---

## Build & Run Instructions

### Prerequisites

- Java 11 or higher ([Download JDK](https://adoptium.net/))
- Apache Maven 3.6+ ([Download Maven](https://maven.apache.org/download.cgi))

### Step 1 — Clone the Repository

```bash
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the Project

```bash
mvn clean package
```

This compiles everything and creates a fat JAR at `target/smart-campus-api-1.0-SNAPSHOT.jar`.

### Step 3 — Run the Server

**Option A: Using Maven exec plugin (recommended for development)**

```bash
mvn exec:java
```

**Option B: Running the fat JAR directly**

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

### Step 4 — Verify

The server starts at **`http://localhost:8080`**. Open your browser or Postman and visit:

```
http://localhost:8080/api/v1
```

You should see the discovery JSON response.

### Step 5 — Stop the Server

Press `ENTER` in the terminal where the server is running.

---

## API Endpoints Reference

### Discovery
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1` | API metadata and resource links |

### Rooms
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| `GET` | `/api/v1/rooms` | List all rooms | 200 |
| `POST` | `/api/v1/rooms` | Create a new room | 201 |
| `GET` | `/api/v1/rooms/{roomId}` | Get room by ID | 200 |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room (fails if sensors exist) | 204 |

### Sensors
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| `GET` | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 |
| `POST` | `/api/v1/sensors` | Register a sensor (roomId must exist) | 201 |
| `GET` | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 |

### Readings (Sub-Resource)
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| `GET` | `/api/v1/sensors/{sensorId}/readings` | Get all historical readings | 200 |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Add new reading (blocked if MAINTENANCE) | 201 |

### Error Codes
| Scenario | HTTP Status |
|---|---|
| Room has sensors — cannot delete | 409 Conflict |
| Sensor's roomId doesn't exist | 422 Unprocessable Entity |
| Sensor is in MAINTENANCE | 403 Forbidden |
| Any unexpected runtime error | 500 Internal Server Error |

---

## Sample curl Commands

### 1. Discover the API

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

**Expected:** 200 OK with API metadata, version, contact, and resource links.

---

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

**Expected:** 201 Created with `Location: /api/v1/rooms/LIB-301` header.

---

### 3. Register a Sensor in that Room

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 400.0,
    "roomId": "LIB-301"
  }'
```

**Expected:** 201 Created. The sensor is linked to `LIB-301`.

---

### 4. Attempt to register a Sensor with a non-existent Room (422 demo)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-999",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "ROOM-DOESNOTEXIST"
  }'
```

**Expected:** 422 Unprocessable Entity with JSON error body.

---

### 5. Post a Reading to a Sensor (updates currentValue)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 850.5
  }'
```

**Expected:** 201 Created with reading object (auto-generated UUID + timestamp). Then `GET /api/v1/sensors/CO2-001` will show `currentValue: 850.5`.

---

### 6. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Expected:** 200 OK with only CO2 sensors listed.

---

### 7. Attempt to delete a Room that has sensors (409 demo)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected:** 409 Conflict — "Room cannot be deleted because it has sensors assigned."

---

### 8. Set sensor to MAINTENANCE and attempt a reading (403 demo)

First update the sensor status to MAINTENANCE (by re-registering it — or via the PATCH endpoint if implemented). Then try posting a reading:

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-002",
    "type": "Temperature",
    "status": "MAINTENANCE",
    "currentValue": 0.0,
    "roomId": "LIB-301"
  }'

curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 99.9}'
```

**Expected:** 403 Forbidden — Sensor is in MAINTENANCE and cannot accept readings.

---

### 9. Get all readings for a sensor

```bash
curl -X GET http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Accept: application/json"
```

**Expected:** 200 OK with array of historical reading objects.

---

### 10. Get a specific room by ID

```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected:** 200 OK with full room object including sensorIds list.

---

## Conceptual Report — Question Answers

---

### Part 1.1 — JAX-RS Resource Class Lifecycle

**Question:**  In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con
ditions

**Answer:**

In a default scenario, each time a request reaches the server, a fresh copy of the resource class will be 
created and thrown away afterwards. This way, any state stored inside the resource class using instance 
variables will not survive for long, making it impossible to keep a consistent set of values between different 
requests. In order to overcome this limitation, it is necessary to keep all the information somewhere outside 
the resource class, like in a static collection. In this case, a static ConcurrentHashMap was employed in a 
utility class in order to share data across several threads.
---

### Part 1.2 — Hypermedia / HATEOAS

**Question:**  Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?

**Answer:**

HATEOAS (Hypermedia As The Engine Of Application State) is a REST architecture concept whereby the 
responses from the API are provided with links which show the client what actions they can take; essentially, 
the links act just like web page hyperlinks. Clients no longer have to be hard-coded into knowing certain 
endpoints; rather, the discovery process enables them to find resources through links. This makes the API 
more robust and less error-prone to any changes while at the same time making it self-explanatory through 
linking rather than documentation. This way, there will be no invalid actions since there will be no link for 
performing such actions. Overall, HATEOAS improves communication between client and server by letting 
the API direct interactions and the client follow them.

---

### Part 2.1 — Returning IDs vs Full Objects

**Question:**  When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.

**Answer:**

Reducing the API output to only the IDs ensures that the response sizes remain lean and quick, although this 
creates an API with a chatty interface where the client must make many extra calls in what is known as the 
N+1 problem. However, by sending full objects in response, the client is able to retrieve everything required at 
once without much difficulty; the downside is that this will cause the response sizes to become larger, 
potentially resulting in over-fetching of data. A general good practice would be to send full objects where 
possible with pagination in large collections.
---

### Part 2.2 — Idempotency of DELETE

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing whathappens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**

The DELETE operation in this implementation is idempotent because making the same request multiple times 
results in the same server state and response. On the first DELETE call, the room is removed and a 204 No 
Content status is returned; on subsequent calls, even though the room no longer exists, the server still 
returns 204 instead of 404. This ensures consistent behavior, as the client’s intent—ensuring the room does 
not exist—is satisfied regardless of how many times the request is made. By keeping both the server state 
(room absent) and the response code consistent, this design aligns with HTTP idempotency principles 
outlined in RFC 9110. 


---

### Part 3.1 — @Consumes and Content-Type Mismatch

**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?

**Answer:**

This DELETE action is idempotent in this case since issuing the request multiple times ends up leaving the 
same state on the server and sending the same response. In the first DELETE request issued, the room will be 
deleted and a response status of 204 is sent. The second DELETE request will send the same response of 204 
despite the room already being deleted since there would otherwise be an inconsistency in the responses. 
This is because the client's expectation to ensure that there is no room left after deleting the same is met.
---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?

**Answer:**

Use of query parameters (?type=CO2) is more appropriate for filtering because it explicitly indicates that 
there are optional criteria for selecting a certain resource, not changing the resource itself, which makes the 
approach semantically correct and better than path parameters. The use of query parameters allows us to 
combine different filters more freely and easily supports the use of optional criteria, since when we do not 
provide any value, nothing happens. It is also possible to use caching for requests with query parameters, 
because query parameters are used as a part of the cache key. Moreover, using query parameters 
corresponds to established REST practices regarding filtering, sorting, and paging of resources. 

---

### Part 4.1 — Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con
troller class?

**Answer:**

The Sub-Resource Locator design pattern in JAX-RS enables the resource method to pass a resource class, 
which would handle further request processing on behalf of the parent resource method. In this particular 
case, when the method /sensors/{sensorId}/readings is invoked, a SensorReadingResource object is 
returned. With such a structure, separation of concerns becomes possible – handling the sensor itself and its 
readings can now be handled separately. The advantage here is that code maintainability is improved through 
more specific and smaller classes; reusability is enhanced as well, as is validation, since we may check 
whether the requested sensor is actually present..
---

### Part 5.2 — Why HTTP 422 Instead of 404 for Missing References

**Question:** 
Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

The critical distinction between the 404 Not Found and 422 Unprocessable Entity HTTP status codes hinges 
on the nature of the absence or inaccuracy. While the former suggests that the requested URL or endpoint 
cannot be found on the server, thus the fault being in the URL, the latter implies that the endpoint exists and 
the request payload is correctly structured, but an internal error prevents the processing of the content due to 
an error in semantics or business logic. The 422 status code is appropriate here, as the request made was 
delivered to a correct endpoint, but the data included is faulty.
---

### Part 5.4 — Security Risks of Exposing Stack Traces

**Question:** 
 From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**

The exposure of raw Java stack traces to the client side poses a high risk of information disclosure, as stated 
by OWASP Top 10, since it shows many critical inner workings like package structures, file locations, libraries 
used (which may be compared with known exploits for them), the business logic flow, and possibly other 
critical information like databases or infrastructures. To mitigate this, the implementation uses a 
GenericExceptionMapper Throwable that captures all unexpected errors, logs the full stack trace securely on 
the server for developers, and returns a generic 500 Internal Server Error response to the client without 
exposing any technical details, thereby preserving security while still enabling debugging internally. 

### Part 5.5 — API Request & Response Logging Filters

**Question:** 
 Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?


**Answer:**

Using JAX-RS filters like ContainerRequestFilter and ContainerResponseFilter for logging and other 
aspects that cut across different endpoints provides distinct architectural benefits over manually 
adding logging to every endpoint. Firstly, filters promote separation of concerns since the resource 
methods deal only with the business logic, but not with concerns that are common to other 
methods too. Secondly, filters make the code more concise due to the DRY philosophy, which 
means no redundant lines of code are used in implementing logging because logging is done in one 
single place. Changes will then be applied once only in order to ensure consistency throughout the 
system. Thirdly, filters allow access to all the request and response details.