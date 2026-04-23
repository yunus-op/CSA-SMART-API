# Smart Campus — Sensor & Room Management API

> **Module:** Client-Server Architectures | **Stack:** JAX-RS (Jersey 2.39.1) + Grizzly + Jackson | **Java 11**

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

**Question:** What is the default lifecycle of a JAX-RS Resource class? Is a new instance created per request or is it a singleton?

**Answer:**

By default, JAX-RS uses a **per-request lifecycle** for resource classes. The JAX-RS runtime creates a **brand new instance** of the resource class for every single incoming HTTP request, and that instance is discarded once the response is sent.

This design approach has significant implications for in-memory data management. Since each request gets a fresh resource object, **you cannot store application state in instance variables of a resource class** — any data stored as an instance field would be lost at the end of the request. If you stored your `HashMap<String, Room>` as an instance field rather than in a shared static or singleton class, every request would start with an empty data store.

To prevent data loss and race conditions in this project, I used a **static `ConcurrentHashMap`** inside a dedicated `InMemoryStore` utility class. The maps are declared `static`, meaning they belong to the class itself rather than any particular instance, and are shared across all incoming requests throughout the application's lifetime. Using `ConcurrentHashMap` instead of a plain `HashMap` ensures that concurrent writes from multiple simultaneous request-handling threads cannot corrupt the data structure, avoiding race conditions without needing explicit `synchronized` blocks.

A singleton lifecycle (`@Singleton`) is also possible in JAX-RS via annotation; in that case you can use instance fields but must handle all thread safety yourself. The default per-request model avoids thread-safety issues on individual objects at the cost of requiring shared state to live elsewhere.

---

### Part 1.2 — Hypermedia / HATEOAS

**Question:** Why is hypermedia considered a hallmark of advanced RESTful design (HATEOAS)? How does it benefit client developers?

**Answer:**

HATEOAS (Hypermedia As The Engine Of Application State) means that an API response includes **links that tell the client what it can do next** — similar to how hyperlinks on a webpage guide navigation. Instead of a client needing a hardcoded knowledge of every URL, the server's responses embed URIs pointing to related resources and available actions.

For example, our discovery endpoint (`GET /api/v1`) returns:
```json
{
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

This benefits client developers in several ways:

1. **Decoupling:** The client does not need to hardcode URLs. If the API evolves and a path changes, the client dynamically discovers the new path from the response — reducing breaking changes.
2. **Self-documentation:** The API becomes self-describing. A new developer can start from the root endpoint and discover the entire API surface by following links, without reading external documentation first.
3. **Reduced errors:** Clients are less likely to construct invalid URLs manually when they follow server-provided links.
4. **Workflow guidance:** The server can guide the client through complex workflows by including only the links that are valid in the current application state (e.g., not providing a "delete" link for a room that has sensors — because that action is currently forbidden).

---

### Part 2.1 — Returning IDs vs Full Objects

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects?

**Answer:**

**Returning only IDs:**
- ✅ **Minimal bandwidth** — the response payload is tiny, especially when there are thousands of rooms.
- ✅ **Fast initial response** — the list endpoint is quick.
- ❌ **Chatty API** — the client must then make N additional `GET /rooms/{id}` calls to retrieve data for each room, causing the "N+1 request problem" which can devastate performance under load.
- ❌ **Client complexity** — clients must implement multi-step data fetching logic.

**Returning full objects:**
- ✅ **Single-request completion** — clients get everything they need in one call.
- ✅ **Simpler client code** — no need to chain requests.
- ❌ **Larger payloads** — if rooms have many fields or embedded sub-objects, responses are significantly heavier.
- ❌ **Over-fetching** — clients that only need names/IDs still receive unnecessary data.

**Industry best practice (used in this project):** Return full objects in list responses for small-to-medium datasets. For very large collections, use **pagination** (e.g., `?page=0&size=20`) combined with full objects, so clients get actionable data per page without downloading the entire dataset at once.

---

### Part 2.2 — Idempotency of DELETE

**Question:** Is the DELETE operation idempotent in your implementation? Justify by describing what happens across multiple identical DELETE requests.

**Answer:**

**Yes, the DELETE operation is idempotent** in this implementation.

HTTP defines idempotency as: making the same request N times produces the same server state as making it once.

In this implementation:
- **First DELETE call on an existing room (with no sensors):** The room is found, removed from the store, and `204 No Content` is returned.
- **Second (and any subsequent) DELETE call with the same room ID:** The room no longer exists in the store. Rather than returning a `404 Not Found` error, the service returns `204 No Content` again.

This is the correct idempotent behaviour. The client's intent was "ensure this room does not exist" — and the server confirms that state is achieved, regardless of whether it was already gone before the call. Returning `404` on a repeated DELETE would technically be non-idempotent from the client's perspective (different response codes), though the server state would be the same.

The key distinction is between **server state** (which remains identical: the room is absent) and the HTTP **response code** (which is identical in this design: always 204 when there are no sensors). This design is consistent with RFC 9110 guidance that DELETE should be idempotent.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

**Question:** What happens technically if a client sends data in a different format (text/plain, application/xml) when @Consumes(APPLICATION_JSON) is declared?

**Answer:**

When a resource method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime enforces that the incoming request's `Content-Type` header **must** be `application/json`.

If a client sends a body with `Content-Type: text/plain` or `Content-Type: application/xml`:

1. The JAX-RS runtime inspects the incoming request's `Content-Type` header **before** the method is even invoked.
2. It finds no matching resource method that `@Consumes` the declared media type.
3. JAX-RS automatically returns **HTTP 415 Unsupported Media Type** — the method is never called.
4. The response body is typically a default error message from the JAX-RS implementation (Jersey in this case).

This is a framework-level enforcement mechanism — the developer does not need to write any validation code. The `@Consumes` annotation acts as a declarative content-type filter, routing requests to the correct method and rejecting incompatible formats at the container level. This prevents malformed or incorrectly typed data from even reaching the deserialization stage.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

**Question:** Why is the query parameter approach (`?type=CO2`) considered superior to a path-based approach (`/sensors/type/CO2`) for filtering collections?

**Answer:**

**Query parameters (`?type=CO2`) are superior for filtering for these reasons:**

1. **Semantic correctness:** URL paths identify **resources**. `/sensors/CO2` implies CO2 is a distinct resource, not a filter on the sensors collection. Query parameters communicate "here is the resource; these are optional constraints on what to return."

2. **Composability:** Query parameters combine naturally. `?type=CO2&status=ACTIVE&roomId=LIB-301` is intuitive. Achieving the same with path parameters requires complex, fragile URL segments: `/sensors/type/CO2/status/ACTIVE/room/LIB-301`.

3. **Optionality:** In JAX-RS, `@QueryParam` values are optional and default to `null` when absent — so `GET /sensors` and `GET /sensors?type=CO2` both map to the same method, with the filter simply not applied when omitted. Path parameters are mandatory by definition.

4. **Caching friendliness:** Caches (browsers, CDNs, proxies) treat query parameters as part of the cache key, making filtered vs. unfiltered requests separately cacheable without collision.

5. **REST conventions:** REST best practices (and frameworks like Spring MVC, JAX-RS) universally use query parameters for search, filter, sort, and pagination — establishing a developer-expectation that query strings mean "narrow down this collection."

6. **Idempotency is preserved:** Query-parameter-based GET requests remain safe and idempotent without any additional considerations.

---

### Part 4.1 — Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes manage complexity?

**Answer:**

The Sub-Resource Locator pattern is a JAX-RS mechanism where a method in a resource class **returns an object instance** (a sub-resource class) rather than a response — effectively delegating routing to that returned object.

In this project, `SensorResource` contains:
```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingsSubResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

