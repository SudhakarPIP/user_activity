# User Activity Service

A Spring Boot + MySQL microservice to record user activities, soft-delete them, and fetch a paginated timeline. Includes validation, global error handling, Swagger/OpenAPI documentation, and comprehensive tests.

## Features

- **Record user activities** (LOGIN, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE)
- **Soft delete activities** (data recovery and audit trails)
- **Paginated & sorted timeline** (newest first)
- **Swagger/OpenAPI documentation**
- **Input validation** with detailed error responses
- **SQL queries externalized** in XML for production readiness

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate) |
| API Docs | SpringDoc OpenAPI 2.2.0 |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc |
| Code Coverage | JaCoCo |

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8.0+ running and accessible

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/SudhakarPIP/user_activity.git
cd user_activity
```
## 1.1 Build the project using Maven:

## 1.2 Run the application:

The application will start on the default port: `http://localhost:8080`.


### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE pip;
```

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pip?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application starts at: **http://localhost:8080**

### 5. Verify Startup

On startup, the application validates the database connection and displays table information in the logs.

## API Documentation

### Swagger UI
Access interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### REST API Endpoints

Base path: `/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users/{userId}/activities` | Create a new activity |
| DELETE | `/activities/{activityId}` | Soft delete an activity |
| GET | `/users/{userId}/activities/timeline` | Get paginated timeline |

### Create Activity

**POST** `/api/v1/users/{userId}/activities`

Request Body:
```json
{
  "activityType": "LOGIN",
  "description": "User logged in from web",
  "metadata": "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome\"}"
}
```

Response (201 Created):
```json
{
  "id": 1001,
  "activityType": "LOGIN",
  "description": "User logged in from web",
  "metadata": "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome\"}",
  "createdAt": "2025-12-10T10:15:30Z"
}
```

### Delete Activity

**DELETE** `/api/v1/activities/{activityId}`

Response: `204 No Content`

### Get Timeline

**GET** `/api/v1/users/{userId}/activities/timeline?page=0&size=20`

Response (200 OK):
```json
{
  "userId": 123,
  "page": 0,
  "size": 20,
  "totalElements": 52,
  "totalPages": 3,
  "activities": [...]
}
```

### Activity Types

| Type | Description |
|------|-------------|
| `LOGIN` | User login event |
| `LOGOUT` | User logout event |
| `PASSWORD_CHANGE` | Password change event |
| `PROFILE_UPDATE` | Profile update event |

## Project Structure

```
src/
├── main/
│   ├── java/com/useractivity/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── enums/               # Enumerations
│   │   ├── config/              # Configuration classes
│   │   └── exception/           # Exception handling
│   └── resources/
│       ├── application.properties
│       ├── sql-queries.xml      # Externalized SQL queries
│       └── static/
└── test/
    └── java/com/useractivity/   # Test classes
```

## Database Schema

Table: `user_activities`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Auto-increment primary key |
| `user_id` | BIGINT | User identifier (indexed) |
| `activity_type` | VARCHAR | Activity type enum |
| `description` | TEXT | Activity description |
| `metadata` | TEXT | JSON metadata string |
| `created_at` | TIMESTAMP | Creation timestamp (indexed) |
| `updated_at` | TIMESTAMP | Last update timestamp |
| `is_deleted` | BOOLEAN | Soft delete flag |

## Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html
```

## Error Handling

All errors return consistent JSON responses:

```json
{
  "timestamp": "2025-12-10T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "path": "/api/v1/users/123/activities",
  "fieldErrors": [
    {
      "field": "activityType",
      "message": "activityType is required",
      "rejectedValue": null
    }
  ]
}
```

| Status Code | Description |
|-------------|-------------|
| 201 | Activity created successfully |
| 204 | Activity deleted successfully |
| 400 | Validation error or activity already deleted |
| 404 | Activity not found |
| 500 | Internal server error |

## Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.jpa.hibernate.ddl-auto` | validate | Schema management |
| `spring.jpa.show-sql` | true | Log SQL queries |
| `springdoc.swagger-ui.path` | /swagger-ui.html | Swagger UI path |

### SQL Queries

SQL queries are externalized in `src/main/resources/sql-queries.xml` for production readiness and easy maintenance.

## Performance

- Timeline API latency target: ≤ 800 ms
- Database indexes on `user_id` and `created_at` columns
- Pagination prevents large dataset loading

## License

This project is licensed under the MIT License.
