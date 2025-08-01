# JWT Service Documentation

## Overview

This document provides an overview of the JWT (JSON Web Token) authentication service implemented in the UserService microservice. The JWT service provides secure authentication and authorization for the application.

## Components

### 1. JWT Dependencies

The following JWT dependencies were added to the project:
- `io.jsonwebtoken:jjwt-api:0.11.5` - The core JWT API
- `io.jsonwebtoken:jjwt-impl:0.11.5` - The implementation of the JWT API
- `io.jsonwebtoken:jjwt-jackson:0.11.5` - Jackson integration for JWT

### 2. JWT Service

The JWT service is responsible for generating, validating, and parsing JWT tokens. It consists of:

- **JwtService Interface**: Defines the contract for JWT operations
  - `extractUsername(String token)`: Extracts the username from a token
  - `generateToken(UserDetails userDetails)`: Generates a token for a user
  - `generateToken(Map<String, Object> extraClaims, UserDetails userDetails)`: Generates a token with extra claims
  - `isTokenValid(String token, UserDetails userDetails)`: Validates a token
  - `generateRefreshToken(UserDetails userDetails)`: Generates a refresh token

- **JwtServiceImpl Class**: Implements the JwtService interface
  - Uses the JJWT library to handle JWT operations
  - Configures JWT with secret key, expiration time, and refresh token expiration time from application.yml

### 3. Authentication Filter

The **JwtAuthenticationFilter** extends `OncePerRequestFilter` to validate JWT tokens in incoming requests:
- Extracts the JWT token from the Authorization header
- Validates the token using the JwtService
- Loads the user details using the UserDetailsService
- Sets the authentication in the SecurityContextHolder if the token is valid

### 4. Security Configuration

The **SecurityConfig** class configures Spring Security to use JWT authentication:
- Disables CSRF protection (common for REST APIs)
- Configures stateless session management
- Permits access to authentication endpoints without authentication
- Adds the JwtAuthenticationFilter to the security filter chain

### 5. Authentication Service

The **AuthenticationService** handles authentication logic:
- **Register**: Creates a new user and generates JWT tokens
- **Authenticate**: Validates credentials and generates JWT tokens
- **Refresh Token**: Refreshes JWT tokens using a valid refresh token

### 6. Authentication Controller

The **AuthController** exposes REST endpoints for authentication:
- `POST /api/v1/auth/register`: Registers a new user
- `POST /api/v1/auth/login`: Authenticates a user
- `POST /api/v1/auth/refresh-token`: Refreshes JWT tokens

## Configuration

JWT configuration is defined in `application.yml`:
```yaml
jwt:
  secretKey: ${JWT_SECRET_KEY:defaultSecretKeyForDevelopment12345678901234567890}
  expiration: 86400000  # 24 hours in milliseconds
  header: Authorization
  prefix: Bearer
  refresh-token:
    expiration: 604800000  # 7 days in milliseconds
```

## Authentication Flow

1. **Registration**:
   - Client sends registration request with user details
   - Server creates a new user and generates JWT tokens
   - Server returns JWT tokens to the client

2. **Login**:
   - Client sends login request with credentials
   - Server validates credentials and generates JWT tokens
   - Server returns JWT tokens to the client

3. **Authenticated Requests**:
   - Client includes JWT token in Authorization header
   - Server validates token and processes the request
   - If token is invalid, server returns 401 Unauthorized

4. **Token Refresh**:
   - Client sends refresh token in Authorization header
   - Server validates refresh token and generates new JWT tokens
   - Server returns new JWT tokens to the client

## Security Considerations

- JWT tokens are signed with a secret key to prevent tampering
- Access tokens have a short expiration time (24 hours)
- Refresh tokens have a longer expiration time (7 days)
- Passwords are hashed using BCrypt before storage
- Authentication failures are handled with appropriate error responses