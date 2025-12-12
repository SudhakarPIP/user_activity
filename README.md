# User Activity Service

Spring Boot + MySQL module to record user activities, soft-delete them, and fetch a paginated timeline. Includes validation, global error handling, Swagger/OpenAPI docs, and unit/integration tests.
User Management - Java Spring Boot
This is a Java Spring Boot–based User Activity Management & Timeline API that supports to 
•	Record user activities
•	Update & delete activities
•	Retrieve a paginated & sorted timeline

The project implements secure authentication using JSON Web Tokens (JWT) and runs on the default server port 8080. 

## Stack
- Java 17, Spring Boot 3.x (Web, Validation)
- Spring Data JPA (Hibernate), MySQL
- Swagger/OpenAPI via springdoc
- JUnit 5, Mockito, MockMvc, H2 (tests)
- Maven, JaCoCo
Features
User activityType =  LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE

## Getting Started   
### Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL running and reachable

### Configuration
Edit `src/main/resources/application.properties` as needed:
spring.datasource.url=jdbc:mysql://localhost:3306/pip?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=******

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.htmlFor tests, H2 is used via `src/test/resources/application-test.properties`.

### Build & Run
mvn clean install
mvn spring-boot:runApp runs on `http://localhost:8080`.

### Swagger / OpenAPI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## API Endpoints
Base path: `/api/v1`

- `POST /users/{userId}/activities`  
  Create activity. Body:
 
  {
    "activityType": "LOGIN",
    "description": "User logged in from web",
    "metadata": "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome\"}"
  }
  - `DELETE /activities/{activityId}`  
  Soft delete activity.
- `GET /users/{userId}/activities/timeline?page=0&size=20`  
  Paginated timeline (sorted desc by `createdAt`).

Supported `activityType`: `LOGIN`, `LOGOUT`, `PASSWORD_CHANGE`, `PROFILE_UPDATE`.

## Validation & Errors
- Request validation via `jakarta.validation`.
- Consistent error JSON from `GlobalExceptionHandler`.
- Soft-delete guard prevents double deletion.
- Not-found returns 404.

## Data Model
Table: `user_activities`
- `id` (PK, BIGINT, auto-increment)
- `user_id` (indexed)
- `activity_type` (VARCHAR)
- `description` (TEXT)
- `metadata` (JSON/TEXT)
- `created_at`, `updated_at` (timestamps)
- `is_deleted` (BOOLEAN)

Indexes on `user_id`, `created_at`.

## Tests
- Unit: service logic, exceptions.
- Web layer: controller validation and HTTP codes.
- Integration: create/delete/timeline flows with H2.
- Run: `mvn test` (JaCoCo report under `target/site/jacoco`).

## Notes
- Timeline latency target ≤ 800 ms (ensure DB indexes present).
- Code quality: avoid Sonar blocker/critical issues.
- Default packaging: WAR (Tomcat provided scope). Adjust if you prefer JAR.
REST API Endpoints
Method	        Endpoint	                                              Description
POST	          /api/v1/users/{userId}/activities	                      Create new activity
DELETE	        /api/v1/activities/{activityId}	                        Soft delete
GET	            /api/v1/users/{userId}/activities/timeline?page=&size=	Paginated timeline

Default Port: 8080

Getting Started
Prerequisites
Make sure you have the following installed:

Java Development Kit (JDK)
Maven
MSSQL
Installation
Clone the repository:

git clone https://github.com/SudhakarPIP/user_activity.git
Navigate to the project directory:

cd user_activity
Build the project using Maven:

Run the application:

The application will start on the default port: http://localhost:8080.
