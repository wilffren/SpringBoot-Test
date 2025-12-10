# CoopCredit System - Decisiones de Diseño y Arquitectura

## 🏗️ Introducción

Este documento describe las decisiones arquitectónicas y de diseño tomadas en el desarrollo del sistema CoopCredit. Cada decisión está fundamentada en principios de ingeniería de software y requisitos específicos del proyecto.

---

## 1. Arquitectura Hexagonal (Puertos y Adaptadores)

### 📌 Decisión

**Implementar Arquitectura Hexagonal** separando la aplicación en tres capas: Domain, Application e Infrastructure.

### 🎯 Justificación

#### Beneficios Clave

1. **Independencia del dominio**: La lógica de negocio no depende de frameworks externos
2. **Testabilidad**: Tests unitarios sin necesidad de infraestructura
3. **Flexibilidad**: Cambiar tecnologías sin afectar el core
4. **Mantenibilidad**: Separación clara de responsabilidades
5. **Evolución**: Fácil adaptar a nuevos requisitos

#### Estructura de Capas

```
src/main/java/com/coopcredit/creditapplication/
├── domain/                          # ← Núcleo del negocio (INNER)
│   ├── model/                       #   Entidades de dominio (POJOs)
│   ├── ports/in/                    #   Use cases (interfaces)
│   ├── ports/out/                   #   Contratos salida (repositories, services)
│   └── exception/                   #   Excepciones de dominio
│
├── application/                     # ← Orquestación (MIDDLE)
│   └── usecases/                    #   Implementaciones de use cases
│
└── infrastructure/                  # ← Detalles técnicos (OUTER)
    ├── adapters/                    #   Implementaciones de ports/out
    ├── controllers/                 #   REST API controllers
    ├── entities/                    #   JPA entities
    ├── mappers/                     #   Conversión domain ↔ entity
    ├── repositories/                #   Spring Data repositories
    ├── security/                    #   JWT, Spring Security
    └── config/                      #   Configuración Spring
```

#### Flujo de Dependencias

```
Controller → Use Case (Port In) → Domain Model
                ↓
        Port Out ← Adapter → JPA/HTTP
```

**Regla de oro**: Las dependencias siempre apuntan HACIA ADENTRO (hacia el dominio).

### 💡 Alternativas Consideradas

| Alternativa | Razón de Descarte |
|-------------|------------------|
| **Arquitectura en Capas Tradicional** | Acoplamiento entre capas, difícil testear sin BD |
| **Arquitectura de Microservicios Compleja** | Overkill para el alcance actual, mayor complejidad operacional |
| **Monolito sin estructura** | Difícil de mantener y evolucionar |

### ✅ Resultado

Un sistema con dominio totalmente independiente, testeable al 100% sin infraestructura, y preparado para evolucionar.

---

## 2. Patrón Repository con Puertos

### 📌 Decisión

**Abstraer la persistencia mediante puertos** (`MemberRepositoryPort`, `CreditApplicationRepositoryPort`) implementados por adaptadores.

### 🎯 Justificación

```java
// Puerto en el dominio (abstracción)
public interface MemberRepositoryPort {
    Member save(Member member);
    Optional<Member> findById(Long id);
    // ...
}

// Adaptador en infraestructura (implementación)
@Component
public class MemberRepositoryAdapter implements MemberRepositoryPort {
    private final JpaMemberRepository jpaRepository;
    private final MemberMapper mapper;
    
    public Member save(Member member) {
        var entity = mapper.toEntity(member);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

#### Ventajas

1. **Inversión de dependencias**: Domain no depende de JPA
2. **Testeable**: Mocks sin Spring context
3. **Cambio de tecnología**: Migrarmysql → PostgreSQL/MongoDB sin tocar domain
4. **Separación de modelos**: Domain models ≠ JPA entities

### 💡 Alternativa Considerada

**Usar directamente Spring Data Repositories en use cases**  
❌ Descartado: Acopla el dominio a Spring Framework y JPA.

---

## 3. Mapeo Domain ↔ Entity con MapStruct

### 📌 Decisión

**Separar Domain Models de JPA Entities** using mappers estratégicos.

### 🎯 Justificación

#### Problema

Las entidades JPA tienen anotaciones técnicas (@Entity, @Table, @Column) que no deben contaminar el dominio puro.

#### Solución

```java
// Dominio (POJO limpio)
public class Member {
    private Long id;
    private String document;
    private String name;
    private BigDecimal salary;
    
