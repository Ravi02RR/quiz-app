# Quiz Application Backend

A backend-only quiz application built with Spring Boot 3.5.6, featuring JWT authentication, CRUD operations, pagination, and filtering.

## Features

- JWT-based authentication with separate tokens for USER and ADMIN roles
- Quiz management (Admin only)
- Quiz access with pagination and filtering (User and Admin)
- Quiz attempt submission and result retrieval (User and Admin)
- H2 in-memory database
- RESTful APIs returning JSON

## Technology Stack

- Spring Boot 3.5.6
- Spring Security (JWT)
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- Maven

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

**Important: Change the JDBC URL in the H2 console login screen to:**
- **JDBC URL**: `jdbc:h2:mem:quizdb` (NOT the default `jdbc:h2:~/test`)
- **Username**: `sa`
- **Password**: (leave empty)

**Note**: The H2 console will show a default JDBC URL of `jdbc:h2:~/test`. You MUST change this to `jdbc:h2:mem:quizdb` to connect to the application's in-memory database.

## API Documentation

### 1. Authentication APIs

#### Register a New User

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "username": "john_user",
  "password": "password123",
  "role": "USER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_user",
  "role": "USER"
}
```

#### Register an Admin

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123",
  "role": "ADMIN"
}
```

#### Login

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "john_user",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_user",
  "role": "USER"
}
```

---

### 2. Quiz Management APIs (Admin Only)

**Note:** Include the JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

#### Create a Quiz

**Endpoint:** `POST /quizzes`

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Java Programming Quiz",
  "category": "Programming",
  "difficulty": "EASY"
}
```

**Difficulty Values:** `EASY`, `MEDIUM`, `HARD`

**Response:**
```json
{
  "id": 1,
  "title": "Java Programming Quiz",
  "category": "Programming",
  "difficulty": "EASY",
  "createdDate": "2025-10-07T09:30:00",
  "questions": []
}
```

#### Add Questions to a Quiz

**Endpoint:** `POST /quizzes/{quizId}/questions`

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "text": "What is the capital of France?",
    "options": ["London", "Berlin", "Paris", "Madrid"],
    "correctAnswerIndex": 2
  },
  {
    "text": "Which planet is known as the Red Planet?",
    "options": ["Venus", "Mars", "Jupiter", "Saturn"],
    "correctAnswerIndex": 1
  }
]
```

**Response:** Quiz object with questions

---

### 3. Quiz Access APIs (User and Admin)

#### Get All Quizzes (with Pagination and Filtering)

**Endpoint:** `GET /quizzes`

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 5) - Page size
- `category` (optional) - Filter by category
- `difficulty` (optional) - Filter by difficulty (EASY, MEDIUM, HARD)

**Examples:**

Get first page with default size:
```
GET /quizzes?page=0&size=5
```

Filter by category:
```
GET /quizzes?category=Programming
```

Filter by difficulty:
```
GET /quizzes?difficulty=EASY
```

Filter by both:
```
GET /quizzes?page=0&size=5&category=Programming&difficulty=EASY
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Java Programming Quiz",
      "category": "Programming",
      "difficulty": "EASY",
      "createdDate": "2025-10-07T09:30:00",
      "questions": []
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5
  },
  "totalElements": 1,
  "totalPages": 1
}
```

#### Get Quiz Details with Questions

**Endpoint:** `GET /quizzes/{quizId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "title": "Java Programming Quiz",
  "category": "Programming",
  "difficulty": "EASY",
  "createdDate": "2025-10-07T09:30:00",
  "questions": [
    {
      "id": 1,
      "text": "What is the capital of France?",
      "options": ["London", "Berlin", "Paris", "Madrid"]
    },
    {
      "id": 2,
      "text": "Which planet is known as the Red Planet?",
      "options": ["Venus", "Mars", "Jupiter", "Saturn"]
    }
  ]
}
```

**Note:** The `correctAnswerIndex` is NOT included in the response for security reasons.

---

### 4. Quiz Attempt and Results APIs (User and Admin)

#### Submit Quiz Attempt

**Endpoint:** `POST /quizzes/{quizId}/attempt`

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "answers": {
    "1": 2,
    "2": 1
  }
}
```

