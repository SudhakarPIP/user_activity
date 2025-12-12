# User Activity Service - Technical Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Database Design](#database-design)
6. [API Documentation](#api-documentation)
7. [Data Models](#data-models)
8. [Error Handling](#error-handling)
9. [Testing](#testing)
10. [Configuration](#configuration)
11. [Deployment](#deployment)
12. [Performance Considerations](#performance-considerations)

---

## 1. Overview

The **User Activity Service** is a production-ready Spring Boot REST API module designed to manage user activities. It provides capabilities to:

- **Record** user activities with type, description, and optional metadata
- **Update** activities (via soft delete)
- **Retrieve** paginated and sorted activity timelines

The service follows REST principles, implements comprehensive validation, global error handling, and includes extensive test coverage.

### Key Features
- ✅ RESTful API design with versioning (`/api/v1`)
- ✅ Soft delete functionality for data retention
- ✅ Paginated timeline retrieval with sorting
- ✅ Request validation with detailed error messages
- ✅ Global exception handling with consistent error responses
- ✅ Swagger/OpenAPI documentation
- ✅ Comprehensive unit and integration tests
- ✅ Database indexing for performance optimization
- ✅ Transaction management for data consistency

---

## 2. Architecture

### Architecture Pattern
The application follows a **layered architecture** pattern:

┌─────────────────────────────────────┐
│ Controller Layer (REST API) │
│ UserActivityController │
└──────────────┬──────────────────────┘
│
┌──────────────▼──────────────────────┐
│ Service Layer (Business Logic) │
│ UserActivityService │
└──────────────┬──────────────────────┘
│
┌──────────────▼──────────────────────┐
│ Repository Layer (Data Access) │
│ UserActivityRepository (JPA) │
└──────────────┬──────────────────────┘
│
┌──────────────▼──────────────────────┐
│ Database (MySQL) │
│ user_activities table │
└─────────────────────────────────────┘
### Component Responsibilities| Layer | Component | Responsibility ||-------|-----------|----------------|| **Controller** | `UserActivityController` | Handles HTTP requests/responses, input validation, API documentation || **Service** | `UserActivityService` | Business logic, transaction management, data transformation || **Repository** | `UserActivityRepository` | Data access, custom queries, JPA operations || **Entity** | `UserActivity` | Domain model, database mapping || **DTO** | `CreateActivityRequest`, `ActivityResponse`, `TimelineResponse` | Data transfer objects for API contracts || **Exception** | `GlobalExceptionHandler`, `ResourceNotFoundException` | Centralized error handling |---## 3. Technology Stack### Core Framework- **Java**: 17- **Spring Boot**: 3.2.0- **Spring Framework Modules**:  - Spring Web MVC (REST API)  - Spring Data JPA (Data persistence)  - Spring Validation (Input validation)  - Spring Boot Test (Testing support)### Database & ORM- **MySQL**: 8.0+ (Production)- **H2**: 2.x (Testing)- **Hibernate**: 6.x (via Spring Data JPA)### Documentation- **SpringDoc OpenAPI**: 2.2.0- **Swagger UI**: Embedded### Testing- **JUnit**: 5.x- **Mockito**: 5.x- **MockMvc**: Spring Test integration- **JaCoCo**: Code coverage analysis### Build & Tools- **Maven**: 3.8+- **Lombok**: Code generation- **Jackson**: JSON processing---## 4. Project Structure
useractivity/
├── src/
│ ├── main/
│ │ ├── java/com/useractivity/
│ │ │ ├── controller/
│ │ │ │ └── UserActivityController.java # REST endpoints
│ │ │ ├── service/
│ │ │ │ └── UserActivityService.java # Business logic
│ │ │ ├── repository/
│ │ │ │ └── UserActivityRepository.java # Data access
│ │ │ ├── entity/
│ │ │ │ └── UserActivity.java # JPA entity
│ │ │ ├── dto/
│ │ │ │ ├── CreateActivityRequest.java # Request DTO
│ │ │ │ ├── ActivityResponse.java # Response DTO
│ │ │ │ ├── TimelineResponse.java # Paginated response
│ │ │ │ └── ErrorResponse.java # Error DTO
│ │ │ ├── enums/
│ │ │ │ └── ActivityType.java # Activity type enum
│ │ │ ├── exception/
│ │ │ │ ├── GlobalExceptionHandler.java # Global error handler
│ │ │ │ └── ResourceNotFoundException.java # Custom exception
│ │ │ └── UseractivityApplication.java # Main application
│ │ └── resources/
│ │ ├── application.properties # Configuration
│ │ └── schema.sql # Optional DDL
│ └── test/
│ ├── java/com/useractivity/
│ │ ├── controller/
│ │ │ └── UserActivityControllerTest.java # Controller tests
│ │ ├── service/
│ │ │ └── UserActivityServiceTest.java # Service tests
│ │ └── integration/
│ │ └── UserActivityIntegrationTest.java # Integration tests
│ └── resources/
│ └── application-test.properties # Test configuration
├── pom.xml # Maven configuration
└── README.md # Project documentation
---## 5. Database Design### Table: `user_activities`| Column | Type | Constraints | Description ||--------|------|-------------|-------------|| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique activity identifier || `user_id` | BIGINT | NOT NULL, INDEXED | Reference to user || `activity_type` | VARCHAR(50) | NOT NULL | Type of activity (enum) || `description` | TEXT | NOT NULL | Human-readable description || `metadata` | JSON / TEXT | NULLABLE | Additional activity data (JSON format) || `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Activity creation time || `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time || `is_deleted` | BOOLEAN | NOT NULL, DEFAULT FALSE | Soft delete flag |### Indexes1. **Primary Index**: `id` (auto-created)2. **Index on `user_id`**: `idx_user_id` - Optimizes timeline queries by user3. **Index on `created_at`**: `idx_created_at` - Optimizes sorting and filtering by date### Database Schema SQLCREATE TABLE IF NOT EXISTS user_activities (    id BIGINT AUTO_INCREMENT PRIMARY KEY,    user_id BIGINT NOT NULL,    activity_type VARCHAR(50) NOT NULL,    description TEXT NOT NULL,    metadata JSON,    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,    INDEX idx_user_id (user_id),    INDEX idx_created_at (created_at));### Soft Delete StrategyThe application uses **soft delete** instead of hard delete:- Records are marked as deleted (`is_deleted = true`) rather than physically removed- Deleted records are excluded from timeline queries- Allows for data recovery and audit trails---## 6. API Documentation### Base URL
Soft Delete Strategy
The application uses soft delete instead of hard delete:
Records are marked as deleted (is_deleted = true) rather than physically removed
Deleted records are excluded from timeline queries
Allows for data recovery and audit trails
6. API Documentation
Base URL
http://locar UIi/v1ess interactive API documentation at:
Swagger UI
Access interactive API documentation at:
http://localhost:8080/swagger-ui.html
OpenAPI JSON
htt-### 6.1. Creat80/api-docs**Endpoint**: `POST /users/{userId}/activities`**Description**: Creates a new user activity record.**Path Parameters**:- `userId` (Long, required): The ID of the user performing the activity**Request Body**:son{  "activityType": "LOGIN",  "description": "User logged in from web",  "metadata": "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome on Windows\"}"}**Request Schema**:| Field | Type | Required | Description ||-------|------|----------|-------------|| `activityType` | Enum | Yes | Activity type (LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE) || `description` | String | Yes | Human-readable description || `metadata` | String | No | JSON string containing additional data |**Response**: `201 Created`**Response Body**:n{  "id": 1001,  "activityType": "LOGIN",  "description": "User logged in from web",  "metadata": "{\"ip\":\"192.168.1.10\"}",  "createdAt": "2025-12-10T10:15:30Z"}**Error Responses**:- `400 Bad Request`: Validation failure (missing required fields, invalid enum)- `500 Internal Server Error`: Server error**Example cURL**:curl -X POST "http://localhost:8080/api/v1/users/123/activities" \  -H "Content-Type: application/json" \  -d '{    "activityType": "LOGIN",    "description": "User logged in from web",    "metadata": "{\"ip\":\"192.168.1.10\"}"  }'---### 6.2. Delete Activity**Endpoint**: `DELETE /activities/{activityId}`**Description**: Soft deletes an activity by setting `is_deleted = true`.**Path Parameters**:- `activityId` (Long, required): The ID of the activity to delete**Response**: `204 No Content` (empty body)**Error Responses**:- `404 Not Found`: Activity not found- `400 Bad Request`: Activity already deleted- `500 Internal Server Error`: Server error**Example cURL**:hcurl -X DELETE "http://localhost:8080/api/v1/activities/1001"---### 6.3. Get Timeline**Endpoint**: `GET /users/{userId}/activities/timeline`**Description**: Retrieves a paginated and sorted list of user activities. Results are sorted by `created_at` in descending order (newest first) and exclude soft-deleted records.**Path Parameters**:- `userId` (Long, required): The ID of the user**Query Parameters**:- `page` (int, optional, default: 0): Page number (0-indexed)- `size` (int, optional, default: 20): Number of items per page**Response**: `200 OK`**Response Body**:{  "userId": 123,  "page": 0,  "size": 20,  "totalElements": 52,  "totalPages": 3,  "activities": [    {      "id": 1001,      "activityType": "LOGIN",      "description": "User logged in from web",      "metadata": "{\"ip\":\"192.168.1.10\"}",      "createdAt": "2025-12-10T10:15:30Z"    },    {      "id": 1000,      "activityType": "PROFILE_UPDATE",      "description": "User updated profile picture",      "metadata": null,      "createdAt": "2025-12-10T09:30:15Z"    }  ]}**Response Schema**:| Field | Type | Description ||-------|------|-------------|| `userId` | Long | The user ID || `page` | Integer | Current page number || `size` | Integer | Page size || `totalElements` | Long | Total number of activities (non-deleted) || `totalPages` | Integer | Total number of pages || `activities` | Array | List of activity objects |**Error Responses**:- `400 Bad Request`: Invalid pagination parameters- `500 Internal Server Error`: Server error**Example cURL**:curl -X GET "http://localhost:8080/api/v1/users/123/activities/timeline?page=0&size=20"---## 7. Data Models### 7.1. ActivityType Enumpublic enum ActivityType {    LOGIN,              // User login event    LOGOUT,             // User logout event    PASSWORD_CHANGE,    // Password change event    PROFILE_UPDATE      // Profile update event}### 7.2. CreateActivityRequest DTOva{  "activityType": ActivityType,  // Required, not null  "description": String,          // Required, not blank  "metadata": String              // Optional, JSON string}**Validation Rules**:- `activityType`: Must not be null, must be a valid enum value- `description`: Must not be null or blank (trimmed)- `metadata`: Optional, can be null or empty### 7.3. ActivityResponse DTO{  "id": Long,  "activityType": String,  "description": String,  "metadata": String,  "createdAt": LocalDateTime  // ISO-8601 format: "yyyy-MM-dd'T'HH:mm:ss'Z'"}### 7.4. TimelineResponse DTO{  "userId": Long,  "page": Integer,  "size": Integer,  "totalElements": Long,  "totalPages": Integer,  "activities": List<ActivityResponse>}### 7.5. ErrorResponse DTO{  "timestamp": LocalDateTime,  "status": Integer,           // HTTP status code  "error": String,             // Error type  "message": String,           // Error message  "path": String,              // Request path  "fieldErrors": [             // Only present for validation errors    {      "field": String,      "message": String,      "rejectedValue": Object    }  ]}**Example Error Response**:{  "timestamp": "2025-12-10T10:30:00",  "status": 400,  "error": "Validation Failed",  "message": "Input validation failed",  "path": "/api/v1/users/123/activities",  "fieldErrors": [    {      "field": "activityType",      "message": "activityType is required",      "rejectedValue": null    }  ]}---## 8. Error Handling### Global Exception HandlerThe application uses `@RestControllerAdvice` to centralize exception handling:#### Exception Types Handled1. **MethodArgumentNotValidException** (Validation Errors)   - **Status**: 400 Bad Request   - **Response**: Includes `fieldErrors` array with validation details2. **ResourceNotFoundException** (Not Found)   - **Status**: 404 Not Found   - **Message**: "Activity not found with id: {id}"3. **RuntimeException** (Business Logic Errors)   - **Status**: Determined by message content:     - Contains "not found" → 404 Not Found     - Contains "already deleted" → 400 Bad Request     - Otherwise → 500 Internal Server Error4. **Exception** (Generic Errors)   - **Status**: 500 Internal Server Error   - **Message**: "An unexpected error occurred"### Error Response StructureAll error responses follow a consistent structure:{  "timestamp": "2025-12-10T10:30:00",  "status": 400,  "error": "Error Type",  "message": "Error message",  "path": "/api/v1/endpoint"}---## 9. Testing### Test StructureThe project includes three levels of testing:1. **Unit Tests** (`UserActivityServiceTest`)   - Tests service layer business logic   - Uses Mockito for mocking dependencies   - Covers success and failure scenarios2. **Controller Tests** (`UserActivityControllerTest`)   - Tests REST endpoint behavior   - Uses MockMvc for HTTP request simulation   - Validates request/response handling and HTTP status codes3. **Integration Tests** (`UserActivityIntegrationTest`)   - Tests complete flow from controller to database   - Uses H2 in-memory database   - Covers end-to-end scenarios### Running Tests# Run all testsmvn test# Run tests with coverage reportmvn clean test jacoco:report# View coverage report# Open: target/site/jacoco/index.html### Test CoverageTarget: **≥ 80% code coverage**Coverage includes:- ✅ Create activity flow- ✅ Delete activity flow- ✅ Timeline retrieval flow- ✅ Validation failures- ✅ Not found errors- ✅ Already deleted errors### Test ConfigurationTest profile uses H2 in-memory database (`application-test.properties`):opertiesspring.datasource.url=jdbc:h2:mem:testdbspring.datasource.driver-class-name=org.h2.Driverspring.jpa.hibernate.ddl-auto=create-dropspring.jpa.show-sql=true---## 10. Configuration### Application PropertiesMain configuration file: `src/main/resources/application.properties`# Applicationspring.application.name=useractivity# Database Configurationspring.datasource.url=jdbc:mysql://localhost:3306/pip?useSSL=false&serverTimezone=UTCspring.datasource.username=rootspring.datasource.password=******spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver# JPA/Hibernate Configurationspring.jpa.hibernate.ddl-auto=updatespring.jpa.show-sql=truespring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialectspring.jpa.properties.hibernate.format_sql=true# Jackson Configuration (Date/Time formatting)spring.jackson.serialization.write-dates-as-timestamps=falsespring.jackson.time-zone=UTC# Swagger Configurationspringdoc.api-docs.path=/api-docsspringdoc.swagger-ui.path=/swagger-ui.html# Server Configurationserver.port=8080### Configuration Properties Explained| Property | Description ||----------|-------------|| `spring.jpa.hibernate.ddl-auto=update` | Automatically updates database schema on startup || `spring.jpa.show-sql=true` | Logs SQL queries (disable in production) || `spring.jackson.time-zone=UTC` | Uses UTC for all date/time serialization || `springdoc.swagger-ui.path` | Swagger UI access path |### Environment-Specific ConfigurationFor different environments, use Spring profiles:# Developmentmvn spring-boot:run -Dspring-boot.run.profiles=dev# Productionmvn spring-boot:run -Dspring-boot.run.profiles=prod---## 11. Deployment### Build Process# Clean and compilemvn clean compile# Run testsmvn test# Package application (WAR file)mvn clean package# Skip tests during buildmvn clean package -DskipTests### Deployment Options#### Option 1: Standalone JAR (Recommended for Spring Boot)Change packaging in `pom.xml`:<packaging>jar</packaging>Run:java -jar target/useractivity-0.0.1-SNAPSHOT.jar#### Option 2: WAR Deployment (Current)Deploy WAR file to external Tomcat/JBoss server:# Build WARmvn clean package# Copy WAR to servercp target/useractivity-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/#### Option 3: Docker (Recommended for Production)Create `Dockerfile`:FROM openjdk:17-jdk-slimVOLUME /tmpCOPY target/useractivity-0.0.1-SNAPSHOT.war app.warENTRYPOINT ["java","-jar","/app.war"]Build and run:docker build -t useractivity-service .docker run -p 8080:8080 useractivity-service### Pre-Deployment Checklist- [ ] Update database connection credentials- [ ] Set `spring.jpa.show-sql=false` for production- [ ] Configure proper logging levels- [ ] Verify database indexes are created- [ ] Run integration tests against production database- [ ] Review security settings- [ ] Set up monitoring and logging- [ ] Configure connection pooling- [ ] Review JVM memory settings---## 12. Performance Considerations### Performance Targets| Metric | Target | Notes ||--------|--------|-------|| Timeline API Latency | ≤ 800 ms | With proper indexing || Code Quality | No blocker/critical Sonar issues | Use SonarQube analysis |### Database Optimization1. **Indexes**: Already configured on `user_id` and `created_at`2. **Query Optimization**: Repository uses indexed fields in WHERE and ORDER BY clauses3. **Pagination**: Prevents loading large datasets into memory### Performance MonitoringMonitor the following:- Database query execution times- API response times- Memory usage- Connection pool utilization### Recommendations1. **Connection Pooling**: Configure HikariCP (Spring Boot default) with appropriate pool size2. **Caching**: Consider caching frequently accessed user timelines3. **Database**: Monitor slow query logs and optimize as needed4. **Scaling**: Use read replicas for timeline queries if needed---## Additional Resources### Useful Commands# Start MySQL (if using Docker)docker run --name mysql-useractivity -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pip -p 3306:3306 -d mysql:8.0# View application logstail -f logs/application.log# Check database connectionmysql -h localhost -u root -p pip### Support & MaintenanceFor issues or questions:1. Check Swagger UI documentation2. Review application logs3. Verify database connectivity4. Run test suite to validate functionality---## Version History- **v0.0.1-SNAPSHOT**: Initial release  - Core CRUD operations  - Paginated timeline  - Soft delete functionality  - Comprehensive test coverage  - Swagger documentation---**Document Version**: 1.0  **Last Updated**: 2025-12-10
6.1. Create Activity
Endpoint: POST /users/{userId}/activities
Description: Creates a new user activity record.
Path Parameters:
userId (Long, required): The ID of the user performing the activity
Request Body:
{  "activityType": "LOGIN",  "description": "User logged in from web",  "metadata": "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome on Windows\"}"}
Request Schema:
Field	Type	Required	Description
activityType	Enum	Yes	Activity type (LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE)
description	String	Yes	Human-readable description
metadata	String	No	JSON string containing additional data
Response: 201 Created
Response Body:
{  "id": 1001,  "activityType": "LOGIN",  "description": "User logged in from web",  "metadata": "{\"ip\":\"192.168.1.10\"}",  "createdAt": "2025-12-10T10:15:30Z"}
Error Responses:
400 Bad Request: Validation failure (missing required fields, invalid enum)
500 Internal Server Error: Server error
Example cURL:
curl -X POST "http://localhost:8080/api/v1/users/123/activities" \  -H "Content-Type: application/json" \  -d '{    "activityType": "LOGIN",    "description": "User logged in from web",    "metadata": "{\"ip\":\"192.168.1.10\"}"  }'
6.2. Delete Activity
Endpoint: DELETE /activities/{activityId}
Description: Soft deletes an activity by setting is_deleted = true.
Path Parameters:
activityId (Long, required): The ID of the activity to delete
Response: 204 No Content (empty body)
Error Responses:
404 Not Found: Activity not found
400 Bad Request: Activity already deleted
500 Internal Server Error: Server error
Example cURL:
curl -X DELETE "http://localhost:8080/api/v1/activities/1001"
6.3. Get Timeline
Endpoint: GET /users/{userId}/activities/timeline
Description: Retrieves a paginated and sorted list of user activities. Results are sorted by created_at in descending order (newest first) and exclude soft-deleted records.
Path Parameters:
userId (Long, required): The ID of the user
Query Parameters:
page (int, optional, default: 0): Page number (0-indexed)
size (int, optional, default: 20): Number of items per page
Response: 200 OK
Response Body:
{  "userId": 123,  "page": 0,  "size": 20,  "totalElements": 52,  "totalPages": 3,  "activities": [    {      "id": 1001,      "activityType": "LOGIN",      "description": "User logged in from web",      "metadata": "{\"ip\":\"192.168.1.10\"}",      "createdAt": "2025-12-10T10:15:30Z"    },    {      "id": 1000,      "activityType": "PROFILE_UPDATE",      "description": "User updated profile picture",      "metadata": null,      "createdAt": "2025-12-10T09:30:15Z"    }  ]}
Response Schema:
Field	Type	Description
userId	Long	The user ID
page	Integer	Current page number
size	Integer	Page size
totalElements	Long	Total number of activities (non-deleted)
totalPages	Integer	Total number of pages
activities	Array	List of activity objects
Error Responses:
400 Bad Request: Invalid pagination parameters
500 Internal Server Error: Server error
Example cURL:
curl -X GET "http://localhost:8080/api/v1/users/123/activities/timeline?page=0&size=20"
7. Data Models
7.1. ActivityType Enum
public enum ActivityType {    LOGIN,              // User login event    LOGOUT,             // User logout event    PASSWORD_CHANGE,    // Password change event    PROFILE_UPDATE      // Profile update event}
7.2. CreateActivityRequest DTO
{  "activityType": ActivityType,  // Required, not null  "description": String,          // Required, not blank  "metadata": String              // Optional, JSON string}
Validation Rules:
activityType: Must not be null, must be a valid enum value
description: Must not be null or blank (trimmed)
metadata: Optional, can be null or empty
7.3. ActivityResponse DTO
{  "id": Long,  "activityType": String,  "description": String,  "metadata": String,  "createdAt": LocalDateTime  // ISO-8601 format: "yyyy-MM-dd'T'HH:mm:ss'Z'"}
7.4. TimelineResponse DTO
{  "userId": Long,  "page": Integer,  "size": Integer,  "totalElements": Long,  "totalPages": Integer,  "activities": List<ActivityResponse>}
7.5. ErrorResponse DTO
{  "timestamp": LocalDateTime,  "status": Integer,           // HTTP status code  "error": String,             // Error type  "message": String,           // Error message  "path": String,              // Request path  "fieldErrors": [             // Only present for validation errors    {      "field": String,      "message": String,      "rejectedValue": Object    }  ]}
Example Error Response:
{  "timestamp": "2025-12-10T10:30:00",  "status": 400,  "error": "Validation Failed",  "message": "Input validation failed",  "path": "/api/v1/users/123/activities",  "fieldErrors": [    {      "field": "activityType",      "message": "activityType is required",      "rejectedValue": null    }  ]}
8. Error Handling
Global Exception Handler
The application uses @RestControllerAdvice to centralize exception handling:
Exception Types Handled
MethodArgumentNotValidException (Validation Errors)
Status: 400 Bad Request
Response: Includes fieldErrors array with validation details
ResourceNotFoundException (Not Found)
Status: 404 Not Found
Message: "Activity not found with id: {id}"
RuntimeException (Business Logic Errors)
Status: Determined by message content:
Contains "not found" → 404 Not Found
Contains "already deleted" → 400 Bad Request
Otherwise → 500 Internal Server Error
Exception (Generic Errors)
Status: 500 Internal Server Error
Message: "An unexpected error occurred"
Error Response Structure
All error responses follow a consistent structure:
{  "timestamp": "2025-12-10T10:30:00",  "status": 400,  "error": "Error Type",  "message": "Error message",  "path": "/api/v1/endpoint"}
9. Testing
Test Structure
The project includes three levels of testing:
Unit Tests (UserActivityServiceTest)
Tests service layer business logic
Uses Mockito for mocking dependencies
Covers success and failure scenarios
Controller Tests (UserActivityControllerTest)
Tests REST endpoint behavior
Uses MockMvc for HTTP request simulation
Validates request/response handling and HTTP status codes
Integration Tests (UserActivityIntegrationTest)
Tests complete flow from controller to database
Uses H2 in-memory database
Covers end-to-end scenarios
Running Tests
# Run all testsmvn test# Run tests with coverage reportmvn clean test jacoco:report# View coverage report# Open: target/site/jacoco/index.html
Test Coverage
Target: ≥ 80% code coverage
Coverage includes:
✅ Create activity flow
✅ Delete activity flow
✅ Timeline retrieval flow
✅ Validation failures
✅ Not found errors
✅ Already deleted errors
Test Configuration
Test profile uses H2 in-memory database (application-test.properties):
spring.datasource.url=jdbc:h2:mem:testdbspring.datasource.driver-class-name=org.h2.Driverspring.jpa.hibernate.ddl-auto=create-dropspring.jpa.show-sql=true
10. Configuration
Application Properties
Main configuration file: src/main/resources/application.properties
# Applicationspring.application.name=useractivity# Database Configurationspring.datasource.url=jdbc:mysql://localhost:3306/pip?useSSL=false&serverTimezone=UTCspring.datasource.username=rootspring.datasource.password=******spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver# JPA/Hibernate Configurationspring.jpa.hibernate.ddl-auto=updatespring.jpa.show-sql=truespring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialectspring.jpa.properties.hibernate.format_sql=true# Jackson Configuration (Date/Time formatting)spring.jackson.serialization.write-dates-as-timestamps=falsespring.jackson.time-zone=UTC# Swagger Configurationspringdoc.api-docs.path=/api-docsspringdoc.swagger-ui.path=/swagger-ui.html# Server Configurationserver.port=8080
Configuration Properties Explained
Property	Description
spring.jpa.hibernate.ddl-auto=update	Automatically updates database schema on startup
spring.jpa.show-sql=true	Logs SQL queries (disable in production)
spring.jackson.time-zone=UTC	Uses UTC for all date/time serialization
springdoc.swagger-ui.path	Swagger UI access path
Environment-Specific Configuration
For different environments, use Spring profiles:
# Developmentmvn spring-boot:run -Dspring-boot.run.profiles=dev# Productionmvn spring-boot:run -Dspring-boot.run.profiles=prod
11. Deployment
Build Process
# Clean and compilemvn clean compile# Run testsmvn test# Package application (WAR file)mvn clean package# Skip tests during buildmvn clean package -DskipTests
Deployment Options
Option 1: Standalone JAR (Recommended for Spring Boot)
Change packaging in pom.xml:
<packaging>jar</packaging>
Run:
java -jar target/user
Option 2: WAR Deployment (Current)
Deploy WAR file to external Tomcat/JBoss server:
# Build WARmvn clean package# Copy WAR to servercp target/useractivity-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/
Option 3: Docker (Recommended for Production)
Create Dockerfile:
FROM openjdk:17-jdk-slimVOLUME /tmpCOPY target/useractivity-0.0.1-SNAPSHOT.war app.warENTRYPOINT ["java","-jar","/app.war"]
Build and run:
docker build -t useractivity-service .docker run -p 8080:8080 useractivity-service
Pre-Deployment Checklist
[ ] Update database connection credentials
[ ] Set spring.jpa.show-sql=false for production
[ ] Configure proper logging levels
[ ] Verify database indexes are created
[ ] Run integration tests against production database
[ ] Review security settings
[ ] Set up monitoring and logging
[ ] Configure connection pooling
[ ] Review JVM memory settings
12. Performance Considerations
Performance Targets
Metric	Target	Notes
Timeline API Latency	≤ 800 ms	With proper indexing
Code Quality	No blocker/critical Sonar issues	Use SonarQube analysis
Database Optimization
Indexes: Already configured on user_id and created_at
Query Optimization: Repository uses indexed fields in WHERE and ORDER BY clauses
Pagination: Prevents loading large datasets into memory
Performance Monitoring
Monitor the following:
Database query execution times
API response times
Memory usage
Connection pool utilization
Recommendations
Connection Pooling: Configure HikariCP (Spring Boot default) with appropriate pool size
Caching: Consider caching frequently accessed user timelines
Database: Monitor slow query logs and optimize as needed
Scaling: Use read replicas for timeline queries if needed
Additional Resources
Useful Commands
# Start MySQL (if using Docker)docker run --name mysql-useractivity -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pip -p 3306:3306 -d mysql:8.0# View application logstail -f logs/application.log# Check database connectionmysql -h localhost -u root -p pip
Support & Maintenance
For issues or questions:
Check Swagger UI documentation
Review application logs
Verify database connectivity
Run test suite to validate functionality
Version History
v0.0.1-SNAPSHOT: Initial release
Core CRUD operations
Paginated timeline
Soft delete functionality
Comprehensive test coverage
Swagger documentation
Document Version: 1.0