    // Lógica de negocio
    public BigDecimal getMaxCreditAmount() {
        return salary.multiply(BigDecimal.valueOf(4));
    }
}

// Entidad JPA (con anotaciones técnicas)
@Entity
@Table(name = "members")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String document;
    
    // ... sin lógica de negocio
}

// Mapper (conversión automática)
@Component
public class MemberMapper {
    public Member toDomain(MemberEntity entity) { ... }
    public MemberEntity toEntity(Member domain) { ... }
}
```

#### Ventajas

1. **Dominio limpio**: Sin dependencias de JPA
2. **Mantenibilidad**: Cambios en BD no afectan dominio
3. **Performance**: Control fino sobre lazy loading
4. **Testing**: Domain models instanciables sin Hibernate

### 💡 Alternativa Considerada

**Usar mismas clases para dominio y persistencia**  
❌ Descartado: Contamina el dominio con preocupaciones técnicas.

---

## 4. Use Cases como Servicios con Responsabilidad Única

### 📌 Decisión

**Un use case = una operación de negocio** siguiendo SRP estricto.

### 🎯 Justificación

En lugar de servicios "gordos" con múltiples métodos:

```java
// ❌ MAL: Service con múltiples responsabilidades
@Service
public class CreditService {
    public CreditApplication create(...) { }
    public CreditApplication getById(...) { }
    public List<CreditApplication> list(...) { }
    public RiskEvaluation evaluate(...) { }
}
```

Usamos use cases específicos:

```java
// ✅ BIEN: Use cases específicos con responsabilidad única

@Service
public class CreateCreditApplicationUseCaseImpl 
    implements CreateCreditApplicationUseCase {
    // Solo crea solicitudes
}

@Service
public class GetCreditApplicationUseCaseImpl 
    implements GetCreditApplicationUseCase {
    // Solo consulta solicitudes
}

@Service
public class EvaluateCreditApplicationUseCaseImpl 
    implements EvaluateCreditApplicationUseCase {
    // Solo evalúa solicitudes
}
```

#### Ventajas

1. **SRP**: Una razón para cambiar
2. **ISP**: Dependencias mínimas
3. **Testing**: Tests enfocados y simples
4. **Reusabilidad**: Composición de use cases

---

## 5. Records para DTOs y Commands

### 📌 Decisión

**Usar Java Records** para DTOs, Commands y Responses (Java 21).

### 🎯 Justificación

```java
// Command pattern con Record (inmutable)
public interface CreateMemberUseCase {
    record CreateMemberCommand(
        String document,
        String name,
        BigDecimal salary,
        LocalDate affiliationDate
    ) {}
    
    Member execute(CreateMemberCommand command);
}

// DTO response
record MemberResponse(
    Long id,
    String document,
    String name,
    BigDecimal salary,
    LocalDate affiliationDate,
    String status
) {}
```

#### Ventajas

1. **Inmutabilidad**: Thread-safe por defecto
2. **Menos boilerplate**: No getters/setters/equals/hashCode
3. **Semántica clara**: Indica que es un value object
4. **Pattern matching**: Preparado para futuras features de Java

### 💡 Alternativa Considerada

**Usar clases con Lombok @Data**  
✅ También válido, pero Records es la forma idiomática en Java 21.

---

## 6. JWT para Autenticación Stateless

### 📌 Decisión

**Implementar autenticación JWT** sin sesiones en servidor.

### 🎯 Justificación

```java
// Generación de token
public String generateToken(User user) {
    return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .setExpiration(new Date(now + 24h))
            .signWith(secretKey, HS256)
            .compact();
}

// Validación en cada request
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Set SecurityContext
        }
    }
}
```

#### Ventajas

1. **Stateless**: No sesiones en servidor
2. **Escalable**: Horizontal scaling sin sticky sessions
3. **Microservices ready**: Token válido en múltiples servicios
4. **Mobile friendly**: Auth para SPAs y mobile apps

#### Configuración de Seguridad

- Expiración: 24 horas
- Algoritmo: HS256
- Claims: username, role, userId
- Secret: Variable de entorno

### 💡 Alternativa Considerada

**Sesiones con Spring Session + Redis**  
❌ Descartado: Requiere infraestructura adicional, no stateless.

---

## 7. Resilience4j para Circuit Breaker

### 📌 Decisión

**Implementar Circuit Breaker y Retry** para llamadas a Risk Central Service.

### 🎯 Justificación

```java
@Component
public class RiskCentralHttpAdapter implements RiskCentralPort {
    