**Format:** `"questionId": selectedAnswerIndex`

**Response:**
```json
{
  "id": 1,
  "quizId": 1,
  "quizTitle": "Java Programming Quiz",
  "score": 100.0,
  "totalQuestions": 2,
  "correctAnswers": 2,
  "submittedAt": "2025-10-07T10:00:00",
  "userAnswers": {
    "1": 2,
    "2": 1
  }
}
```

#### Get Quiz Attempt Result

**Endpoint:** `GET /results/{attemptId}`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "quizId": 1,
  "quizTitle": "Java Programming Quiz",
  "score": 100.0,
  "totalQuestions": 2,
  "correctAnswers": 2,
  "submittedAt": "2025-10-07T10:00:00",
  "userAnswers": {
    "1": 2,
    "2": 1
  }
}
```

---

## Testing with cURL

### 1. Register an Admin

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "role": "ADMIN"
  }'
```

### 2. Register a User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_user",
    "password": "password123",
    "role": "USER"
  }'
```

### 3. Login as Admin

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Save the token from the response.

### 4. Create a Quiz (Admin)

```bash
curl -X POST http://localhost:8080/quizzes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "title": "Java Programming Quiz",
    "category": "Programming",
    "difficulty": "EASY"
  }'
```

### 5. Add Questions to Quiz (Admin)

```bash
curl -X POST http://localhost:8080/quizzes/1/questions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '[
    {
      "text": "What is the capital of France?",
      "options": ["London", "Berlin", "Paris", "Madrid"],
      "correctAnswerIndex": 2
    },
    {
      "text": "Which planet is known as the Red Planet?",
      "options": ["Venus", "Mars", "Jupiter", "Saturn"],
      "correctAnswerIndex": 1
    }
  ]'
```

### 6. Login as User

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_user",
    "password": "password123"
  }'
```

### 7. Get All Quizzes with Filtering (User)

```bash
curl -X GET "http://localhost:8080/quizzes?page=0&size=5&category=Programming&difficulty=EASY" \
  -H "Authorization: Bearer <user_token>"
```

### 8. Get Quiz Details (User)

```bash
curl -X GET http://localhost:8080/quizzes/1 \
  -H "Authorization: Bearer <user_token>"
```

### 9. Submit Quiz Attempt (User)

```bash
curl -X POST http://localhost:8080/quizzes/1/attempt \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <user_token>" \
  -d '{
    "answers": {
      "1": 2,
      "2": 1
    }
  }'
```

### 10. Get Attempt Result (User)

```bash
curl -X GET http://localhost:8080/results/1 \
  -H "Authorization: Bearer <user_token>"
```

---

## Security Features

- **JWT Authentication:** All endpoints (except `/auth/**` and `/h2-console/**`) require JWT authentication
- **Role-Based Access Control:**
  - Admin can create quizzes and add questions
  - Both User and Admin can view quizzes and attempt them
  - Users can only view their own attempt results
- **Separate JWT tokens** for USER and ADMIN roles with role information embedded in the token
- **Password encryption** using BCrypt

## Database Schema

### User
- id (Long, PK)
- username (String, unique)
- password (String, encrypted)
- role (Enum: USER, ADMIN)

### Quiz
- id (Long, PK)
- title (String)
- category (String)
- difficulty (Enum: EASY, MEDIUM, HARD)
- createdDate (LocalDateTime)

### Question
- id (Long, PK)
- quiz_id (Long, FK)
- text (String)
- options (String, JSON array)
- correctAnswerIndex (Integer)

### Attempt
- id (Long, PK)
- user_id (Long, FK)
- quiz_id (Long, FK)
- score (Double)
- answers (String, JSON)
- submittedAt (LocalDateTime)

---

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Notes

- JWT tokens expire after 24 hours (86400000 ms)
- The H2 database is in-memory and will be reset when the application restarts
- Question options are stored as JSON arrays in the database
- User answers are stored as JSON objects in the Attempt table
- The correct answer index is not exposed in the quiz details API for security
