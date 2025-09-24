# OBUS Partner API

A comprehensive Spring Boot REST API for OBUS Partner Management System with JWT-based authentication and modular architecture.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## 🚀 Overview

OBUS Partner API is a modern, scalable REST API built with Spring Boot that provides comprehensive partner management functionality. The application features JWT-based authentication, role-based access control, and a modular architecture designed for enterprise use.

## ✨ Features

- **🔐 JWT Authentication** - Secure token-based authentication
- **👥 Role-Based Access Control** - User, Admin, and Partner roles
- **🏗️ Modular Architecture** - Clean separation of concerns
- **📚 API Documentation** - Swagger/OpenAPI integration
- **🗄️ Database Integration** - MySQL with JPA/Hibernate
- **✅ Input Validation** - Comprehensive request validation
- **🛡️ Security** - Spring Security with CORS support
- **📊 Health Monitoring** - Actuator endpoints
- **🔧 Configuration Management** - Environment-based configuration

## 🏗️ Architecture

The application follows a modular, domain-driven design pattern:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Layer     │    │  Business Logic │    │  Data Layer     │
│                 │    │                 │    │                 │
│ • Controllers   │◄──►│ • Services      │◄──►│ • Repositories  │
│ • DTOs          │    │ • Domain Logic  │    │ • Entities      │
│ • Validation    │    │ • Utilities     │    │ • Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

Before running the application, ensure you have:

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+** (or compatible database)
- **Git** (for cloning the repository)

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd obus-partner-api
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Set up the database**
   ```sql
   CREATE DATABASE obus_partner_db;
   ```

4. **Configure the application** (see [Configuration](#configuration) section)

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## ⚙️ Configuration

### Application Properties

The application uses `application.yml` for configuration. Key settings include:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/obus_partner_db
    username: your_username
    password: your_password

# JWT Configuration
jwt:
  secret: your-secret-key-here
  expiration: 86400  # 24 hours

# Server Configuration
server:
  port: 8080
```

### Environment Variables

For production, use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/obus_partner_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-production-secret-key
```

## 📚 API Documentation

Once the application is running, access the API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Authentication

### Registration

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### Using JWT Token

```bash
curl -X GET http://localhost:8080/api/v1/partners \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📁 Project Structure

```
src/main/java/com/obus/partner/
├── api/                          # API Layer
│   ├── auth/                     # Authentication endpoints
│   └── partner/                  # Partner business endpoints
├── config/                       # Application configuration
├── modules/                      # Modular business logic
│   ├── auth_management/          # Authentication module
│   │   ├── domain/
│   │   │   ├── dto/              # Auth DTOs
│   │   │   ├── entity/           # Auth entities
│   │   │   └── enums/            # Auth enums
│   │   └── util/                 # Auth utilities (JWT)
│   ├── common/                   # Shared components
│   │   ├── exception/            # Common exceptions
│   │   └── security/             # Security configuration
│   └── user_and_role_management/ # User management module
│       ├── domain/
│       │   ├── entity/           # User entities
│       │   └── enums/            # Role enums
│       ├── repository/           # Data access
│       └── service/              # Business logic
└── ObusPartnerApiApplication.java
```

### Key Components

- **API Layer**: REST controllers and DTOs
- **Modules**: Domain-specific business logic
- **Common**: Shared utilities and configurations
- **Security**: JWT authentication and authorization

## 🛠️ Development

### Adding New Modules

1. Create module directory under `modules/`
2. Add domain entities, DTOs, and enums
3. Implement repository and service layers
4. Create API controllers
5. Add security configurations if needed

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments
- Implement proper error handling

### Database Migrations

The application uses Hibernate's `ddl-auto: update` for development. For production:

1. Use Flyway or Liquibase for migrations
2. Set `ddl-auto: validate`
3. Create proper migration scripts

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=UserServiceTest
```

### Test Structure

```
src/test/java/
├── integration/                  # Integration tests
├── unit/                        # Unit tests
└── testcontainers/              # Container-based tests
```

## 🚀 Deployment

### Docker Deployment

1. **Create Dockerfile**
   ```dockerfile
   FROM openjdk:17-jre-slim
   COPY target/obus-partner-api-1.0.0.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

2. **Build and run**
   ```bash
   docker build -t obus-partner-api .
   docker run -p 8080:8080 obus-partner-api
   ```

### Production Considerations

- Use external database
- Set strong JWT secret
- Enable HTTPS
- Configure proper logging
- Set up monitoring
- Use environment-specific configurations

## 📊 Monitoring

### Health Checks

- **Health**: http://localhost:8080/actuator/health
- **Info**: http://localhost:8080/actuator/info
- **Metrics**: http://localhost:8080/actuator/metrics

### Logging

Logs are configured to output at DEBUG level for development. Adjust in `application.yml`:

```yaml
logging:
  level:
    com.obus.partner: INFO  # Change to INFO for production
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Write unit tests for new features
- Follow the existing code structure
- Update documentation as needed
- Ensure all tests pass before submitting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:

- **Email**: support@obus.com
- **Documentation**: [API Docs](http://localhost:8080/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/obus/obus-partner-api/issues)

## 🔄 Version History

- **v1.0.0** - Initial release with authentication and user management
- **v1.1.0** - Added partner management features (planned)
- **v1.2.0** - Enhanced security and monitoring (planned)

---

**Made with ❤️ by the OBUS Team**
