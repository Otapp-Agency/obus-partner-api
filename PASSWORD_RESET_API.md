# Password Reset API Documentation

This document describes the password reset and change functionality endpoints.

## Endpoints

### 1. Change Password (Authenticated Users)

**Endpoint:** `POST /v1/auth/password/change`

**Description:** Allows authenticated users to change their password by providing their current password.

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
    "currentPassword": "currentPassword123",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
}
```

**Response:**
```json
{
    "status": true,
    "statusCode": 200,
    "message": "Password changed successfully",
    "data": null
}
```

**Error Responses:**
- `400` - Current password is incorrect
- `400` - New password and confirm password do not match
- `401` - User not authenticated

**cURL Example:**
```bash
curl -X POST http://localhost:8080/v1/auth/password/change \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "currentPassword": "currentPassword123",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
  }'
```

---

### 2. Request Password Reset

**Endpoint:** `POST /v1/auth/password/reset`

**Description:** Sends a password reset email to the user if an account exists with the provided email.

**Request Body:**
```json
{
    "email": "user@example.com"
}
```

**Response:**
```json
{
    "status": true,
    "statusCode": 200,
    "message": "If an account with that email exists, a password reset link has been sent",
    "data": null
}
```

**Note:** For security reasons, the same response is returned whether the email exists or not.

**cURL Example:**
```bash
curl -X POST http://localhost:8080/v1/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

---

### 3. Confirm Password Reset

**Endpoint:** `POST /v1/auth/password/confirm-reset`

**Description:** Resets the user's password using a valid reset token from the email.

**Request Body:**
```json
{
    "token": "reset_token_from_email",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
}
```

**Response:**
```json
{
    "status": true,
    "statusCode": 200,
    "message": "Password reset successfully",
    "data": null
}
```

**Error Responses:**
- `400` - Invalid or expired reset token
- `400` - Reset token has already been used
- `400` - New password and confirm password do not match

**cURL Example:**
```bash
curl -X POST http://localhost:8080/v1/auth/password/confirm-reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
  }'
```

---

## Password Requirements

- Minimum length: 6 characters
- Maximum length: 100 characters
- Must match confirmation password

## Token Expiration

- Password reset tokens expire after 24 hours (configurable via `app.password-reset.token-expiration-hours`)
- Tokens can only be used once
- Multiple reset requests invalidate previous tokens

## Security Features

- Secure random token generation
- Token expiration
- Single-use tokens
- Rate limiting (if implemented)
- No user enumeration (same response for existing/non-existing emails)
- **Limited scope tokens**: Users with `requireResetPassword = true` receive limited tokens that only allow password change operations

## Limited Token Security

When a user logs in with `requireResetPassword = true`, they receive a **limited scope token** that:

### ✅ **Allowed Operations:**
- `POST /v1/auth/password/change` - Change password
- `POST /v1/auth/logout` - Logout

### 🔄 **Refresh Token Behavior:**
- **No refresh token issued** when `requireResetPassword = true`
- **Limited tokens expire normally** - user must complete password change
- **No token extension possible** until password is changed

### ❌ **Blocked Operations:**
- All other API endpoints return `403 Forbidden`
- Normal business operations are restricted
- Agent operations are blocked

### **Example Response for Blocked Operations:**
```json
{
    "status": false,
    "statusCode": 403,
    "message": "Access denied. Password change required.",
    "data": null
}
```

### **Token Scope Flow:**
1. User logs in with `requireResetPassword = true`
2. System issues limited token with scope `"password_change"` (no refresh token)
3. User can only access password change endpoints
4. After successful password change, `requireResetPassword` is set to `false`
5. Next login issues full access token with refresh token

### **Login Response Examples:**