**Architectural benefits:**

1. **Separation of Concerns:** `SensorResource` manages sensor CRUD. `SensorReadingResource` manages reading operations. Each class has a single, well-defined responsibility — adhering to the Single Responsibility Principle (SRP).

2. **Reduced complexity:** If all nested paths (`/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`) lived in one massive `SensorResource`, that class would become enormous and hard to maintain. Sub-resources keep class sizes manageable.

3. **Reusability:** A sub-resource class could theoretically be reused by multiple parent resources if the business domain required it.

4. **Context injection:** The parent resource can validate the parent entity (confirm the sensor exists) before constructing the sub-resource, providing a clean validation layer.

5. **Independent testability:** `SensorReadingResource` can be unit-tested in isolation by constructing it directly with a `sensorId`, without needing the full JAX-RS runtime.

6. **Scalability:** As the API grows, adding more sub-resource types (e.g., `/sensors/{id}/alerts`) is trivial — just add a new locator method and a new class. No existing code is modified.

---

### Part 5.2 — Why HTTP 422 Instead of 404 for Missing References

**Question:** Why is HTTP 422 Unprocessable Entity often more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

The core distinction is **what** was not found:

- **404 Not Found** means the **endpoint/URL** the client requested does not exist on the server. The request URL itself is the problem — there is no resource at that address.

- **422 Unprocessable Entity** means the **request URL was found and understood**, the **payload was syntactically valid JSON**, but the **semantic content of that payload is problematic**. The server understood what you asked for but cannot process it because of a logical/business error within the data.

In our case, a client sends a valid JSON body to `/api/v1/sensors` (a valid endpoint), with `"roomId": "ROOM-DOES-NOT-EXIST"`. The endpoint exists. The JSON is valid. But the value inside the payload contains a reference to a non-existent resource.

Using `404` here would be misleading — it would imply `/api/v1/sensors` does not exist, which is false. Using `422` precisely communicates: "Your request arrived at the right place in the right format, but the content you provided contains an unresolvable reference." This precision helps client developers debug issues faster and is consistent with web standards such as RFC 4918, which defines 422 specifically for semantically invalid requests.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

**Question:** From a cybersecurity standpoint, explain the risks of exposing internal Java stack traces to external API consumers.

**Answer:**

Exposing raw Java stack traces to external clients constitutes **information disclosure** — a category of vulnerability listed in the OWASP Top 10. Specifically, an attacker can extract:

1. **Internal file paths and package names:** Stack traces contain full qualified class names and sometimes file paths (e.g., `at com.smartcampus.storage.InMemoryStore.getRoom(InMemoryStore.java:45)`). This reveals the internal package structure and naming conventions, helping an attacker map the application's architecture.

2. **Library names and versions:** Exception messages often include library class names (e.g., `org.glassfish.jersey.server...`, `com.fasterxml.jackson...`). This enables attackers to look up **known CVEs** (Common Vulnerabilities and Exposures) for the exact version in use and craft targeted exploits.

3. **Business logic flow:** The sequence of method calls in a stack trace reveals how the application processes requests internally — exposing decision points, validation steps, and data access patterns that can be reverse-engineered.

4. **Database and ORM details:** If persistence were added, stack traces from ORM frameworks (Hibernate, JPA) would expose table names, column names, and SQL queries — greatly aiding SQL injection attacks.

5. **Infrastructure details:** Error messages may expose server type, OS details, or network topology clues.

**Mitigation (implemented in this project):** The `GenericExceptionMapper<Throwable>` catches all unexpected errors, logs the full stack trace **server-side** (where only authorised developers can access it), and returns a deliberately vague, generic `500 Internal Server Error` JSON response to the client — containing no technical implementation details whatsoever.
