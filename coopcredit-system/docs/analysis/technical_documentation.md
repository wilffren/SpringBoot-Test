# CoopCredit System - Documentación Técnica

## 📑 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Stack Tecnológico Detallado](#stack-tecnológico-detallado)
4. [Modelos de Dominio](#modelos-de-dominio)
5. [Casos de Uso (Use Cases)](#casos-de-uso)
6. [Seguridad y Autenticación](#seguridad-y-autenticación)
7. [Persistencia y Base de Datos](#persistencia-y-base-de-datos)
8. [Observabilidad y Métricas](#observabilidad-y-métricas)
9. [Resiliencia y Tolerancia a Fallos](#resiliencia-y-tolerancia-a-fallos)
10. [Testing](#testing)
11. [Deployment](#deployment)

---

## 1. Resumen Ejecutivo

**CoopCredit System** es un sistema integral de gestión de solicitudes de crédito construido con **arquitectura hexagonal** y orientado a **microservicios**. El sistema implementa las mejores prácticas de desarrollo de software moderno incluyendo **SOLID principles**, **clean architecture**, **testing multinivel**, y **observabilidad completa**.

### Características Principales

- ✅ **Arquitectura Hexagonal**: Domain-driven design con inversión de dependencias
- ✅ **Spring Boot 3.3.0**: Framework enterprise con Java 21
- ✅ **JWT Authentication**: Autenticación stateless
- ✅ **Microservicios**: Separación de concerns
- ✅ **Observabilidad**: Prometheus + Grafana
- ✅ **Resiliencia**: Circuit breaker + retry patterns
- ✅ **Testing**: Unit + Integration + E2E
- ✅ **Documentación Automática**: Swagger/OpenAPI

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura Hexagonal (Puertos y Adaptadores)

El sistema implement a arquitectura hexagonal que separa:

```
┌─────────────────────────────────────────────────────────────┐
│                   OUTER LAYER (Infrastructure)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ REST API     │  │  Persistence │  │   Security   │     │
│  │ Controllers  │  │  JPA Adapters│  │   JWT Filter │     │
│  └──────┬───────┘  └───────┬──────┘  └──────┬───────┘     │
│         │                  │                  │             │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │ implements       │ implements       │ uses
          ▼                  ▼                  │
┌─────────────────────────────────────────────────────────────┐
│                   MIDDLE LAYER (Application)                │
│               ┌──────────────────────┐                      │
│               │   Use Cases Impl     │←─────────────────────┘
│               │  (Business Logic)    │                      │
│               └──────────┬───────────┘                      │
│                          │ uses                             │
└──────────────────────────┼─────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   INNER LAYER (Domain)                      │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Models    │  │ Input Ports  │  │ Output Ports │      │
│  │   +Logic    │  │ (Use Cases)  │  │(Repositories)│      │
│  └─────────────┘  └──────────────┘  └──────────────┘      │
│                    Pure Business Logic                      │
└─────────────────────────────────────────────────────────────┘
```

**Flujo de Dependencias**: Las dependencias siempre apuntan hacia adentro (hacia el dominio).

### 2.2 Arquitectura de Microservicios

```
┌──────────────────────────────────────────────┐
│           Client (Browser/Mobile)             │
└──────────────────┬───────────────────────────┘
                   │ HTTP/JWT
                   ▼
┌──────────────────────────────────────────────┐
│     Credit Application Service :8080         │
│  ┌────────────────────────────────────────┐  │
│  │  REST API (Swagger UI)                 │  │
│  │  ┌──────┐  ┌──────┐  ┌──────────┐    │  │
│  │  │ Auth │  │Member│  │  Credit  │    │  │
│  │  │Controller│Ctrl │  │Appl.Ctrl │    │  │
│  │  └──────┘  └──────┘  └──────────┘    │  │
│  └────────────────┬───────────────────────┘  │
│                   │                           │
│  ┌────────────────┴───────────────────────┐  │
│  │        Application Layer                │  │
│  │    (Use Cases Implementations)          │  │
│  └────────────────┬───────────────────────┘  │
│                   │                           │
│  ┌────────────────┴───────────────────────┐  │
│  │         Domain Layer                    │  │
│  │  (Models, Ports, Business Rules)        │  │
│  └────────────────┬───────────────────────┘  │
│                   │                           │
│  ┌────────────────┴───────────────────────┐  │
│  │      Infrastructure Layer               │  │
│  │  ┌──────────┐         ┌──────────────┐ │  │
│  │  │JPA Repos │         │ HTTP Client  │ │  │
│  │  └────┬─────┘         └──────┬───────┘ │  │
└───────────┼───────────────────────┼────────┘  │
            │                       │            │
            ▼                       ▼            │
      ┌──────────┐          ┌──────────────┐    │
      │  MySQL   │          │ Risk Central │    │
      │  :3307   │          │  Service     │    │
      └──────────┘          │    :8081     │    │
                            └──────────────┘    │
                                                 │
      ┌──────────────────────────────────┐      │
      │  Observability Stack             │      │
      │  ┌──────────┐   ┌────────────┐   │      │
      │  │Prometheus│ → │  Grafana   │   │      │
      │  │  :9091   │   │   :3000    │   │      │
      │  └──────────┘   └────────────┘   │      │
      └──────────────────────────────────┘
```

### 2.3 Componentes del Sistema

#### Credit Application Service

**Responsabilidades**:
- Gestión de usuarios y autenticación (JWT)
- CRUD de afiliados (Members)
- Gestión de solicitudes de crédito
- Orquestación de evaluación de riesgo
- Exposición de API REST
- Métricas y health checks

**Puerto**: 8080

#### Risk Central Mock Service

**Responsabilidades**:
- Simulación de central de riesgo externa
- Generación determinística de scores crediticios
- API REST para evaluación de riesgo

**Puerto**: 8081

**Algoritmo de Score**:
```java
// Hash del documento módulo 300 + base 500 = score entre 500-800
int hash = Math.abs(documentNumber.hashCode());
int score = 500 + (hash % 300);
```

#### Infraestructura

- **MySQL 8.0**: Base de datos relacional
- **Prometheus**: Time-series database para métricas
- **Grafana**: Visualización de métricas y dashboards

---

## 3. Stack Tecnológico Detallado

### 3.1 Backend Framework

**Spring Boot 3.3.0**
- `spring-boot-starter-web`: REST API
- `spring-boot-starter-data-jpa`: ORM/Persistence
- `spring-boot-starter-validation`: Bean Validation
- `spring-boot-starter-security`: Authentication/Authorization
- `spring-boot-starter-actuator`: Health checks, metrics
- `spring-boot-starter-webflux`: WebClient para HTTP
- `spring-boot-starter-aop`: Aspect-oriented programming

**Java 21**
- Records para DTOs
- Pattern matching
- Virtual threads ready

**Maven 3.8+**
- Gestión de dependencias
- Build automation
- Plugins: compiler, surefire, failsafe

### 3.2 Persistencia

**Spring Data JPA**
- Abstracción de acceso a datos
- Repositories con métodos derivados
- Custom queries con@Query

**MySQL 8.0**
- Base de datos relacional
- Soporte transaccional (ACID)
- Índices y constraints

**Flyway**
- Versionamiento de esquemas
- Migraciones automatizadas
- Rollback controlado

**HikariCP**
- Connection pooling
- Alto rendimiento
- Auto-configurado por Spring Boot

### 3.3 Seguridad

**Spring Security 6**
- Filter chain configuration
- Role-based access control
- Method security (@PreAuthorize)

**JWT (JJWT 0.11.5)**
- Token generation con HS256
- Claims: username, role, userId
- Expiration: 24 horas configurable

**BCrypt**
- Hash de passwords
- Salt automático
- Strength configurable

### 3.4 Resiliencia

**Resilience4j 2.1.0**
- Circuit Breaker pattern
- Retry with exponential backoff
- Fallback methods
- Métricas integradas

### 3.5 Observabilidad

**Micrometer**
- Abstracción de métricas
- Múltiples backends
- Custom metrics

**Prometheus**
- Scraping de métricas
- Time-series storage
- PromQL queries

**Grafana**
- Dashboards visuales
- Alerting
- Multiple datasources

**Spring Boot Actuator**
- `/actuator/health`: Health checks
- `/actuator/metrics`: Métricas disponibles
- `/actuator/prometheus`: Endpoint Prometheus
- `/actuator/info`: Información de la app

### 3.6 Documentación

**SpringDoc OpenAPI 3 (2.6.0)**
- Generación automática de OpenAPI 3.0 spec
- Swagger UI integrado
- Anotaciones para documentar endpoints

### 3.7 Mapeo

**MapStruct 1.5.5**
- Mapeo compile-time
- Type-safe
- Performance óptimo
- Integración con Lombok

**Lombok**
- @Getter/@Setter
- @Builder
- @AllArgsConstructor/@NoArgsConstructor
- Reducción de boilerplate

### 3.8 Testing

**JUnit 5**
- Framework de testing
- @Test, @BeforeEach, @AfterEach
- Assertions

**Mockito**
- Mocking framework
- @Mock, @InjectMocks
- Verification

**Testcontainers 1.19.3**
- Testing con contenedores Docker
- MySQL container para integration tests
- Aislamiento de tests

**REST Assured**
- Testing de APIs REST
- Fluent API
- JSON/XML response validation

**Spring Security Test**
- @WithMockUser
- Security context testing

### 3.9 DevOps

**Docker**
- Containerización de servicios
- Reproducibilidad de ambientes

**Docker Compose**
- Orquestación multi-container
- Desarrollo local simplificado

**Shell Scripts**
- `start.sh`: Inicio automatizado
- `stop.sh`: Parada limpia

---

## 4. Modelos de Dominio

### 4.1 User

**Propósito**: Usuarios del sistema con autenticación.

```java
public class User {
    private Long id;
    private String username;
    private String password;  // BCrypt hashed
    private UserRole role;     // ADMIN, ANALYST, MEMBER
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Roles**:
- `ADMIN`: Administrador del sistema
- `ANALYST`: Analista de crédito
- `MEMBER`: Afiliado de la cooperativa

### 4.2 Member

**Propósito**: Afiliados de la cooperativa.

```java
public class Member {
    private Long id;
    private String document;           // Unique
    private String name;
    private BigDecimal salary;
    private LocalDate affiliationDate;
    private MemberStatus status;       // ACTIVE, INACTIVE
    private Long userId;               // FK to User
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business methods
    public long getSeniorityInMonths();
    public boolean isActive();
    public BigDecimal getMaxCreditAmount();  // 4x salary
}
```

**Reglas de Negocio**:
- Documento único
- Antigüedad calculada desde affiliationDate
- Monto máximo de crédito = 4 × salario
- Debe estar ACTIVE para solicitar crédito

### 4.3 CreditApplication

**Propósito**: Solicitudes de crédito de los afiliados.

```java
public class CreditApplication {
    private Long id;
    private Long memberId;             // FK to Member
    private BigDecimal requestedAmount;
    private Integer termMonths;
    private BigDecimal proposedRate;
    private LocalDate applicationDate;
    private ApplicationStatus status;  // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business methods
    public BigDecimal calculateMonthlyPayment();
    public boolean isPending();
    public void approve();
    public void reject();
}
```

**Estados**:
- `PENDING`: Recién creada, pendiente de evaluación
- `APPROVED`: Aprobada tras evaluación positiva
- `REJECTED`: Rechazada por incumplir criterios

### 4.4 RiskEvaluation

**Propósito**: Resultado de la evaluación de riesgo crediticio.

```java
public class RiskEvaluation {
    private Long id;
    private Long creditApplicationId;   // FK to CreditApplication
    private Integer score;               // From Risk Central
    private RiskLevel riskLevel;         // LOW, MEDIUM, HIGH
    private BigDecimal paymentToIncomeRatio;
    private Boolean meetsSeniority;
    private Boolean meetsMaxAmount;
    private FinalDecision finalDecision; // APPROVED, REJECTED
    private String reason;
    private String riskCentralDetail;
    private LocalDateTime evaluatedAt;
}
```

**Criterios de Evaluación**:
1. **Score crediticio**: Obtenido de Risk Central Service
2. **Ratio cuota/ingreso**: ≤ 40%
3. **Antigüedad**: ≥ 6 meses
4. **Monto máximo**: ≤ 4 × salario
5. **Nivel de riesgo**: NO debe ser HIGH

---

## 5. Casos de Uso

### 5.1 Autenticación

#### RegisterUserUseCase

**Input**: `RegisterUserCommand(username, password, role)`  
**Output**: `User`  
**Lógica**:
1. Validar que username no exista
2. Encriptar password con BCrypt
3. Crear usuario con rol especificado
4. Retornar usuario creado

#### AuthenticateUserUseCase

**Input**: `AuthenticateCommand(username, password)`  
**Output**: `String` (JWT token)  
**Lógica**:
1. Buscar usuario por username
2. Validar password con BCrypt
3. Generar JWT token con claims
4. Retornar token

### 5.2 Gestión de Afiliados

#### CreateMemberUseCase

**Input**: `CreateMemberCommand(document, name, salary, affiliationDate)`  
**Output**: `Member`  
**Lógica**:
1. Validar que documento no exista
2. Crear afiliado con status ACTIVE
3. Guardar en repositorio
4. Retornar afiliado creado

#### GetMemberUseCase

**Input**: `Long id` o `String document`  
**Output**: `Member`  
**Lógica**:
1. Buscar afiliado por ID o documento
2. Lanzar NotFoundException si no existe
3. Retornar afiliado

#### UpdateMemberUseCase

**Input**: `Long id`, `UpdateMemberCommand(name, salary, status)`  
**Output**: `Member`  
**Lógica**:
1. Buscar afiliado por ID
2. Actualizar campos permitidos
3. Guardar cambios
4. Retornar afiliado actualizado

### 5.3 Solicitudes de Crédito

#### CreateCreditApplicationUseCase

**Input**: `CreateCreditApplicationCommand(memberId, requestedAmount, termMonths, proposedRate)`  
**Output**: `CreditApplication`  
**Lógica**:
1. Validar que afiliado existe
2. Validar que afiliado está ACTIVE
3. Validar antigüedad ≥ 6 meses
4. Crear solicitud con status PENDING
5. Guardar y retornar solicitud

#### GetCreditApplicationUseCase

**Input**: `Long id`  
**Output**: `CreditApplication`  
**Lógica**:
1. Buscar solicitud por ID
2. Lanzar NotFoundException si no existe
3. Retornar solicitud

#### ListCreditApplicationsUseCase

**Input**: Opcional `Long memberId` o `ApplicationStatus status`  
**Output**: `List<CreditApplication>`  
**Lógica**:
1. Si hay filtro de memberId, buscar por afiliado
2. Si hay filtro de status, buscar por estado
3. Si no hay filtros, retornar todas
4. Retornar lista

### 5.4 Evaluación de Riesgo

#### EvaluateCreditApplicationUseCase

**Input**: `Long creditApplicationId`  
**Output**: `RiskEvaluation`  
**Lógica**:
1. Validar que no esté ya evaluada
2. Obtener solicitud y validar estado PENDING
3. Obtener afiliado asociado
4. Llamar Risk Central Service (con circuit breaker)
5. Calcular ratio cuota/ingreso
6. Verificar criterios de aprobación:
   - Antigüedad ≥ 6 meses
   - Monto ≤ 4 × salario
   - Ratio cuota/ingreso ≤ 40%
   - Riesgo ≠ HIGH
7. Determinar decisión final (APPROVED/REJECTED)
8. Guardar evaluación
9. Actualizar status de solicitud
10. Retornar evaluación

**Ejemplo de Evaluación**:

```java
// Afiliado: salario $5,000,000, antigüedad 8 meses
// Solicitud: monto $10,000,000, 36 meses, tasa 12%

// 1. Risk Central score: 720 → MEDIUM risk ✓
// 2. Cuota mensual: $10M × (1 + 0.12×36) / 36 = $1,477,777
// 3. Ratio cuota/ingreso: $1,477,777 / $5,000,000 = 0.296 (29.6%) ✓
// 4. Antigüedad: 8 meses ≥ 6 meses ✓
// 5. Monto máximo: $10M ≤ 4×$5M = $20M ✓

// Resultado: APPROVED
```

---

## 6. Seguridad y Autenticación

### 6.1 JWT Authentication Flow

```
1. USER → POST /api/auth/login {username, password}
          ↓
2. AuthController → AuthenticateUserUseCase
          ↓
3. ValidateBCrypt password
          ↓
4. JwtTokenProvider.generateToken(user)
          ↓
5. JWT Token ← {
   "sub": "admin",
   "role": "ADMIN",
   "userId": 1,
   "iat": 1702410000,
   "exp": 1702496400
}
          ↓
6. USER receives token
          ↓
7. USER → GET /api/members (Authorization: Bearer {token})
          ↓
8. JwtAuthenticationFilter intercepts request
          ↓
9. Extract & validate token
          ↓
10. Set Authentication in SecurityContext
          ↓
11. Controller executes (user authenticated)
```

### 6.2 Security Configuration

**Public Endpoints** (sin autenticación):
- `/api/auth/**` - Login y registro
- `/swagger-ui/**` - Documentación
- `/v3/api-docs/**` - OpenAPI spec
- `/actuator/health` - Health check

**Protected Endpoints** (requieren JWT):
- `/api/members/**` - CRUD afiliados
- `/api/credit-applications/**` - Solicitudes

**Role-Based**:
- `POST /api/credit-applications/{id}/evaluate` - Solo ANALYST y ADMIN

### 6.3 JWT Configuration

```yaml
jwt:
  secret: "coopcredit-secret-key-must-be-at-least-256-bits-long-for-hs256"
  expiration: 86400000  # 24 hours in milliseconds
```

**Claims del Token**:
- `sub`: username
- `role`: UserRole (ADMIN/ANALYST/MEMBER)
- `userId`: ID del usuario
- `iat`: Issued at
- `exp`: Expiration

**Algoritmo**: HS256 (HMAC with SHA-256)

---

## 7. Persistencia y Base de Datos

### 7.1 Esquema de Base de Datos

```sql
-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Members table
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    salary DECIMAL(15,2) NOT NULL,
    affiliation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Credit Applications table
CREATE TABLE credit_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    term_months INT NOT NULL,
    proposed_rate DECIMAL(5,4) NOT NULL,
    application_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id)
);

-- Risk Evaluations table
CREATE TABLE risk_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credit_application_id BIGINT UNIQUE NOT NULL,
    score INT,
    risk_level VARCHAR(20),
    payment_to_income_ratio DECIMAL(5,4),
    meets_seniority BOOLEAN,
    meets_max_amount BOOLEAN,
    final_decision VARCHAR(20),
    reason TEXT,
    risk_central_detail TEXT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (credit_application_id) REFERENCES credit_applications(id)
);

-- Indexes
CREATE INDEX idx_members_document ON members(document);
CREATE INDEX idx_members_status ON members(status);
CREATE INDEX idx_applications_member ON credit_applications(member_id);
CREATE INDEX idx_applications_status ON credit_applications(status);
CREATE INDEX idx_evaluations_application ON risk_evaluations(credit_application_id);
```

### 7.2 Migraciones Flyway

Ubicación: `src/main/resources/db/migration/`

- `V1__create_users_table.sql`
- `V2__create_members_table.sql`
- `V3__create_credit_applications_table.sql`
- `V4__create_risk_evaluations_table.sql`
- `V5__add_foreign_keys.sql`
- `V6__insert_initial_data.sql`

**Configuración**:
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### 7.3 JPA Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

---

## 8. Observabilidad y Métricas

### 8.1 Métricas Expuestas

**JVM Metrics**:
- `jvm_memory_used_bytes`: Memoria heap usada
- `jvm_memory_max_bytes`: Memoria heap máxima
- `jvm_gc_pause_seconds`: Tiempo en GC
- `jvm_threads_live`: Threads activos

**HTTP Metrics**:
- `http_server_requests_seconds`: Latencia de requests
- `http_server_requests_seconds_count`: Contador de requests
- `http_server_requests_seconds_sum`: Total tiempo en requests

**Custom Business Metrics**:
- `credit_applications_created_total`: Total solicitudes creadas
- `credit_applications_evaluated_total`: Total evaluaciones
- `risk_evaluations_approved`: Evaluaciones aprobadas
- `risk_evaluations_rejected`: Evaluaciones rechazadas

**Resilience4j Metrics**:
- `resilience4j_circuitbreaker_state`: Estado del circuit breaker
- `resilience4j_circuitbreaker_calls`: Llamadas por estado
- `resilience4j_retry_calls`: Intentos de retry

### 8.2 Configuración Prometheus

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'credit-application-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### 8.3 Dashboards Grafana

Pre-provisionados en `grafana_provisioning/dashboards/`:

1. **JVM Dashboard**: Heap, threads, GC
2. **HTTP Traffic**: Requests/sec, latency percentiles, error rate
3. **Business Metrics**: Applications created/day, approval rate
4. **Resilience**: Circuit breaker states, retry counts

---

## 9. Resiliencia y Tolerancia a Fallos

### 9.1 Circuit Breaker

**Configuración**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      riskCentral:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
```

**Estados**:
1. **CLOSED**: Normal, llamadas pasan
2. **OPEN**: Servicio caído, llamadas fallan rápido
3. **HALF_OPEN**: Probando recuperación

### 9.2 Retry

**Configuración**:
```yaml
resilience4j:
  retry:
    instances:
      riskCentral:
        maxAttempts: 3
        waitDuration: 1s
```

### 9.3 Fallback

Cuando Risk Central no responde:

```java
private RiskCentralResponse fallbackEvaluateRisk(..., Throwable t) {
    log.warn("Risk Central unavailable, using fallback");
    return new RiskCentralResponse(
        600,              // Score neutral
        RiskLevel.MEDIUM, // Riesgo medio por defecto
        "Service unavailable - default evaluation"
    );
}
```

---

## 10. Testing

### 10.1 Unit Tests

**Ejemplo**:
```java
@Test
void shouldCreateMember() {
    // Given
    MemberRepositoryPort repository = mock(MemberRepositoryPort.class);
    var useCase = new CreateMemberUseCaseImpl(repository);
    var command = new CreateMemberCommand(...);
    
    when(repository.existsByDocument(...)).thenReturn(false);
    when(repository.save(any())).thenReturn(member);
    
    // When
    Member result = useCase.execute(command);
    
    // Then
    assertNotNull(result);
    assertEquals("Juan", result.getName());
    verify(repository).save(any());
}
```

### 10.2 Integration Tests

**Con Testcontainers**:
```java
@SpringBootTest
@Testcontainers
class MemberIntegrationTest {
    
    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0")
        .withDatabaseName("coopcredit_test");
    
    @Test
    void shouldPersistMember() {
        // Test con BD real en container
    }
}
```

### 10.3 API Tests

**Con REST Assured**:
```java
@Test
void shouldCreateCreditApplication() {
    given()
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .body(request)
    .when()
        .post("/api/credit-applications")
    .then()
        .statusCode(200)
        .body("status", equalTo("PENDING"));
}
```

---

## 11. Deployment

### 11.1 Environments

**Development** (`application-dev.yml`):
- MySQL local puerto 3307
- Logs nivel DEBUG
- Flyway enabled

**Production** (`application-prod.yml`):
- Database URL desde variable de entorno
- Logs nivel INFO
- SSL enabled

### 11.2 Docker Deployment

**Dockerfile**:
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build**:
```bash
mvn clean package -DskipTests
docker build -t coopcredit-service .
docker run -p 8080:8080 coopcredit-service
```

### 11.3 Production Checklist

- [ ] Variables de entorno configuradas
- [ ] JWT secret único y seguro
- [ ] SSL/TLS habilitado
- [ ] Logs en archivos rotados
- [ ] Backups de BD automatizados
- [ ] Monitoring alerts configurados
- [ ] Rate limiting implementado
- [ ] CORS restringido a dominios válidos

---

## 12. Conclusión

El sistema CoopCredit es un ejemplo profesional de aplicación empresarial moderna que combina:

- ✅ **Arquitectura limpia** con separation of concerns
- ✅ **Código de calidad** siguiendo SOLID principles
- ✅ **Resiliencia y confiabilidad** con circuit breakers
- ✅ **Observabilidad completa** para operación
- ✅ **Testing exhaustivo** en múltiples niveles
- ✅ **Documentación automática** con Swagger
- ✅ **Deployment simple** con Docker

El sistema está listo para producción y preparado para escalar horizontalmente.