**Normal User (Full Access):**
```json
{
    "status": true,
    "statusCode": 200,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "abc123def456...",
        "type": "Bearer",
        "username": "superagent001",
        "email": "desideryg@gmail.com",
        "userType": "AGENT",
        "requireResetPassword": false,
        "partnerId": 1,
        "partnerUid": "01K6AF54DEP36C2DK7P9XA2E2T",
        "partnerCode": "MIXX",
        "partnerBusinessName": "Mixx by Yas",
        "displayName": "Godfrey Desidery - Super Agent",
        "roles": ["AGENT"],
        "tokenExpiresAt": "2025:10:01 15:30:00",
        "agentId": 1,
        "agentStatus": "ACTIVE",
        "lastLoginAt": "2025:10:01 11:30:00"
    }
}
```

**User Requiring Password Change (Limited Access):**
```json
{
    "status": true,
    "statusCode": 200,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": null,
        "type": "Bearer",
        "username": "user123",
        "email": "user@example.com",
        "userType": "AGENT",
        "requireResetPassword": true,
        "partnerId": 1,
        "partnerUid": "01K6AF54DEP36C2DK7P9XA2E2T",
        "partnerCode": "MIXX",
        "partnerBusinessName": "Mixx by Yas",
        "displayName": "John Doe - Agent",
        "roles": ["AGENT"],
        "tokenExpiresAt": "2025:10:01 15:30:00",
        "agentId": 2,
        "agentStatus": "ACTIVE",
        "lastLoginAt": "2025:10:01 11:30:00"
    }
}
```

## Configuration

**Optional:** Add these properties to your `application.properties` (defaults provided):

```properties
# Password reset token expiration in hours (defaults to 24)
app.password-reset.token-expiration-hours=24

# Frontend URL for reset links (defaults to http://localhost:3000)
app.frontend.url=http://localhost:3000
```

**Environment Variables (Recommended for Production):**
```bash
# Development
export APP_FRONTEND_URL=http://localhost:3000

# Production
export APP_FRONTEND_URL=https://yourdomain.com

# Staging
export APP_FRONTEND_URL=https://staging.yourdomain.com
```

**Docker Configuration:**
```yaml
environment:
  - APP_FRONTEND_URL=https://yourdomain.com
```

**Kubernetes ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  app.frontend.url: "https://yourdomain.com"
```

## Email Template

The password reset email includes:
- User's display name
- Reset link with token
- Token expiration time
- Security notice

Example email:
```
Hello John Doe,

You have requested to reset your password for your OBUS Partners account.

To reset your password, please click the link below:
http://localhost:3000/reset-password?token=abc123...

This link will expire in 24 hours for security reasons.

If you did not request this password reset, please ignore this email and your password will remain unchanged.

Best regards,
OBUS Partners Team
```

## Complete cURL Examples

### 1. Change Password (Authenticated)
```bash
# First, get an access token by logging in
ACCESS_TOKEN=$(curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }' | jq -r '.data.accessToken')

# Then change password
curl -X POST http://localhost:8080/v1/auth/password/change \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "currentPassword": "currentPassword123",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
  }'
```

### 2. Request Password Reset
```bash
curl -X POST http://localhost:8080/v1/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

### 3. Confirm Password Reset
```bash
# Use the token from the email
curl -X POST http://localhost:8080/v1/auth/password/confirm-reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "token_from_email",
    "newPassword": "newSecurePassword456",
    "confirmPassword": "newSecurePassword456"
  }'
```

### Complete Password Reset Flow
```bash
# Step 1: Request reset
curl -X POST http://localhost:8080/v1/auth/password/reset \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

# Step 2: Check email for reset link and extract token
# Step 3: Confirm reset with token
curl -X POST http://localhost:8080/v1/auth/password/confirm-reset \
  -H "Content-Type: application/json" \
  -d '{
    "token": "actual_token_from_email",
    "newPassword": "MyNewPassword123!",
    "confirmPassword": "MyNewPassword123!"
  }'

# Step 4: Login with new password
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "MyNewPassword123!"
  }'
```
