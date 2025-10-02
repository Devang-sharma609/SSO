# SSO Authentication Service

A comprehensive Single Sign-On (SSO) Authentication Service built with Spring Boot that provides organization-based user management and cross-client app authentication.

## Features

### Core Architecture
- **Organization Owners**: Users without API key headers who can create and manage organizations
- **Client App Users**: Users who authenticate through specific client applications using client-app API keys
- **API Key-based Authentication**: Distinguishes between org owners and client app users based on API key presence and type
- **JWT Tokens**: Access and refresh token mechanism for secure authentication
- **Cross-Client App Access**: Users authenticated in one client app can access all other client apps within the same organization
- **Custom Organization Naming**: Users can specify organization name and description during signup
- **User Metadata Support**: Client app users can include custom metadata that gets stored and returned in JWT claims

### Key Components

1. **Entity Models**
   - `Organization`: Represents a company/organization with auto-generated org owner API key
   - `OrgOwner`: Organization administrators who manage the org and client apps
   - `ClientApp`: Applications within an organization with their own API keys
   - `User`: End users belonging to specific client apps and organizations with optional custom metadata
   - `RefreshToken`: Secure token storage for persistent authentication

2. **Authentication Flow**
   - **No API Key Header**: Treated as potential org owner (signup/login)
   - **Org Owner API Key** (`org_*`): Full organization management access
   - **Client App API Key** (`app_*`): User authentication for specific client app

3. **API Endpoints**

#### Authentication Endpoints (`/api/auth/`)
- `POST /signup` - User registration (org owner or client app user based on API key)
- `POST /login` - User authentication (org owner or client app user based on API key)
- `POST /refresh` - Refresh access token using refresh token
- `POST /logout` - Revoke refresh token
- `GET /validate` - Validate API key and return user context

#### Organization Management (`/api/organization/`) - Requires Org Owner API Key
- `POST /` - Create new organization
- `GET /` - List all organizations
- `GET /{id}` - Get organization by ID
- `GET /me` - Get current user's organization
- `PUT /{id}` - Update organization
- `DELETE /{id}` - Delete organization

#### Client App Management
- `POST /{orgId}/client-apps` - Create client app
- `GET /{orgId}/client-apps` - List client apps for organization
- `GET /client-apps/{id}` - Get client app details
- `PUT /client-apps/{id}` - Update client app
- `DELETE /client-apps/{id}` - Delete client app

## Configuration

### Environment-Based Configuration
The application supports multiple environments (dev, prod) with secure credential management:

#### Environment Files
- **`.env`** - Production environment variables (not committed to git)
- **`.env.example`** - Template for environment variables
- **`application-dev.properties`** - Development-specific settings
- **`application-prod.properties`** - Production-specific settings

#### Database Configuration
```properties
# Environment Variables (from .env file)
DATABASE_URL=jdbc:postgresql://your-host:5432/your-db
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password
```

#### JWT Configuration
```properties
# Environment Variables (from .env file)
JWT_SECRET=your-secure-jwt-secret-key-here
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

### Profile-Specific Settings

#### Development Profile (`dev`)
- Detailed SQL logging
- Debug-level application logs
- Database auto-update enabled
- CORS enabled for local development

#### Production Profile (`prod`)
- Minimal logging for performance
- Database validation mode (no auto-updates)
- Connection pooling optimized
- Security-focused settings

## Usage Examples

### 1. Organization Owner Registration (No API Key)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "organizationName": "Tech Solutions Inc",
    "organizationDescription": "A technology solutions company"
  }'
```

Response includes:
- Access token
- Refresh token
- User claims (non-sensitive)
- Organization API key (`orgOwnerApiKey`) for future requests

### 2. Create Client App (Using Org Owner API Key)
```bash
curl -X POST http://localhost:8080/api/organization/{orgId}/client-apps \
  -H "Content-Type: application/json" \
  -H "apikey: org_abc123..." \
  -d '{
    "name": "Mobile App",
    "description": "Customer mobile application"
  }'
```

Response includes client app API key (`app_xyz789...`)

### 3. Client App User Registration (Using Client App API Key)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -H "apikey: app_xyz789..." \
  -d '{
    "username": "user123",
    "password": "userpass",
    "email": "user@example.com",
    "user_metadata": {
      "role": "DRIVER",
      "empID": "E26241",
      "department": "Operations"
    }'
