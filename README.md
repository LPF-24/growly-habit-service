# Habit Service

`habit-service` is responsible for managing user habits in the **Growly** microservices system. It provides a REST API for creating, updating, deleting, and retrieving habits associated with authenticated users.

## 📌 Responsibilities

- Create, update, and delete habits
- Associate habits with authenticated users
- Fetch and list habits
- Track status and progress (optional future feature)
- Validate user access via JWT

## 🔐 Security

- All endpoints require JWT-based authentication
- Token must be provided via `Authorization: Bearer <token>`
- User ID is resolved from the token and used for ownership checks

## 🔁 Examples of API Endpoints

| Method | Endpoint              | Description                  |
|--------|------------------------|------------------------------|
| `POST` | `/habits/create-habit`       | Create a new habit           |
| `PATCH`| `/habits/update/{id}`  | Update an existing habit     |
| `DELETE`| `/habits/delete/{id}` | Delete habit by ID           |
| `GET`  | `/habits/all-habits`         | Get current user's habits    |
| `GET`  | `/habits/{id}`         | Get habit by ID (owned only) |

## 📚 Documentation

Interactive API available at:
```
http://localhost:8082/swagger-ui.html
```

## 🧰 Technologies

- Java 17
- Spring Boot
- Spring Web
- Spring Security
- Spring Validation
- JWT (via Spring Security filter)
- JPA + PostgreSQL
- Docker
- Kafka
- JUnit 5
- Mockito

## ⚙️ Configuration

Example environment variables:

```env
JWT_SECRET=internship
```

## 🐳 Docker

To build and run the service:

```bash
docker build -t habit-service .
docker run -p 8082:8082 --env-file .env habit-service
```

## 🧪 Testing
Unit tests for service and validation layers

Integration tests for controller endpoints (in progress)

Uses Spring Boot Test and Testcontainers (optional)

## 📚 Notes
JWT is validated internally — no external auth call

User data (e.g., userId) is extracted from the JWT

Works behind gateway-service

## 🔗 Related
Part of the growly-infra project.