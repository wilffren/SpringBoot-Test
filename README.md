# CoopCredit System

A credit application management system for cooperative financial institutions built with Spring Boot 3.3 and Hexagonal Architecture.

## Table of Contents

1. [System Description](#system-description)
2. [Architecture](#architecture)
3. [API Endpoints](#api-endpoints)
4. [Local Execution](#local-execution)
5. [Docker Compose Deployment](#docker-compose-deployment)
6. [User Roles and Flow](#user-roles-and-flow)
7. [Observability](#observability)
8. [Test Evidence](#test-evidence)

---

## System Description

CoopCredit System is a microservices-based application designed for cooperative financial institutions to manage credit applications. The system provides:

- Member registration and management
- Credit application submission and tracking
- Automated risk evaluation with external service integration
- Role-based access control with JWT authentication
- Real-time metrics and monitoring

### Services Overview

| Service | Port | Description |
|---------|------|-------------|
| credit-application-service | 8080 | Core service for members, credit applications, and authentication |
| risk-central-mock-service | 8081 | Simulates external risk evaluation service |
| MySQL | 3307 | Relational database for persistence |
| Prometheus | 9091 | Metrics collection and storage |
| Grafana | 3000 | Metrics visualization dashboard |

---

## Architecture

### Hexagonal Architecture Diagram

```text
+------------------------------------------------------------------+
|                        INFRASTRUCTURE                             |
|  +------------------------------------------------------------+  |
|  |                      REST Controllers                       |  |
|  |  AuthController | MemberController | CreditAppController   |  |
|  +-----------------------------+------------------------------+  |
|                                |                                  |
|  +-----------------------------v------------------------------+  |
|  |                      INPUT PORTS                            |  |
|  |  RegisterUserUseCase | CreateMemberUseCase | ...            |  |
|  +-----------------------------+------------------------------+  |
|                                |                                  |
|  +=============================v==============================+  |
|  ||                        DOMAIN                            ||  |
|  ||  +-----------------------------------------------------+ ||  |
|  ||  |                    MODELS                           | ||  |
|  ||  |  User | Member | CreditApplication | RiskEvaluation | ||  |
|  ||  +-----------------------------------------------------+ ||  |
|  ||  +-----------------------------------------------------+ ||  |
|  ||  |                 DOMAIN SERVICES                     | ||  |
|  ||  |  CreditEvaluationService | BusinessRulesValidator   | ||  |
|  ||  +-----------------------------------------------------+ ||  |
|  +============================================================+  |
|                                |                                  |
|  +-----------------------------v------------------------------+  |
|  |                      OUTPUT PORTS                           |  |
|  |  UserRepositoryPort | MemberRepositoryPort | RiskCentralPort|  |
|  +-----------------------------+------------------------------+  |
|                                |                                  |
|  +-----------------------------v------------------------------+  |
|  |                       ADAPTERS                              |  |
|  |  JPA Repositories | RiskCentralAdapter | MapStruct Mappers  |  |
|  +------------------------------------------------------------+  |
|                                |                                  |
|  +-----------------------------v------------------------------+  |
|  |                    EXTERNAL SYSTEMS                         |  |
|  |        MySQL Database    |    Risk Central Service          |  |
|  +------------------------------------------------------------+  |
+------------------------------------------------------------------+
```

### Package Structure

```text
credit-application-service/
└── src/main/java/com/coopcredit/creditapplication/
    ├── domain/
    │   ├── model/                    # Business entities
    │   ├── model/enums/              # Domain enumerations
    │   ├── ports/in/                 # Input port interfaces (use cases)
    │   ├── ports/out/                # Output port interfaces (repositories)
    │   └── services/                 # Domain services
    ├── application/
    │   └── usecases/                 # Use case implementations
    │       ├── auth/                 # Authentication use cases
    │       ├── member/               # Member management use cases
    │       ├── credit/               # Credit application use cases
    │       └── evaluation/           # Risk evaluation use cases
    └── infrastructure/
        ├── adapters/                 # External service adapters
        ├── config/                   # Spring configuration classes
        ├── controllers/              # REST API controllers
        ├── entities/                 # JPA entity classes
        ├── exceptions/               # Exception handlers
        ├── mappers/                  # MapStruct mappers
        ├── repositories/             # JPA repository implementations
        └── security/                 # JWT authentication components
```

---

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | /api/auth/register | Register new user | username, password, role |
| POST | /api/auth/login | Authenticate user | username, password |

### Members (Protected)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | /api/members | Create member | ANALYST, ADMIN |
| GET | /api/members/{id} | Get member by ID | ANALYST, ADMIN |
| GET | /api/members/document/{doc} | Get member by document | ANALYST, ADMIN |
| PUT | /api/members/{id} | Update member | ANALYST, ADMIN |

### Credit Applications (Protected)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | /api/credit-applications | Create application | MEMBER, ANALYST, ADMIN |
| GET | /api/credit-applications | List applications | ANALYST, ADMIN |
| GET | /api/credit-applications/{id} | Get application by ID | ANALYST, ADMIN |
| POST | /api/credit-applications/{id}/evaluate | Evaluate application | ANALYST, ADMIN |

### Risk Central Mock

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/risk/evaluate | Evaluate credit risk |
| GET | /api/risk/health | Health check |

### Interactive Documentation

- Swagger UI (Credit Service): http://localhost:8080/swagger-ui.html
- Swagger UI (Risk Service): http://localhost:8081/swagger-ui.html

---

## Local Execution

### Prerequisites

- Java 21 (JDK)
- Maven 3.8+
- Docker and Docker Compose

### Option 1: Using Start Script (Recommended)

```bash
cd coopcredit-system

# Start all services
./start.sh

# Stop all services
./stop.sh
```

The start script will:

1. Release ports 8080 and 8081
2. Start Docker containers (MySQL, Prometheus, Grafana)
3. Wait for MySQL initialization
4. Start risk-central-mock-service in background
5. Start credit-application-service
6. Display endpoints and test JWT token

### Option 2: Manual Execution

Terminal 1 - Start infrastructure:

```bash
cd coopcredit-system
docker-compose up -d
```

Terminal 2 - Start risk service:

```bash
cd coopcredit-system/risk-central-mock-service
mvn spring-boot:run
```

Terminal 3 - Start credit service:

```bash
cd coopcredit-system/credit-application-service
mvn spring-boot:run
```

---

## Docker Compose Deployment

### Configuration

The docker-compose.yml file defines the following services:

| Service | Image | Port Mapping |
|---------|-------|--------------|
| mysql | mysql:8.0 | 3307:3306 |
| prometheus | prom/prometheus:latest | 9091:9090 |
| grafana | grafana/grafana:latest | 3000:3000 |

### Commands

```bash
# Start infrastructure only
docker-compose up -d

# View logs
docker-compose logs -f

# Stop infrastructure
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## User Roles and Flow

### Role Permissions

| Role | Description | Permissions |
|------|-------------|-------------|
| ROLE_MEMBER | Cooperative member | View own profile, submit credit applications |
| ROLE_ANALYST | Credit analyst | Manage members, evaluate applications |
| ROLE_ADMIN | System administrator | Full access to all operations |

### Credit Application Flow

```text
1. REGISTRATION
   User registers via POST /api/auth/register

2. AUTHENTICATION
   User logs in via POST /api/auth/login
   System returns JWT token

3. MEMBER CREATION
   Analyst creates member profile via POST /api/members
   Required: document, name, salary, affiliation date

4. APPLICATION SUBMISSION
   Member/Analyst creates credit application via POST /api/credit-applications
   Required: memberId, requestedAmount, termMonths, proposedRate

5. EVALUATION
   Analyst triggers evaluation via POST /api/credit-applications/{id}/evaluate
   System performs:
   - Seniority check (minimum 6 months)
   - Maximum amount check (4x salary)
   - Payment-to-income ratio check (max 30%)
   - External risk score query

6. DECISION
   Application status updated to APPROVED or REJECTED
   Evaluation details stored with reason
```

### Business Rules

| Rule | Condition | Result if Failed |
|------|-----------|------------------|
| Seniority | Member affiliation >= 6 months | Rejection |
| Maximum Amount | Requested <= 4x monthly salary | Rejection |
| Payment Ratio | Monthly payment <= 30% of salary | Rejection |
| Risk Score | Score from risk-central service | Affects decision |

---

## Observability

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| GET /actuator/health | Application health status |
| GET /actuator/info | Application information |
| GET /actuator/metrics | Available metrics |
| GET /actuator/prometheus | Prometheus-formatted metrics |

### Prometheus Metrics

Access: http://localhost:9091

Monitored metrics:

- HTTP request count and latency
- JVM memory usage
- Database connection pool
- Custom business metrics

### Grafana Dashboard

Access: http://localhost:3000

Credentials: admin / admin

### Logging

The application uses structured logging with SLF4J and Logback.

Log levels: ERROR, WARN, INFO, DEBUG

---

## Test Evidence

### Swagger UI Screenshots

Credit Application Service (http://localhost:8080/swagger-ui.html):

- Authentication endpoints visible
- Member CRUD operations available
- Credit application operations with evaluate endpoint
- JWT Authorization configured via Authorize button

Risk Central Mock (http://localhost:8081/swagger-ui.html):

- Risk evaluation endpoint available
- Health check endpoint available

### Sample API Requests

Register User:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ROLE_ADMIN"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Create Member:

```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"document":"12345678","name":"John Doe","salary":5000,"affiliationDate":"2024-01-01"}'
```

Create Credit Application:

```bash
curl -X POST http://localhost:8080/api/credit-applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"memberId":1,"requestedAmount":10000,"termMonths":12,"proposedRate":1.5}'
```

Evaluate Application:

```bash
curl -X POST http://localhost:8080/api/credit-applications/1/evaluate \
  -H "Authorization: Bearer TOKEN"
```

### Metrics Evidence

Actuator Health Check:

```bash
curl http://localhost:8080/actuator/health
```

Response: status UP with database and diskSpace components

Prometheus Metrics:

```bash
curl http://localhost:8080/actuator/prometheus
```

Response: JVM metrics, HTTP request metrics, Hikari pool metrics

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot 3.3.0 |
| Language | Java 21 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Migration | Flyway |
| Security | Spring Security / JWT |
| Documentation | SpringDoc OpenAPI 2.6.0 |
| Mapping | MapStruct 1.5.5 |
| Resilience | Resilience4j |
| Monitoring | Spring Actuator / Prometheus / Grafana |

---

## Project Structure

```text
coopcredit-system/
├── credit-application-service/     # Main microservice
│   ├── src/main/java/              # Java source code
│   ├── src/main/resources/         # Configuration files
│   └── pom.xml                     # Maven dependencies
├── risk-central-mock-service/      # Mock external service
│   ├── src/main/java/              # Java source code
│   └── pom.xml                     # Maven dependencies
├── docker-compose.yml              # Infrastructure definition
├── prometheus.yml                  # Prometheus configuration
├── start.sh                        # System startup script
└── stop.sh                         # System shutdown script
```

---

## License

Proprietary software for Wilffren Muñoz