# Authentication API Documentation

## Overview
The OBUS Partner API provides JWT-based authentication with username and password login functionality.

## Base URL
```
http://localhost:8080/api/v1/auth
```

## Endpoints

### 1. User Registration

**POST** `/register`

Register a new user account.

#### Request Body
```json
{
  "username": "string (3-50 chars, required)",
  "email": "string (valid email, max 100 chars, required)",
  "password": "string (6-100 chars, required)",
  "firstName": "string (max 50 chars, required)",
  "lastName": "string (max 50 chars, required)"
}
```

#### Example Request
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Success Response (200)
```json
"User registered successfully"
```

#### Error Responses (400)
```json
"Username is already taken!"
```
```json
"Email is already in use!"
```
```json
"Registration failed: [error message]"
```

---

### 2. User Login

**POST** `/login`

Authenticate user and receive JWT token.

#### Request Body
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

#### Example Request
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

#### Success Response (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

#### Error Responses (400)
```json
"Invalid username or password"
```
```json
"Login failed: [error message]"
```

---

### 3. User Logout

**POST** `/logout`

Clear the current user's authentication session.

#### Example Request
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout
```

#### Success Response (200)
```json
"Logged out successfully"
```

---

## Using JWT Token

After successful login, include the JWT token in the Authorization header for protected endpoints:

```bash
curl -X GET http://localhost:8080/api/v1/partners \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## User Roles

The system supports the following user roles:
- `USER` - Regular user (default)
- `ADMIN` - Administrator
- `PARTNER` - Business partner

## Security Features

1. **Password Encryption**: Passwords are automatically encrypted using BCrypt
2. **JWT Tokens**: Secure token-based authentication
3. **Token Expiration**: Tokens expire after 24 hours (configurable)
4. **Input Validation**: All inputs are validated with appropriate constraints
5. **CORS Support**: Cross-origin requests are supported

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200` - Success
- `400` - Bad Request (validation errors, authentication failures)
- `401` - Unauthorized (invalid or expired token)
- `500` - Internal Server Error

## Testing

Use the provided test cases in `AuthControllerTest.java` to verify the authentication functionality.

## Configuration

JWT settings and root admin credentials can be configured in `application.yml`:

```yaml
jwt:
  secret: a8f5f167f44f4964e6c998dee827110c8f5f167f44f4964e6c998dee827110c8f5f167f44f4964e6c998dee827110c8f5f167f44f4964e6c998dee827110c
  expiration: 86400 # 24 hours in seconds

root-admin:
  username: rootadmin
  password: Root@2024!Sec
  email: rootadmin@obus.com
  firstName: Root
  lastName: Administrator
```

## Root Admin User

A root administrator user is automatically created on application startup with the following credentials:

- **Username**: `rootadmin`
- **Password**: `Root@2024!Sec`
- **Email**: `rootadmin@obus.com`
- **Role**: `ADMIN`

### Login as Root Admin

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rootadmin",
    "password": "Root@2024!Sec"
  }'
```

**Expected Response:**
```json
{
  "status": true,
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "rootadmin",
    "email": "rootadmin@obus.com",
    "role": "ADMIN"
  }
}
```

## Example Usage Flow

1. **Register a new user**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'
   ```

2. **Login to get JWT token**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"password123"}'
   ```

3. **Use the token for protected endpoints**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/partners \
     -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
   ```

**Expected Response:**
```json
{
  "status": true,
  "statusCode": 200,
  "message": "Partners retrieved successfully",
  "data": [
    {
      "id": 1,
      "uid": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
      "partnerCode": "P001",
      "businessName": "Sample Partner",
      "status": "ACTIVE",
      "tier": "BRONZE"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

4. **Logout when done**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/logout
   ```
