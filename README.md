# Smart Campus Sensor and Room Management API
**Module:** 5COSC022W Client-Server Architectures  
**Coursework Report**  
**Student Name:** Ahamed Amhar  
**Student ID:** w1956142 / 20221439  
**Date:** 22/04/2026  
---
## API Overview
This project is a RESTful API built using JAX-RS for managing campus rooms, sensors, and sensor readings as part of the Smart Campus scenario. The API supports room management, sensor registration, filtered retrieval, nested reading history, validation, exception handling, and request/response logging.
---
## Technology Stack
- Java 8
- JAX-RS (Jersey 2.35)
- Apache Tomcat 9
- Maven WAR packaging
- In-memory data structures only
- ConcurrentHashMap
- CopyOnWriteArrayList
---
## Build and Run
### Prerequisites
- JDK 8 or higher
- Apache Maven
- Apache Tomcat 9
- NetBeans IDE
### Build
```bash
mvn clean package

Run in NetBeans with Tomcat

1. Open the project in NetBeans
2. Add Apache Tomcat in NetBeans
3. Right-click the project → Properties → Run
4. Select Apache Tomcat as the server
5. Right-click the project → Clean and Build
6. Right-click the project → Run

⸻

Base URL

http://localhost:8080/smart-campus-api/api/v1

⸻

Sample curl Commands

1. Discovery endpoint

curl http://localhost:8080/smart-campus-api/api/v1

2. Get all rooms

curl http://localhost:8080/smart-campus-api/api/v1/rooms

3. Create a room

curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"LT-01","name":"Lecture Theatre 01","capacity":120}'

4. Get all sensors

curl http://localhost:8080/smart-campus-api/api/v1/sensors

5. Filter sensors by type

curl "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"

6. Create sensor with invalid roomId

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"HUM-999","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"FAKE-ROOM"}'

7. Add reading

curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{"value":27.8}'

⸻

Report: Answers to Coursework Questions

Part 1: Service Architecture and Setup

1.1 JAX-RS Resource Lifecycle and In-Memory Data Management

JAX-RS creates a new instance of a resource class for each incoming HTTP request. This is the request-scoped lifecycle. Each instance is independent, so any data stored in instance fields disappears after the response is sent.

This directly affects in-memory storage. If the rooms map were declared as an instance field inside RoomResource, it would be empty on every new request. To solve this, shared data must be placed in a class with static fields. Static fields belong to the class rather than any instance, so they remain in memory for the full life of the application. This project uses DataStore.java for this purpose.

Because the server handles requests concurrently, the data structures must also handle concurrent access safely. A standard HashMap is not thread-safe and can be corrupted when two threads write to it at the same time. ConcurrentHashMap handles this correctly without requiring manual synchronisation blocks. The readings list uses CopyOnWriteArrayList because reads are much more frequent than writes and this structure keeps reads very efficient. The sensorIds list in the Room class uses Collections.synchronizedList to prevent concurrent modification issues when sensors are registered simultaneously.

Registering a sensor involves two separate writes: adding the sensor to the sensors map and adding its ID to the room list. These are not atomic, but keeping them consecutive with no exception-throwing code between them is an acceptable approach for this coursework.

1.2 HATEOAS and Hypermedia

HATEOAS stands for Hypermedia as the Engine of Application State. It means including navigable links inside API responses so that clients can move between resources without needing separate documentation.

The discovery endpoint at GET /api/v1 demonstrates this. It returns the API version, contact details, and a map of available resource paths:

{
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}

These links come directly from the server. If a URL changes, the link in the response changes automatically. A client that navigates by following links does not break. Static documentation must be manually updated and can easily become outdated. With HATEOAS, a developer can discover all available resources starting from one known entry point, and the API is self-describing.

⸻

Part 2: Room Management

2.1 Returning IDs vs Full Objects

Returning only IDs from GET /api/v1/rooms would produce a small response payload, but the client would then need a separate GET request for each room to retrieve its details. For 30 rooms that is 31 total requests. This is the N+1 problem.

Returning full room objects gives the client everything in one response. The payload is larger, but for a campus system managing a few hundred rooms with small JSON objects, the overhead is minor. Avoiding extra round trips is more valuable than saving a few kilobytes. This project returns full objects.

2.2 Idempotency of DELETE

The DELETE operation is idempotent in this implementation.

Idempotency means that making the same request more than once produces the same server state as making it once. If a client sends DELETE /api/v1/rooms/LIB-301 and the room exists, it is removed and the response is 200 OK. If the same request is sent again, the room is already gone and the response is 404 Not Found. In both cases the server state is the same: the room does not exist.

POST is not idempotent. Sending POST /api/v1/rooms twice with the same body either creates a duplicate or returns a conflict. The server state is different each time.

⸻

Part 3: Sensor Operations

3.1 Content-Type Mismatch with @Consumes

The @Consumes(MediaType.APPLICATION_JSON) annotation on POST /api/v1/sensors tells the JAX-RS framework to only accept requests with Content-Type: application/json.

If a client sends the request with Content-Type: text/plain or Content-Type: application/xml, the framework rejects it automatically before any application code runs. The response is HTTP 415 Unsupported Media Type. No manual Content-Type checking is needed in the application. This is handled entirely by the framework.

3.2 @QueryParam vs @PathParam for Filtering

Using @QueryParam for filtering, as in /api/v1/sensors?type=CO2, is correct for a collection endpoint. Using a path parameter would produce /api/v1/sensors/type/CO2, which implies that type is a sub-resource. That is semantically wrong.

Query parameters are optional by design. Omitting the type parameter returns all sensors, which is the expected default. Multiple filters also compose naturally: ?type=CO2&status=ACTIVE is clean and readable. Equivalent path parameters produce deeply nested URLs that are hard to extend.

⸻

Part 4: Sub-Resources

4.1 Sub-Resource Locator Pattern

The sub-resource locator is a method in SensorResource annotated only with @Path("{sensorId}/readings"), with no HTTP verb annotation. When a request arrives for /sensors/TEMP-001/readings, JAX-RS calls this method, which returns a new SensorReadingResource instance. JAX-RS then dispatches the actual HTTP method to that instance.

The main benefit is separation of concerns. SensorResource handles sensor data. SensorReadingResource handles reading history. Neither class contains the other’s logic. This is easier to read and maintain than placing every handler in one large class.

Testability also improves. SensorReadingResource can be created directly in a test with a sensor ID without setting up the full SensorResource routing chain. As the API grows, new sub-resources can be added as new classes without changing existing ones.

⸻

Part 5: Error Handling and Logging

5.1 Why 422 Rather Than 404 for an Invalid Room Reference

When POST /api/v1/sensors is called with a roomId that does not exist, the URL /api/v1/sensors is valid and accessible. The error is inside the request body: the roomId value references a room that does not exist in the system.

HTTP 404 means the URL itself could not be resolved. Using 404 here would imply that /api/v1/sensors does not exist, which is incorrect.

HTTP 422 Unprocessable Entity means the server received the request, parsed the JSON successfully, but the data inside is logically invalid. This accurately describes the situation. The endpoint is correct, the JSON is well-formed, but the payload contains a reference that cannot be fulfilled. This gives the client a clear and accurate error signal.

5.2 Security Risks of Exposing Stack Traces

Returning a raw Java stack trace in an API response exposes information that attackers can use.

Package and class names in the stack trace reveal the internal structure of the application and which classes handle specific operations.

Library names and version numbers appear in frames from third-party code. An attacker who knows the exact version of Jersey or Jackson being used can search for known CVEs and attempt to exploit them.

File paths and line numbers reveal the server directory structure and pinpoint exactly where in the code an error occurred.

Exception messages often contain field names or variable values that reveal how the application data is structured.

This project uses GenericExceptionMapper, which catches all unhandled exceptions, logs the full trace on the server only, and returns a generic 500 response to the client with no internal details.

{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later."
}

5.3 Why Filters Are Better Than Manual Logging

Adding Logger.info() calls manually to each resource method means writing the same logging code in every method. With ten or more endpoints that is a lot of duplication. Any endpoint written without it silently skips logging.

ContainerRequestFilter and ContainerResponseFilter handle this automatically. One class logs every request and response across the whole API. New endpoints are covered without writing any extra logging code. Changing the log format means editing one class, not ten.

This also keeps resource methods focused on business logic. Observability is a separate concern and the filter keeps it separate. This is the cross-cutting concern principle used in Aspect-Oriented Programming.