    @CircuitBreaker(name = "riskCentral", fallbackMethod = "fallbackEvaluateRisk")
    @Retry(name = "riskCentral")
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        // Llamada HTTP al servicio externo
    }
    
    // Fallback cuando el servicio está caído
    private RiskCentralResponse fallbackEvaluateRisk(
            RiskCentralRequest request, Throwable t) {
        log.warn("Risk Central unavailable, using fallback");
        return new RiskCentralResponse(600, RiskLevel.MEDIUM, "Service unavailable");
    }
}
```

#### Configuración

```yaml
resilience4j:
  circuitbreaker:
    instances:
      riskCentral:
        slidingWindowSize: 10
        failureRateThreshold: 50%
        waitDurationInOpenState: 30s
  retry:
    instances:
      riskCentral:
        maxAttempts: 3
        waitDuration: 1s
```

#### Ventajas

1. **Resiliencia**: Sistema sigue funcionando si Risk Central falla
2. **Fallback inteligente**: Decisión por defecto (MEDIUM risk)
3. **Prevención de cascadas**: Circuit breaker evita llamadas innecesarias
4. **Retry automático**: Manejo de fallos transitorios

---

## 8. Flyway para Migraciones de Base de Datos

### 📌 Decisión

**Usar Flyway** para versionamiento del schema de BD.

### 🎯 Just ificación

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_members_table.sql
├── V3__create_credit_applications_table.sql
├── V4__create_risk_evaluations_table.sql
├── V5__add_foreign_keys.sql
└── V6__insert_initial_data.sql
```

#### Ventajas

1. **Versionamiento**: Historia completa de cambios en BD
2. **Reproducibilidad**: Mismo schema en dev/test/prod
3. **Rollback controlado**: Scripts de reversión
4. **CI/CD friendly**: Migraciones automáticas en deployment

### 💡 Alternativa Considerada

**Hibernate auto-DDL (ddl-auto=update)**  
❌ Descartado: No apto para producción, sin control de cambios.

---

## 9. Bean Validation para Validación de Datos

### 📌 Decisión

**Usar Bean Validation (Jakarta Validation)** en DTOs de entrada.

### 🎯 Justificación

```java
record CreateMemberRequest(
    @NotBlank(message = "Document is required")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Document must be 8-12 digits")
    String document,
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    String name,
    
    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    BigDecimal salary,
    
    @NotNull(message = "Affiliation date is required")
    @PastOrPresent(message = "Affiliation date must be in the past or present")
    LocalDate affiliationDate
) {}

@PostMapping
public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
    // Si no es válido, Spring lanza MethodArgumentNotValidException
}
```

#### Ventajas

1. **Declarativo**: Validaciones claras con anotaciones
2. **Reutilizable**: Validaciones en la definición del DTO
3. **Mensajes personalizados**: Feedback claro al cliente
4. **Standard**: Jakarta Validation es estándar Java

---

## 10. Observabilidad con Prometheus + Grafana

### 📌 Decisión

**Implementar stack completo de observabilidad** con Micrometer, Prometheus y Grafana.

### 🎯 Justificación

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### Componentes

1. **Micrometer**: Métricas de aplicación (JVM, HTTP, custom)
2. **Prometheus**: Scraping y almacenamiento de time-series
3. **Grafana**: Visualización y dashboards

#### Métricas Clave

- **JVM**: Heap usage, GC, threads
- **HTTP**: Requests/sec, latency, status codes
- **Custom**: Credit applications created, evaluations performed
- **Resilience**: Circuit breaker states, retry attempts

---

## 11. OpenAPI/Swagger para Documentación de API

### 📌 Decisión

**Generar documentación interactiva** con SpringDoc OpenAPI.

### 🎯 Justificación