```

Response includes:
- Access token
- Refresh token
- User claims including custom `user_metadata`
- Client app API key (`clientAppApiKey`)

### 4. Cross-Client App Authentication
When a user logs into one client app, they receive an access token that grants access to all other client apps within the same organization. Other client apps can validate the user by checking the access token claims.

### Example Workflow for Cross App Access### Step 1: User logs into Client App A
```bash
POST /api/auth/login
Headers:
  apikey: app_client_A_apikey

Body:
{
  "username": "john.doe",
  "password": "password123"
}

Response:
{
  "accessToken": "token_for_app_A",
  "refreshToken": "refresh_token_A",
  ...
  ...
  ...
}
```

### Step 2: User wants to access Client App B (SSO)
```bash
POST /api/auth/sso-exchange

Body:
{
  "currentAccessToken": "token_for_app_A",
  "targetClientAppApiKey": "app_clientB_apikey"
}

Response:
{
  "accessToken": "token_for_app_B",
  "refreshToken": "refresh_token_B",
  ...
  ...
  ...
}
```

### Step 3: User accesses Client App B with new token
The user can now use `token_for_app_B` to access Client App B's protected resources.


### 5. User Metadata Feature
Client app users can include custom metadata during signup that gets:
- **Stored**: Persisted in the database as JSON
- **Retrieved**: Included in JWT access token claims
- **Validated**: Available for authorization decisions
- **Maintained**: Preserved across login/refresh operations

Example use cases:
- Employee roles and permissions
- User preferences and settings
- Department or team information
- Custom business logic data

## Security Features

- **Password Encryption**: BCrypt hashing for all passwords
- **JWT Security**: Signed tokens with configurable expiration
- **API Key Validation**: Custom authentication filter for API key verification
- **Role-Based Access**: Org owners and client app users have different permissions
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Session Management**: Stateless authentication using JWT tokens

## Token Structure

### Access Token Claims
```json
{
  "userId": "uuid",
  "username": "user123",
  "userType": "ORG_OWNER" | "CLIENT_USER",
  "organizationId": "uuid",
  "organizationName": "Acme Corp",
  "clientAppId": "uuid", // Only for CLIENT_USER
  "user_metadata": { // Only for CLIENT_USER with metadata
    "role": "DRIVER",
    "empID": "E26241",
    "department": "Operations"
  }
}
```

### Response Format
All API responses follow a consistent format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "error": null
}
```

### Authentication Response Structure
```json
{
  "success": true,
  "message": "Signup successful",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userClaims": {
      "userId": "uuid",
      "username": "user123",
      "userType": "CLIENT_USER",
      "organizationId": "uuid",
      "organizationName": "Tech Solutions Inc",
      "clientAppId": "uuid",
      "user_metadata": {
        "role": "DRIVER",
        "empID": "E26241"
      }
    },
    "orgOwnerApiKey": "org_abc123...", // Only for org owners
    "clientAppApiKey": "app_xyz789..." // Only for client app users
  },
  "error": null
}
```

## Getting Started

### Quick Start

#### 1. Environment Setup
```bash
# Copy environment template
cp .env.example .env

# Edit .env with your actual credentials
# Update DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET
```

#### 2. Database Setup
- Create PostgreSQL database (or use existing cloud database)
- Update `.env` file with your database credentials

#### 3. Run Application
```bash
./mvnw spring-boot:run
```
#### 4. Test Endpoints
Use the provided curl examples to test the API

### Environment Variables Reference

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | - | `jdbc:postgresql://host:5432/db` |
| `DATABASE_USERNAME` | Database username | - | `postgres` |
| `DATABASE_PASSWORD` | Database password | - | `password123` |
| `JWT_SECRET` | JWT signing secret | - | `your-secret-key` |
| `JWT_ACCESS_EXPIRATION` | Access token expiry (ms) | `3600000` | `3600000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiry (ms) | `604800000` | `604800000` |
| `SERVER_PORT` | Application port | `8080` | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` | `prod` |

## Architecture Benefits

- **Scalability**: Organization-based multi-tenancy
- **Security**: Role-based access control with API key authentication
- **Flexibility**: Same authentication endpoints serve different user types
- **SSO Capability**: Users authenticated in one client app can access others
- **Token Management**: Secure refresh token mechanism for persistent sessions

The system automatically handles user type detection based on API key presence and format, making it easy to integrate with various client applications while maintaining security and organization isolation.