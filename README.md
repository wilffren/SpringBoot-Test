# CoopCredit System

A credit application management system for cooperative financial institutions built with Spring Boot and Hexagonal Architecture.

## Overview

CoopCredit System is a microservices-based application that manages credit applications for cooperative members. It provides functionality for member registration, credit application submission, and automated risk evaluation.

### Microservices

| Service | Port | Description |
|---------|------|-------------|
| credit-application-service | 8080 | Main service for members and credit applications |
| risk-central-mock-service | 8081 | Mock external risk evaluation service |

### Infrastructure

| Component | Port | Purpose |
|-----------|------|---------|
| MySQL | 3307 | Primary database |
| Prometheus | 9091 | Metrics collection |
| Grafana | 3000 | Metrics visualization |

## Architecture

The system follows Hexagonal Architecture (Ports and Adapters) principles:

```
src/main/java/com/coopcredit/creditapplication/
├── domain/
│   ├── model/           # Business entities
│   ├── ports/
│   │   ├── in/          # Use case interfaces
│   │   └── out/         # Repository interfaces
│   └── services/        # Domain services
├── application/
│   └── usecases/        # Use case implementations
└── infrastructure/
    ├── adapters/        # External service adapters
    ├── config/          # Spring configuration
    ├── controllers/     # REST controllers
    ├── entities/        # JPA entities
    ├── mappers/         # MapStruct mappers
    ├── repositories/    # JPA repositories
    └── security/        # JWT authentication
```

## Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose

## Quick Start

### Start the System

```bash
cd coopcredit-system
./start.sh
```

This command will:
1. Start Docker containers (MySQL, Prometheus, Grafana)
2. Start the risk-central-mock-service
3. Start the credit-application-service
4. Display available endpoints and a test JWT token

### Stop the System

```bash
cd coopcredit-system
./stop.sh
```

## API Documentation

### Swagger UI

- Credit Application Service: http://localhost:8080/swagger-ui.html
- Risk Central Mock: http://localhost:8081/swagger-ui.html

### Authentication

The API uses JWT (JSON Web Token) for authentication.

**Public Endpoints (no authentication required):**
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and receive JWT token

**Protected Endpoints (require JWT in Authorization header):**
- `/api/members/**` - Member management
- `/api/credit-applications/**` - Credit application operations

### Example Usage

1. Register a user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"analyst1","password":"password123","role":"ROLE_ANALYST"}'
```

2. Login to get JWT token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"analyst1","password":"password123"}'
```

3. Use the token for protected endpoints:
```bash
curl -X GET http://localhost:8080/api/members \
  -H "Authorization: Bearer <your-jwt-token>"
```

## User Roles

| Role | Permissions |
|------|-------------|
| ROLE_MEMBER | View own profile and applications |
| ROLE_ANALYST | Manage members and evaluate applications |
| ROLE_ADMIN | Full system access |

## Credit Evaluation Rules

The system evaluates credit applications based on:

1. **Seniority**: Member must have at least 6 months of affiliation
2. **Maximum Amount**: Requested amount cannot exceed 4x monthly salary
3. **Payment-to-Income Ratio**: Monthly payment must not exceed 30% of salary
4. **Risk Score**: External risk evaluation from risk-central service

## Monitoring

- **Actuator**: http://localhost:8080/actuator
- **Prometheus**: http://localhost:9091
- **Grafana**: http://localhost:3000 (admin/admin)

## Technology Stack

- **Framework**: Spring Boot 3.3.0
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Mapping**: MapStruct
- **Resilience**: Resilience4j
- **Monitoring**: Spring Actuator, Prometheus, Grafana

## Project Structure

```
coopcredit-system/
├── credit-application-service/    # Main microservice
├── risk-central-mock-service/     # Mock risk service
├── docker-compose.yml             # Infrastructure setup
├── prometheus.yml                 # Prometheus configuration
├── start.sh                       # Startup script
└── stop.sh                        # Shutdown script
```

## License

This project is proprietary software for Wilffren Muñoz