```java
@RestController
@RequestMapping("/api/credit-applications")
@Tag(name = "3. Credit Applications", description = "Credit application management")
@SecurityRequirement(name = "bearerAuth")
public class CreditApplicationController {
    
    @PostMapping
    @Operation(summary = "Create a new credit application")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Application created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CreditApplicationResponse> create(
            @Valid @RequestBody CreateApplicationRequest request) {
        // ...
    }
}
```

#### Características

- **UI Interactiva**: Swagger UI en `/swagger-ui.html`
- **Testing en vivo**: Ejecutar requests desde el browser
- **Auth integrado**: JWT bearer token en UI
- **Spec OpenAPI 3.0**: Exportable para generación de clientes

---

## 12. Multi-Environment Configuration

### 📌 Decisión

**Perfiles Spring para diferentes ambientes** (dev, prod).

### 🎯 Justificación

```
application.yml           # Configuración común
application-dev.yml       # Desarrollo local
application-prod.yml      # Producción
```

#### Ejemplo: Base de Datos

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/coopcredit
    username: root
    password: root

# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}  # Variable de entorno
```

#### Ventajas

1. **Seguridad**: Credentials no en código
2. **Flexibilidad**: Configuración por ambiente
3. **12-Factor App**: Configuración por entorno

---

## 13. Containerización con Docker

### 📌 Decisión

**Containerizar servicios** con Docker Compose para desarrollo.

### 🎯 Justificación

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: coopcredit
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3307:3307"
  
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
```

#### Ventajas

1. **Reproducibilidad**: Mismo ambiente en todos los devs
2. **Dependencies management**: Todo en containers
3. **CI/CD ready**: Mismo setup en pipelines

---

## 14. Testing Strategy

### 📌 Decisión

**Testing multinivel**: Unit → Integration → E2E.

### 🎯 Justificación

#### Unit Tests (Use Cases)

```java
class CreateMemberUseCaseImplTest {
    @Test
    void shouldCreateMember() {
        // Mock repository
        MemberRepositoryPort repository = mock(MemberRepositoryPort.class);
        var useCase = new CreateMemberUseCaseImpl(repository);
        
        // Test business logic sin infraestructura
    }
}
```

#### Integration Tests (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class MemberIntegrationTest {
    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");
    
    @Test
    void shouldPersistMember() {
        // Test con BD real en container
    }
}
```

#### API Tests (REST Assured)

```java
@Test
void shouldCreateCreditApplication() {
    given()
        .header("Authorization", "Bearer " + token)
        .contentType(JSON)
        .body(request)
    .when()
        .post("/api/credit-applications")
    .then()
        .statusCode(200)
        .body("status", equalTo("PENDING"));
}
```

---

## 📊 Resumen de Decisiones

| Decisión | Tecnología/Patrón | Beneficio Principal |
|----------|------------------|-------------------|
| Arquitectura | Hexagonal | Independencia del dominio |
| Persistencia | Repository Pattern | Abstracción de BD |
| Mapeo | MapStruct | Separación domain/entity |
| Use Cases | SRP Services | Responsabilidad única |
| DTOs | Java Records | Inmutabilidad |
| Auth | JWT | Stateless |
| Resiliencia | Resilience4j | Circuit breaker |
| Migraciones | Flyway | Versionamiento BD |
| Validación | Bean Validation | Declarativa |
| Observabilidad | Prometheus+Grafana | Métricas |
| API Docs | Swagger/OpenAPI | Documentación automática |
| Config | Spring Profiles | Multi-environment |
| Testing | JUnit+Testcontainers | Múltiples niveles |

---

## 🎯 Conclusión

Las decisiones arquitectónicas del sistema CoopCredit están fundamentadas en:

1. **Principios SOLID**: Diseño orientado a objetos de calidad
2. **Clean Architecture**: Independencia del dominio
3. **Best Practices**: Patrones probados de la industria
4. **Pragmatismo**: Soluciones apropiadas al contexto

El resultado es un sistema:

- ✅ **Mantenible**: Separación clara, código limpio
- ✅ **Testeable**: Testing sin infraestructura
- ✅ **Escalable**: Stateless, horizontal scaling
- ✅ **Resiliente**: Manejo de fallos, circuit breakers
- ✅ **Observable**: Métricas y monitoring completo
- ✅ **Profesional**: Estándares de la industria

Estas decisiones facilitan la evolución futura del sistema mientras mantienen la calidad del código.
