---
title: "CoopCredit System - Reporte Técnico Completo"
subtitle: "Sistema Integral de Gestión de Solicitudes de Crédito"
author: "Análisis Técnico y Arquitectónico"
date: "Diciembre 2024"
toc: true
toc-title: "Tabla de Contenidos"
titlepage: true
titlepage-color: "1E3A8A"
titlepage-text-color: "FFFFFF"
titlepage-rule-color: "FFFFFF"
titlepage-rule-height: 2
---

\newpage

# Resumen Ejecutivo

Este documento presenta un análisis técnico exhaustivo del sistema **CoopCredit**, una plataforma integral de gestión de solicitudes de crédito construida con arquitectura hexagonal y las mejores prácticas de desarrollo de software moderno.

## Contenido del Documento

1. **Resumen del Proyecto** - Descripción general, arquitectura y funcionalidades
2. **Análisis de Principios SOLID** - Implementación detallada con ejemplos de código
3. **Decisiones de Diseño** - Justificación de decisiones arquitectónicas
4. **Manual de Usuario** - Guía completa de instalación y uso
5. **Documentación Técnica** - Especificaciones técnicas detalladas

## Tecnologías Principales

- Spring Boot 3.3.0 con Java 21
- Arquitecturahexagonal (Puertos y Adaptadores)
- MySQL 8.0 con Flyway migrations
- JWT Authentication
- Resilience4j (Circuit Breaker)
- Observabilidad: Prometheus + Grafana

\newpage

\part{Parte I: Resumen del Proyecto}

# CoopCredit System - Resumen Ejecutivo del Proyecto

## 📋 Descripción General

El **CoopCredit System** es un sistema integral de gestión de solicitudes de crédito diseñado específicamente para cooperativas de crédito. El sistema automatiza el proceso completo desde la afiliación de miembros hasta la evaluación y aprobación de solicitudes de crédito, integrándose con servicios externos de evaluación de riesgo crediticio.

### Propósito

Proporcionar una plataforma robusta, escalable y segura que permita a las cooperativas:

- **Gestionar afiliados**: Registro y actualización de información de miembros
- **Procesar solicitudes**: Creación y seguimiento de aplicaciones de crédito
- **Evaluar riesgos**: Integración con centrales de riesgo para evaluación automatizada
- **Monitorear operaciones**: Observabilidad completa con métricas y dashboards en tiempo real
- **Garantizar seguridad**: Autenticación y autorización basada en roles con JWT

---

## 🏗️ Arquitectura del Sistema

### Patrón Arquitectónico: Hexagonal (Ports & Adapters)

El proyecto implementa **Arquitectura Hexagonal** (también conocida como Puertos y Adaptadores), que separa claramente las preocupaciones del negocio de los detalles técnicos de infraestructura.

#### Estructura de Capas

```
┌─────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Controllers  │  │  Adapters    │  │   Security   │     │
│  │  (REST API)  │  │ (JPA/HTTP)   │  │    (JWT)     │     │
│  └──────┬───────┘  └───────┬──────┘  └──────────────┘     │
└─────────┼──────────────────┼─────────────────────────────────┘
          │                  │
          ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│               ┌──────────────────────┐                      │
│               │   Use Cases Impl     │                      │
│               │  (Business Logic)    │                      │
│               └──────────┬───────────┘                      │
└────────────────────────┬─┴─────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Models    │  │ Input Ports  │  │ Output Ports │      │
│  │ (Entities)  │  │ (Use Cases)  │  │(Repositories)│      │
│  └─────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

**Beneficios Clave:**
- ✅ **Independencia de frameworks**: El dominio no depende de Spring, JPA, etc.
- ✅ **Testabilidad**: Fácil crear tests unitarios sin infraestructura
- ✅ **Flexibilidad**: Cambiar implementaciones sin afectar el negocio
- ✅ **Mantenibilidad**: Código organizado y con responsabilidades claras

---

## 🎯 Funcionalidades Principales

### 1. Gestión de Autenticación y Usuarios

- **Registro de Usuarios**: Crear cuentas con roles diferenciados (ADMIN, ANALYST, MEMBER)
- **Autenticación JWT**: Login seguro con tokens de larga duración
- **Autorización por Roles**: Control de acceso granular a endpoints

**Roles:**
- `ADMIN`: Acceso completo al sistema
- `ANALYST`: Puede evaluar solicitudes y consultar información
- `MEMBER`: Puede crear solicitudes propias y consultar su información

### 2. Gestión de Afiliados (Members)

- **Crear Afiliado**: Registro con documento, nombre, salario y fecha de afiliación
- **Consultar Afiliado**: Por ID o por documento de identidad
- **Actualizar Afiliado**: Modificar información y estado
- **Validaciones**: 
  - Antigüedad mínima de 6 meses para solicitar crédito
  - Estado activo/inactivo
  - Documento único

### 3. Solicitudes de Crédito (Credit Applications)

- **Crear Solicitud**: Especificar monto, plazo y tasa propuesta
- **Consultar Solicitudes**: Por ID, por afiliado o por estado
- **Estados**: PENDING, APPROVED, REJECTED

**Reglas de Negocio:**
- ✓ Afiliado debe estar activo
- ✓ Mínimo 6 meses de antigüedad
- ✓ Cálculo automático de cuota mensual
- ✓ Validación de montos y plazos

### 4. Evaluación de Riesgo Crediticio

- **Integración con Risk Central**: Consulta automática a servicio externo
- **Evaluación Automatizada**: Basada en múltiples factores
  - Score crediticio externo
  - Relación cuota/ingreso
  - Antigüedad del afiliado
  - Monto máximo según salario
- **Decisión Final**: APPROVED / REJECTED con razón detallada
- **Resiliencia**: Circuit breaker y retry para manejo de fallos

---

## 🏛️ Arquitectura de Microservicios

El sistema está compuesto por dos microservicios independientes:

### 1. Credit Application Service (Puerto 8080)

**Responsabilidad**: Servicio principal de gestión de créditos

**Componentes:**
- REST API con Swagger/OpenAPI
- Lógica de negocio (use cases)
- Persistencia en MySQL
- Seguridad JWT
- Métricas y observabilidad

**Endpoints Principales:**
- `/api/auth/*` - Autenticación
- `/api/members/*` - Gestión de afiliados
- `/api/credit-applications/*` - Solicitudes de crédito
- `/actuator/*` - Health checks y métricas

### 2. Risk Central Mock Service (Puerto 8081)

**Responsabilidad**: Simulación de central de riesgo externa

**Funcionalidad:**
- API REST para evaluación de riesgo
- Generación determinística de scores (basado en documento)
- Respuestas consistentes para testing

**Endpoint:**
- `POST /api/risk/evaluate` - Evaluar riesgo crediticio

---

## 🛠️ Stack Tecnológico

### Backend Framework
- **Spring Boot 3.3.0** - Framework principal
- **Java 21** - Lenguaje de programación
- **Maven** - Gestión de dependencias

### Persistencia
- **MySQL 8.0** - Base de datos relacional
- **Spring Data JPA** - ORM y acceso a datos
- **Flyway** - Migraciones de base de datos
- **H2 Database** - Base de datos en memoria para testing

### Seguridad
- **Spring Security** - Framework de seguridad
- **JWT (jjwt 0.11.5)** - Autenticación basada en tokens
- **BCrypt** - Encriptación de contraseñas

### Resiliencia
- **Resilience4j** - Circuit breaker y retry patterns
- Configuración para llamadas a Risk Central:
  - Circuit breaker con fallback
  - Retry con 3 intentos
  - Timeout y backoff configurables

### Observabilidad
- **Micrometer** - Métricas de aplicación
- **Prometheus** - Recolección de métricas
- **Grafana** - Visualización y dashboards
- **Spring Boot Actuator** - Health checks y endpoints de gestión

### Documentación
- **SpringDoc OpenAPI 3** - Generación automática de documentación API
- **Swagger UI** - Interfaz interactiva para probar endpoints

### Mapeo de Objetos
- **MapStruct 1.5.5** - Mapeo automático entre DTOs, entidades y modelos de dominio
- **Lombok** - Reducción de código boilerplate

### Testing
- **JUnit 5** - Framework de testing
- **Testcontainers** - Testing con contenedores Docker
- **REST Assured** - Testing de APIs REST
- **Spring Security Test** - Testing de seguridad

### DevOps & Containerización
- **Docker** - Containerización de servicios
- **Docker Compose** - Orquestación de contenedores
- Scripts shell para automatización (`start.sh`, `stop.sh`)

---

## 📁 Estructura del Proyecto

```
coopcredit-system/
├── credit-application-service/          # Servicio principal
│   ├── src/main/java/../
│   │   ├── domain/                      # Capa de dominio
│   │   │   ├── model/                   # Entidades de negocio
│   │   │   ├── ports/in/                # Casos de uso (interfaces)
│   │   │   ├── ports/out/               # Contratos de salida
│   │   │   └── exception/               # Excepciones de dominio
│   │   ├── application/                 # Capa de aplicación
│   │   │   └── usecases/                # Implementaciones de casos de uso
│   │   └── infrastructure/              # Capa de infraestructura
│   │       ├── adapters/                # Adaptadores (JPA, HTTP)
│   │       ├── controllers/             # REST controllers
│   │       ├── entities/                # JPA entities
│   │       ├── mappers/                 # MapStruct mappers
│   │       ├── repositories/            # Spring Data repositories
│   │       ├── security/                # Configuración JWT
│   │       └── config/                  # Configuración Spring
│   ├── src/main/resources/
│   │   ├── application.yml              # Configuración principal
│   │   ├── application-dev.yml          # Perfil desarrollo
│   │   └── application-prod.yml         # Perfil producción
│   └── pom.xml                          # Dependencias Maven
│
├── risk-central-mock-service/           # Mock de central de riesgo
│   ├── src/main/java/../
│   │   ├── controller/                  # REST controller
│   │   ├── service/                     # Lógica de scoring
│   │   └── dto/                         # DTOs de request/response
│   └── pom.xml
│
├── docs/                                # Documentación
│   ├── diagrams/                        # Diagramas PlantUML
│   │   ├── architecture.puml            # Arquitectura hexagonal
│   │   ├── microservices.puml           # Diagrama de microservicios
│   │   └── use-cases.puml               # Casos de uso
│   └── analysis/                        # Análisis y documentación técnica
│
├── postman/                             # Colección de Postman
│   └── CoopCredit_API_Collection.json   # Requests de prueba
│
├── grafana_provisioning/                # Configuración Grafana
│   ├── datasources/                     # Datasources automáticos
│   └── dashboards/                      # Dashboards precargados
│
├── docker-compose.yml                   # Orquestación de contenedores
├── prometheus.yml                       # Configuración Prometheus
├── start.sh                             # Script de inicio
└── stop.sh                              # Script de parada
```

---

## 🚀 Componentes Principales

### Domain Layer (Núcleo del Negocio)

**Modelos de Dominio:**
- `Member`: Afiliado de la cooperativa
- `CreditApplication`: Solicitud de crédito
- `RiskEvaluation`: Evaluación de riesgo
- `User`: Usuario del sistema

**Puertos de Entrada (Use Cases):**
- `RegisterUserUseCase`
- `AuthenticateUserUseCase`
- `CreateMemberUseCase`
- `GetMemberUseCase`
- `UpdateMemberUseCase`
- `CreateCreditApplicationUseCase`
- `GetCreditApplicationUseCase`
- `ListCreditApplicationsUseCase`
- `EvaluateCreditApplicationUseCase`

**Puertos de Salida:**
- `UserRepositoryPort`
- `MemberRepositoryPort`
- `CreditApplicationRepositoryPort`
- `RiskEvaluationRepositoryPort`
- `RiskCentralPort`

### Application Layer

Implementaciones concretas de los casos de uso que orquestan la lógica de negocio:
- Validaciones de reglas de negocio
- Coordinación entre repositorios
- Manejo de excepciones de dominio

### Infrastructure Layer

**Adaptadores:**
- `*RepositoryAdapter`: Implementan ports de repositorio usando JPA
- `RiskCentralHttpAdapter`: Cliente HTTP para Risk Central con resilience

**Controllers:**
- `AuthController`: Endpoints de autenticación
- `MemberController`: CRUD de afiliados
- `CreditApplicationController`: Gestión de solicitudes

**Security:**
- `JwtAuthenticationFilter`: Interceptor para validar tokens
- `SecurityConfig`: Configuración de Spring Security
- Gestión de roles y permisos

---

## 🔄 Flujo de Operación Típico

### Flujo Completo de Solicitud de Crédito

```
1. REGISTRO Y LOGIN
   Usuario → POST /api/auth/register → Sistema crea usuario con rol
   Usuario → POST /api/auth/login → Sistema genera JWT token
   
2. CREAR AFILIADO
   Admin → POST /api/members → Sistema valida y crea afiliado
   Sistema verifica documento único
   
3. CREAR SOLICITUD
   Member → POST /api/credit-applications → Sistema valida:
   - Afiliado existe y está activo
   - Tiene mínimo 6 meses de antigüedad
   - Montos y plazos válidos
   Sistema crea solicitud en estado PENDING
   
4. EVALUACIÓN AUTOMÁTICA
   Analyst → POST /api/credit-applications/{id}/evaluate
   Sistema ejecuta:
   a) Consulta Risk Central (con circuit breaker)
   b) Calcula ratio cuota/ingreso
   c) Verifica antigüedad y monto máximo
   d) Determina decisión final: APPROVED/REJECTED
   e) Guarda evaluación con razón detallada
   
5. CONSULTA RESULTADOS
   Usuario → GET /api/credit-applications/{id}
   Sistema retorna solicitud con estado actualizado
```

---

## 📊 Observabilidad e Infraestructura

### Métricas y Monitoreo

**Prometheus (Puerto 9091):**
- Scraping de métricas cada 15 segundos
- Métricas JVM: heap, threads, GC
- Métricas de aplicación: requests, duración, errores
- Métricas de Resilience4j: circuit breaker states

**Grafana (Puerto 3000):**
- Dashboards precargados automáticamente
- Visualización de métricas de negocio
- Alertas configurables
- Credentials: admin/admin

**Spring Boot Actuator:**
- `/actuator/health`: Estado de la aplicación
- `/actuator/metrics`: Métricas disponibles
- `/actuator/prometheus`: Endpoint para Prometheus

### Base de Datos

**MySQL (Puerto 3307):**
- Base de datos: `coopcredit`
- Usuario: root / root
- Persistent volumes via Docker

**Esquema:**
- `users`: Usuarios y autenticación
- `members`: Afiliados de la cooperativa
- `credit_applications`: Solicitudes de crédito
- `risk_evaluations`: Evaluaciones de riesgo

---

## 🔐 Seguridad

### Autenticación JWT

- **Token Generation**: Al hacer login exitoso
- **Token Validation**: En cada request mediante filter
- **Expiración**: 24 horas (configurable)
- **Claims**: username, roles, authorities

### Autorización

- **Role-Based Access Control (RBAC)**
- Anotaciones `@PreAuthorize` en endpoints sensibles
- Ejemplo: Solo ANALYST y ADMIN pueden evaluar solicitudes

### Protección

- CORS configurado para desarrollo
- Passwords encriptados con BCrypt
- Endpoints públicos: `/api/auth/**`, `/actuator/health`
- Resto requiere JWT válido

---

## 🧪 Testing

### Estrategia de Testing

**Unit Tests:**
- Tests de casos de uso aislados
- Mocks de repositorios y servicios externos
- Cobertura de reglas de negocio

**Integration Tests:**
- Testcontainers para MySQL
- Tests end-to-end de flujos completos
- Validación de integraciones

**API Tests:**
- REST Assured para testing de endpoints
- Validación de respuestas HTTP
- Tests de seguridad (sin token, roles incorrectos)

### Colección Postman

Incluye requests para:
- ✓ Autenticación (registro, login)
- ✓ CRUD de afiliados
- ✓ Creación y evaluación de solicitudes
- ✓ Tests de validación (datos inválidos)
- ✓ Tests de seguridad (acceso sin token)
- ✓ Endpoints de observabilidad

---

## 🎨 Patrones de Diseño Aplicados

1. **Hexagonal Architecture**: Separación de capas e inversión de dependencias
2. **Repository Pattern**: Abstracción de persistencia
3. **Adapter Pattern**: Adaptadores para tecnologías externas
4. **Builder Pattern**: Construcción de domain models
5. **Command Pattern**: Use case commands con records
6. **Strategy Pattern**: Mappers intercambiables
7. **Circuit Breaker Pattern**: Resiliencia en llamadas externas
8. **Dependency Injection**: Inyección de dependencias vía constructor

---

## 📈 Escalabilidad y Rendimiento

### Optimizaciones

- **Connection Pooling**: HikariCP para gestión de conexiones
- **Lazy Loading**: JPA lazy fetching para relaciones
- **Caching**: Preparado para implementar cache distribuido
- **Async Processing**: Preparado para procesamiento asíncrono

### Deployment

- **Containerización**: Docker para todos los servicios
- **Multi-Environment**: Perfiles Spring (dev/prod)
- **Cloud Ready**: Compatible con Render, AWS, etc.
- **Stateless**: Aplicación stateless para escalado horizontal

---

## 🎯 Casos de Uso Soportados

### UC-01: Registro de Usuario
Actor: Administrador del sistema  
Flujo: Registrar nuevo usuario con rol específico

### UC-02: Autenticación
Actor: Usuario  
Flujo: Login con credenciales, obtención de JWT

### UC-03: Gestión de Afiliados
Actor: Administrador  
Flujo: Crear, consultar y actualizar información de afiliados

### UC-04: Solicitud de Crédito
Actor: Afiliado o Analista  
Flujo: Crear solicitud especificando monto, plazo y tasa

### UC-05: Evaluación de Riesgo
Actor: Analista  
Flujo: Evaluar solicitud consultando central de riesgo y aplicando reglas

### UC-06: Consulta de Solicitudes
Actor: Todos los usuarios autenticados  
Flujo: Consultar solicitudes con filtros (afiliado, estado)

---

## 🌟 Características Destacadas

### ✨ Arquitectura Limpia
- Separación clara de responsabilidades
- Código testeable y mantenible
- Independiente de frameworks

### 🔒 Seguridad Robusta
- JWT authentication
- Autorización basada en roles
- Validación de datos con Bean Validation

### 📡 Observabilidad Completa
- Métricas en tiempo real
- Dashboards visuales
- Health checks automáticos

### 🛡️ Resiliencia
- Circuit breakers para servicios externos
- Retry automático
- Fallbacks configurados

### 📚 Documentación Automática
- Swagger UI integrado
- OpenAPI 3.0 specification
- Ejemplos en Postman

### 🚀 DevOps Friendly
- Scripts de automatización
- Docker Compose para desarrollo
- Multi-environment configuration

---

## 📝 Conclusión

El **CoopCredit System** es un sistema empresarial completo que demuestra las mejores prácticas en desarrollo de software moderno:

- **Arquitectura sólida**: Hexagonal con separación de responsabilidades
- **Código limpio**: Siguiendo principios SOLID y clean code
- **Testing exhaustivo**: Múltiples niveles de testing
- **Producción ready**: Observabilidad, seguridad y resiliencia
- **Documentación completa**: API docs, diagramas y ejemplos

Es un ejemplo ideal de cómo construir sistemas escalables, mantenibles y robustos utilizando Spring Boot y arquitectura hexagonal.
# PART I - COMPLETE
\newpage\part{Parte II: Análisis de Principios SOLID}
# CoopCredit System - Análisis de Principios SOLID

## 📚 Introducción

Este documento analiza la implementación de los **Principios SOLID** en el sistema CoopCredit. Los principios SOLID son cinco principios fundamentales de diseño orientado a objetos que promueven código mantenible, escalable y testeable.

Los 5 principios son:
1. **S**ingle Responsibility Principle (SRP)
2. **O**pen/Closed Principle (OCP)
3. **L**iskov Substitution Principle (LSP)
4. **I**nterface Segregation Principle (ISP)
5. **D**ependency Inversion Principle (DIP)

---

## 1. Single Responsibility Principle (SRP)

> Una clase debe tener una sola razón para cambiar.

### ✅ Implementación en el Proyecto

El proyecto implementa SRP de manera estricta, especialmente en:

#### 1.1 Use Cases con Responsabilidad Única

Cada caso de uso tiene una única responsabilidad claramente definida:

**Ejemplo: `CreateMemberUseCaseImpl`**

```java
@Service
@Transactional
public class CreateMemberUseCaseImpl implements CreateMemberUseCase {
    
    private final MemberRepositoryPort memberRepository;
    
    public CreateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(CreateMemberCommand command) {
        // ÚNICA RESPONSABILIDAD: Crear un nuevo afiliado
        if (memberRepository.existsByDocument(command.document())) {
            throw new ConflictException("Member", "document", command.document());
        }
        
        Member member = Member.builder()
                .document(command.document())
                .name(command.name())
                .salary(command.salary())
                .affiliationDate(command.affiliationDate())
                .status(MemberStatus.ACTIVE)
                .build();
        
        return memberRepository.save(member);
    }
}
```

**Razón única**: Crear un nuevo afiliado validando que no exista duplicado.

**Ejemplo: `CreateCreditApplicationUseCaseImpl`**

```java
@Service
@Transactional
public class CreateCreditApplicationUseCaseImpl implements CreateCreditApplicationUseCase {
    
    @Override
    public CreditApplication execute(CreateCreditApplicationCommand command) {
        // ÚNICA RESPONSABILIDAD: Crear solicitud de crédito con validaciones básicas
        
        // 1. Validar que el miembro existe y está activo
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new NotFoundException("Member", "id", command.memberId()));
        
        if (!member.isActive()) {
            throw new BusinessRuleException("MEMBER_INACTIVE", "Member is not active");
        }
        
        // 2. Validar antigüedad
        if (member.getSeniorityInMonths() < 6) {
            throw new BusinessRuleException("INSUFFICIENT_SENIORITY", 
                    "Member must have at least 6 months of seniority");
        }
        
        // 3. Crear solicitud
        CreditApplication creditApplication = CreditApplication.builder()
                .memberId(command.memberId())
                .requestedAmount(command.requestedAmount())
                .termMonths(command.termMonths())
                .proposedRate(command.proposedRate())
                .applicationDate(LocalDate.now())
                .status(ApplicationStatus.PENDING)
                .build();
        
        return creditApplicationRepository.save(creditApplication);
    }
}
```

**Razón única**: Orquestar la creación de solicitudes con validaciones de negocio previas.

#### 1.2 Controladores Específicos por Dominio

Cada controlador maneja un solo recurso REST:

**`AuthController`**: Solo autenticación
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // ÚNICA RESPONSABILIDAD: Gestionar autenticación (register, login)
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) { ... }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) { ... }
}
```

**`MemberController`**: Solo gestión de afiliados
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {
    // ÚNICA RESPONSABILIDAD: CRUD de afiliados
    
    @PostMapping
    public ResponseEntity<MemberResponse> create(...) { ... }
    
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(...) { ... }
    
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(...) { ... }
}
```

**`CreditApplicationController`**: Solo solicitudes de crédito
```java
@RestController
@RequestMapping("/api/credit-applications")
public class CreditApplicationController {
    // ÚNICA RESPONSABILIDAD: Gestión de solicitudes de crédito
    
    @PostMapping
    public ResponseEntity<CreditApplicationResponse> create(...) { ... }
    
    @GetMapping("/{id}")
    public ResponseEntity<CreditApplicationResponse> getById(...) { ... }
    
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<EvaluationResponse> evaluate(...) { ... }
}
```

#### 1.3 Adaptadores con ResponsabilidadÚnica

**`CreditApplicationRepositoryAdapter`**

```java
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    
    private final JpaCreditApplicationRepository jpaRepository;
    private final CreditApplicationMapper mapper;
    
    // ÚNICA RESPONSABILIDAD: Adaptar JPA repository al puerto de dominio
    
    @Override
    public CreditApplication save(CreditApplication creditApplication) {
        var entity = mapper.toEntity(creditApplication);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<CreditApplication> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    // ... otros métodos de acceso a datos
}
```

**Razón única**: Traducir entre el modelo de dominio y JPA entities.

**`RiskCentralHttpAdapter`**

```java
@Component
public class RiskCentralHttpAdapter implements RiskCentralPort {
    
    // ÚNICA RESPONSABILIDAD: Comunicación HTTP con Risk Central Service
    
    @Override
    @CircuitBreaker(name = "riskCentral", fallbackMethod = "fallbackEvaluateRisk")
    @Retry(name = "riskCentral")
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        // Llamada HTTP al servicio externo
        // Mapeo de response
        // Manejo de errores
    }
}
```

**Razón única**: Gestionar la comunicación HTTP con el servicio de riesgo externo.

#### 1.4 Modelos de Dominio con Lógica de Negocio Específica

**`Member`**

```java
public class Member {
    // Datos del afiliado
    
    // RESPONSABILIDAD: Lógica de negocio relacionada con afiliados
    
    public long getSeniorityInMonths() {
        if (affiliationDate == null) return 0;
        return ChronoUnit.MONTHS.between(affiliationDate, LocalDate.now());
    }
    
    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(status);
    }
    
    public BigDecimal getMaxCreditAmount() {
        // Business rule: max credit = 4x salary
        return salary.multiply(BigDecimal.valueOf(4));
    }
}
```

**`CreditApplication`**

```java
public class CreditApplication {
    // Datos de la solicitud
    
    // RESPONSABILIDAD: Lógica de negocio relacionada con solicitudes
    
    public BigDecimal calculateMonthlyPayment() {
        if (requestedAmount == null || termMonths == null || proposedRate == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalInterest = requestedAmount.multiply(proposedRate)
                                                   .multiply(BigDecimal.valueOf(termMonths));
        BigDecimal totalAmount = requestedAmount.add(totalInterest);
        return totalAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
    }
    
    public boolean isPending() {
        return ApplicationStatus.PENDING.equals(status);
    }
    
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }
    
    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}
```

### 🎯 Beneficios Observados

- ✅ **Fácil de entender**: Cada clase tiene un propósito claro
- ✅ **Fácil de testear**: Tests unitarios enfocados en una sola responsabilidad
- ✅ **Bajo acoplamiento**: Cambios en una responsabilidad no afectan otras
- ✅ **Alta cohesión**: Métodos relacionados agrupados en la misma clase

---

## 2. Open/Closed Principle (OCP)

> Las entidades de software deben estar abiertas para extensión pero cerradas para modificación.

### ✅ Implementación en el Proyecto

#### 2.1 Uso de Interfaces (Ports)

El sistema define interfaces que permiten agregar nuevas implementaciones sin modificar el código existente:

**Puerto de entrada definido:**

```java
public interface CreateCreditApplicationUseCase {
    
    record CreateCreditApplicationCommand(
        Long memberId,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal proposedRate
    ) {}
    
    CreditApplication execute(CreateCreditApplicationCommand command);
}
```

**Implementación actual:**

```java
@Service
public class CreateCreditApplicationUseCaseImpl implements CreateCreditApplicationUseCase {
    @Override
    public CreditApplication execute(CreateCreditApplicationCommand command) {
        // Implementación actual
    }
}
```

**Extensión sin modificación**: Si necesitamos una implementación diferente (ej: para un tipo especial de crédito), podemos crear `CreateSpecialCreditApplicationUseCaseImpl` sin modificar la interface ni la implementación existente.

#### 2.2 Estrategia de Repositorios

**Puerto definido (cerrado para modificación):**

```java
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication creditApplication);
    Optional<CreditApplication> findById(Long id);
    List<CreditApplication> findAll();
    List<CreditApplication> findByMemberId(Long memberId);
    List<CreditApplication> findByStatus(ApplicationStatus status);
    void deleteById(Long id);
}
```

**Implementación JPA (abierta para extensión):**

```java
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    // Implementación usando JPA
}
```

**Posible extensión**: Podríamos agregar `CreditApplicationMongoRepositoryAdapter` o `CreditApplicationRedisRepositoryAdapter` sin modificar el puerto ni la implementación JPA existente.

#### 2.3 Mappers Configurables

**Uso de MapStruct permite extensión:**

```java
@Component
public class CreditApplicationMapper {
    
    public CreditApplication toDomain(CreditApplicationEntity entity) {
        // Mapeo actual
    }
    
    public CreditApplicationEntity toEntity(CreditApplication domain) {
        // Mapeo actual
    }
}
```

**Extensión**: Si necesitamos un mapeo diferente (ej: con auditoría adicional), podemos crear un nuevo mapper sin modificar el existente:

```java
@Component
public class AuditedCreditApplicationMapper extends CreditApplicationMapper {
    // Agrega comportamiento de auditoría
}
```

#### 2.4 Circuit Breaker y Retry como Extensiones

```java
@CircuitBreaker(name = "riskCentral", fallbackMethod = "fallbackEvaluateRisk")
@Retry(name = "riskCentral")
public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
    // Lógica principal
}

// Extensión mediante fallback sin modificar el método principal
private RiskCentralResponse fallbackEvaluateRisk(RiskCentralRequest request, Throwable t) {
    log.warn("Risk Central service unavailable, using fallback");
    return new RiskCentralResponse(600, RiskLevel.MEDIUM, "Service unavailable");
}
```

#### 2.5 Configuración Mediante Perfiles Spring

El sistema permite diferentes comportamientos sin modificar código:

```yaml
# application-dev.yml (abierto para extensión)
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/coopcredit
    
# application-prod.yml (extensión para producción)
spring:
  datasource:
    url: ${DATABASE_URL}
```

### 🎯 Beneficios Observados

- ✅ **Nuevas funcionalidades**: Se agregan mediante nuevas implementaciones
- ✅ **Código estable**: La lógica existente no se modifica
- ✅ **Menor riesgo**: Cambios no afectan código probado
- ✅ **Flexibilidad**: Fácil cambiar entre implementaciones

---

## 3. Liskov Substitution Principle (LSP)

> Los objetos de una superclase deben ser reemplazables por objetos de sus subclases sin romper la aplicación.

### ✅ Implementación en el Proyecto

#### 3.1 Implementaciones Intercambiables de Use Cases

Todas las implementaciones de use cases pueden sustituirse por la interfaz:

```java
// Interface (contrato)
public interface EvaluateCreditApplicationUseCase {
    RiskEvaluation execute(Long creditApplicationId);
}

// Implementación actual
@Service
public class EvaluateCreditApplicationUseCaseImpl implements EvaluateCreditApplicationUseCase {
    @Override
    public RiskEvaluation execute(Long creditApplicationId) {
        // Implementación completa con llamada a Risk Central
    }
}

// Posible implementación alternativa (ej: para testing)
public class MockEvaluateCreditApplicationUseCase implements EvaluateCreditApplicationUseCase {
    @Override
    public RiskEvaluation execute(Long creditApplicationId) {
        // Mock implementation para tests
        return RiskEvaluation.builder()
                .finalDecision(FinalDecision.APPROVED)
                .build();
    }
}
```

**El controlador no necesita saber cuál implementación usa:**

```java
@RestController
public class CreditApplicationController {
    
    // Dependencia en la abstracción, no en la implementación
    private final EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase;
    
    public CreditApplicationController(
            EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase) {
        this.evaluateCreditApplicationUseCase = evaluateCreditApplicationUseCase;
    }
    
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<EvaluationResponse> evaluate(@PathVariable Long id) {
        // Funciona con cualquier implementación de la interface
        RiskEvaluation evaluation = evaluateCreditApplicationUseCase.execute(id);
        return ResponseEntity.ok(toEvaluationResponse(evaluation));
    }
}
```

#### 3.2 Adaptadores de Repositorio Sustituibles

```java
// Puerto de salida
public interface MemberRepositoryPort {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByDocument(String document);
    boolean existsByDocument(String document);
}

// Implementación JPA (producción)
@Component
public class MemberRepositoryAdapter implements MemberRepositoryPort {
    private final JpaMemberRepository jpaRepository;
    private final MemberMapper mapper;
    
    @Override
    public Member save(Member member) {
        var entity = mapper.toEntity(member);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    // ... otros métodos
}

// Implementación en memoria (testing)
public class InMemoryMemberRepository implements MemberRepositoryPort {
    private final Map<Long, Member> storage = new HashMap<>();
    
    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            member.setId(nextId++);
        }
        storage.put(member.getId(), member);
        return member;
    }
    // ... otros métodos
}
```

**Los use cases funcionan con cualquier implementación:**

```java
@Service
public class CreateMemberUseCaseImpl implements CreateMemberUseCase {
    
    // Acepta CUALQUIER implementación de MemberRepositoryPort
    private final MemberRepositoryPort memberRepository;
    
    public CreateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(CreateMemberCommand command) {
        // Funciona igual con JPA o InMemory repository
        if (memberRepository.existsByDocument(command.document())) {
            throw new ConflictException("Member", "document", command.document());
        }
        
        Member member = Member.builder()
                .document(command.document())
                .name(command.name())
                .salary(command.salary())
                .build();
        
        return memberRepository.save(member);
    }
}
```

#### 3.3 Respeto de Contratos

Todas las implementaciones respetan el contrato definido en la interfaz:

**Contrato del port:**
```java
public interface RiskCentralPort {
    record RiskCentralRequest(String document, BigDecimal requestedAmount) {}
    record RiskCentralResponse(Integer score, RiskLevel riskLevel, String detail) {}
    
    RiskCentralResponse evaluateRisk(RiskCentralRequest request);
}
```

**Implementación HTTP (producción):**
```java
@Component
public class RiskCentralHttpAdapter implements RiskCentralPort {
    @Override
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        // Llamada HTTP real
        // Retorna RiskCentralResponse válido
    }
}
```

**Implementación Mock (testing):**
```java
public class MockRiskCentralAdapter implements RiskCentralPort {
    @Override
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        // Mock response
        return new RiskCentralResponse(700, RiskLevel.MEDIUM, "Mock evaluation");
    }
}
```

Ambas pueden usarse indistintamente sin romper el sistema.

### 🎯 Beneficios Observados

- ✅ **Polimorfismo real**: Implementaciones intercambiables
- ✅ **Testing simplificado**: Mocks y stubs fáciles de crear
- ✅ **Contratos claros**: Interfaces definen comportamiento esperado
- ✅ **Sin sorpresas**: Sustituciones no rompen la aplicación

---

## 4. Interface Segregation Principle (ISP)

> Los clientes no deberían verse forzados a depender de interfaces que no usan.

### ✅ Implementación en el Proyecto

#### 4.1 Puertos de Entrada Específicos por Caso de Uso

En lugar de una interfaz gigante `CreditService` con todos los métodos, el sistema define interfaces pequeñas y específicas:

**❌ Mal diseño (NO usado en el proyecto):**
```java
// Interface "gorda" que viola ISP
public interface CreditService {
    User registerUser(RegisterCommand command);
    User authenticateUser(LoginCommand command);
    Member createMember(CreateMemberCommand command);
    Member getMember(Long id);
    Member updateMember(Long id, UpdateCommand command);
    CreditApplication createApplication(CreateApplicationCommand command);
    CreditApplication getApplication(Long id);
    List<CreditApplication> listApplications();
    RiskEvaluation evaluateApplication(Long id);
}
```

**✅ Buen diseño (usado en el proyecto):**

```java
// Interfaces segregadas, cada una con responsabilidad única

// Solo para registro
public interface RegisterUserUseCase {
    record RegisterUserCommand(String username, String password, UserRole role) {}
    User execute(RegisterUserCommand command);
}

// Solo para autenticación
public interface AuthenticateUserUseCase {
    record AuthenticateCommand(String username, String password) {}
    String execute(AuthenticateCommand command);
}

// Solo para crear afiliados
public interface CreateMemberUseCase {
    record CreateMemberCommand(
        String document,
        String name,
        BigDecimal salary,
        LocalDate affiliationDate
    ) {}
    Member execute(CreateMemberCommand command);
}

// Solo para obtener afiliado
public interface GetMemberUseCase {
    Member execute(Long id);
    Member executeByDocument(String document);
}

// Solo para actualizar afiliado
public interface UpdateMemberUseCase {
    record UpdateMemberCommand(String name, BigDecimal salary, MemberStatus status) {}
    Member execute(Long id, UpdateMemberCommand command);
}

// Solo para crear solicitud
public interface CreateCreditApplicationUseCase {
    record CreateCreditApplicationCommand(
        Long memberId,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal proposedRate
    ) {}
    CreditApplication execute(CreateCreditApplicationCommand command);
}

// Solo para evaluar solicitud
public interface EvaluateCreditApplicationUseCase {
    RiskEvaluation execute(Long creditApplicationId);
}
```

**Ventaja**: Cada controlador o servicio solo depende de los casos de uso que realmente necesita.

#### 4.2 Controladores con Dependencias Específicas

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // Solo depende de los use cases de autenticación
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    
    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }
    
    // No tiene dependencias innecesarias de Member o CreditApplication use cases
}
```

```java
@RestController
@RequestMapping("/api/members")
public class MemberController {
    
    // Solo depende de los use cases de Members
    private final CreateMemberUseCase createMemberUseCase;
    private final GetMemberUserCase getMemberUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    
    // No tiene dependencias de Auth o CreditApplication use cases
}
```

```java
@RestController
@RequestMapping("/api/credit-applications")
public class CreditApplicationController {
    
    // Solo depende de los use cases de Credit Applications
    private final CreateCreditApplicationUseCase createCreditApplicationUseCase;
    private final GetCreditApplicationUseCase getCreditApplicationUseCase;
    private final ListCreditApplicationsUseCase listCreditApplicationsUseCase;
    private final EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase;
    
    // No tiene dependencias de Auth o Member use cases
}
```

#### 4.3 Puertos de Salida Segregados

```java
// Interface segregada para acceso a usuarios
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

// Interface segregada para acceso a afiliados
public interface MemberRepositoryPort {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByDocument(String document);
    boolean existsByDocument(String document);
}

// Interface segregada para acceso a solicitudes
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication creditApplication);
    Optional<CreditApplication> findById(Long id);
    List<CreditApplication> findAll();
    List<CreditApplication> findByMemberId(Long memberId);
    List<CreditApplication> findByStatus(ApplicationStatus status);
    void deleteById(Long id);
}

// Interface segregada para acceso a evaluaciones
public interface RiskEvaluationRepositoryPort {
    RiskEvaluation save(RiskEvaluation riskEvaluation);
    Optional<RiskEvaluation> findById(Long id);
    Optional<RiskEvaluation> findByCreditApplicationId(Long creditApplicationId);
    boolean existsByCreditApplicationId(Long creditApplicationId);
}

// Interface segregada para servicio externo
public interface RiskCentralPort {
    record RiskCentralRequest(String document, BigDecimal requestedAmount) {}
    record RiskCentralResponse(Integer score, RiskLevel riskLevel, String detail) {}
    
    RiskCentralResponse evaluateRisk(RiskCentralRequest request);
}
```

**Ventaja**: Los use cases solo dependen de los repositorios que necesitan:

```java
@Service
public class CreateMemberUseCaseImpl implements CreateMemberUseCase {
    
    // Solo depende de MemberRepositoryPort, no de otros repositorios
    private final MemberRepositoryPort memberRepository;
    
    public CreateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

```java
@Service
public class EvaluateCreditApplicationUseCaseImpl implements EvaluateCreditApplicationUseCase {
    
    // Solo depende de los repositorios que realmente usa
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    private final MemberRepositoryPort memberRepository;
    private final RiskEvaluationRepositoryPort riskEvaluationRepository;
    private final RiskCentralPort riskCentralPort;
    
    // No tiene dependencias innecesarias de UserRepository
}
```

### 🎯 Beneficios Observados

- ✅ **Acoplamiento mínimo**: Clases solo dependen de lo que necesitan
- ✅ **Fácil de testear**: Mocks solo para dependencias reales
- ✅ **Código limpio**: Menos imports y dependencias innecesarias
- ✅ **Cambios localizados**: Modificar una interface afecta solo a quienes la usan

---

## 5. Dependency Inversion Principle (DIP)

> Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones.

### ✅ Implementación en el Proyecto

Este principio es el **core** de la Arquitectura Hexagonal implementada en el proyecto.

#### 5.1 Arquitectura de Dependencias

```
┌─────────────────────────────────────────┐
│    Infrastructure Layer (Detalles)     │
│  - Controllers                          │
│  - JPA Adapters                         │
│  - HTTP Adapters                        │
│  - Security Config                      │
└──────────────┬──────────────────────────┘
               │ DEPENDE DE ↓
               │ (implementa ports)
┌──────────────┴──────────────────────────┐
│    Application Layer (Use Cases)        │
│  - CreateMemberUseCaseImpl              │
│  - EvaluateCreditApplicationUseCaseImpl │
└──────────────┬──────────────────────────┘
               │ DEPENDE DE ↓
               │ (usa abstracciones)
┌──────────────┴──────────────────────────┐
│    Domain Layer (Core Business)         │
│  - Models                                │
│  - Business Logic                       │
│  - Ports (Interfaces)                   │
└─────────────────────────────────────────┘
```

**Flujo de dependencias**: SIEMPRE hacia el dominio (nunca hacia afuera).

#### 5.2 Use Cases Dependen de Abstracciones

**❌ Violación de DIP (NO usado):**
```java
@Service
public class CreateMemberUseCaseImpl {
    
    // Dependencia directa en implementación concreta (malo)
    private final JpaMemberRepository jpaRepository;
    private final MemberMapper mapper;
    
    public Member execute(CreateMemberCommand command) {
        // Acoplado a JPA
        MemberEntity entity = mapper.toEntity(command);
        MemberEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**✅ Aplicación correcta de DIP (usado en el proyecto):**
```java
@Service
public class CreateMemberUseCaseImpl implements CreateMemberUseCase {
    
    // Dependencia en ABSTRACCIÓN (puerto), no en implementación
    private final MemberRepositoryPort memberRepository;
    
    public CreateMemberUseCaseImpl(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member execute(CreateMemberCommand command) {
        // Trabaja con abstracción, no sabe si es JPA, MongoDB, InMemory, etc.
        if (memberRepository.existsByDocument(command.document())) {
            throw new ConflictException("Member", "document", command.document());
        }
        
        Member member = Member.builder()
                .document(command.document())
                .name(command.name())
                .salary(command.salary())
                .build();
        
        return memberRepository.save(member);
    }
}
```

#### 5.3 Adaptadores Implementan Puertos del Dominio

**Puerto definido en el dominio (abstracción de alto nivel):**
```java
// domain/ports/out/CreditApplicationRepositoryPort.java
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication creditApplication);
    Optional<CreditApplication> findById(Long id);
    List<CreditApplication> findAll();
    // ... otros métodos
}
```

**Adaptador en infraestructura implementa la abstracción:**
```java
// infrastructure/adapters/persistence/CreditApplicationRepositoryAdapter.java
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    
    // Detalles de bajo nivel (JPA)
    private final JpaCreditApplicationRepository jpaRepository;
    private final CreditApplicationMapper mapper;
    
    public CreditApplicationRepositoryAdapter(
            JpaCreditApplicationRepository jpaRepository,
            CreditApplicationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public CreditApplication save(CreditApplication creditApplication) {
        // Implementación específica de JPA
        var entity = mapper.toEntity(creditApplication);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<CreditApplication> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    // ... otros métodos
}
```

**Resultado**: El dominio (alto nivel) no depende de JPA (bajo nivel). JPA depende del dominio.

#### 5.4 Caso Real: Evaluación de Crédito

**Puerto de servicio externo definido en el dominio:**
```java
// domain/ports/out/RiskCentralPort.java
public interface RiskCentralPort {
    record RiskCentralRequest(String document, BigDecimal requestedAmount) {}
    record RiskCentralResponse(Integer score, RiskLevel riskLevel, String detail) {}
    
    RiskCentralResponse evaluateRisk(RiskCentralRequest request);
}
```

**Use case de alto nivel depende solo de la abstracción:**
```java
@Service
public class EvaluateCreditApplicationUseCaseImpl implements EvaluateCreditApplicationUseCase {
    
    private final CreditApplicationRepositoryPort creditApplicationRepository;
    private final MemberRepositoryPort memberRepository;
    private final RiskEvaluationRepositoryPort riskEvaluationRepository;
    private final RiskCentralPort riskCentralPort; // ABSTRACCIÓN
    
    @Override
    public RiskEvaluation execute(Long creditApplicationId) {
        // ...
        
        // Llamada a abstracción, no sabe si es HTTP, gRPC, Mock, etc.
        RiskCentralPort.RiskCentralResponse riskResponse = 
            riskCentralPort.evaluateRisk(
                new RiskCentralPort.RiskCentralRequest(
                    member.getDocument(), 
                    application.getRequestedAmount()));
        
        // ...
    }
}
```

**Adaptador HTTP implementa la abstracción (detalle de bajo nivel):**
```java
// infrastructure/adapters/http/RiskCentralHttpAdapter.java
@Component
public class RiskCentralHttpAdapter implements RiskCentralPort {
    
    private final WebClient webClient;
    
    @Override
    @CircuitBreaker(name = "riskCentral", fallbackMethod = "fallbackEvaluateRisk")
    @Retry(name = "riskCentral")
    public RiskCentralResponse evaluateRisk(RiskCentralRequest request) {
        log.info("Calling Risk Central service for document: {}", request.document());
        
        // Detalles de implementación HTTP
        RiskCentralApiResponse response = webClient.post()
                .uri("/api/risk/evaluate")
                .bodyValue(new RiskCentralApiRequest(
                    request.document(), 
                    request.requestedAmount()))
                .retrieve()
                .bodyToMono(RiskCentralApiResponse.class)
                .block();
        
        return new RiskCentralResponse(
                response.score(),
                mapRiskLevel(response.riskLevel()),
                response.detail()
        );
    }
    
    // Fallback también respeta el contrato
    private RiskCentralResponse fallbackEvaluateRisk(
            RiskCentralRequest request, Throwable t) {
        log.warn("Risk Central service unavailable, using fallback");
        return new RiskCentralResponse(600, RiskLevel.MEDIUM, "Service unavailable");
    }
}
```

#### 5.5 Inyección de Dependencias por Constructor

Todas las dependencias se inyectan mediante constructor (nunca field injection):

```java
@RestController
public class CreditApplicationController {
    
    // Dependencias como abstracciones
    private final CreateCreditApplicationUseCase createCreditApplicationUseCase;
    private final GetCreditApplicationUseCase getCreditApplicationUseCase;
    private final ListCreditApplicationsUseCase listCreditApplicationsUseCase;
    private final EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase;
    
    // Inyección por constructor (mejor práctica)
    public CreditApplicationController(
            CreateCreditApplicationUseCase createCreditApplicationUseCase,
            GetCreditApplicationUseCase getCreditApplicationUseCase,
            ListCreditApplicationsUseCase listCreditApplicationsUseCase,
            EvaluateCreditApplicationUseCase evaluateCreditApplicationUseCase) {
        this.createCreditApplicationUseCase = createCreditApplicationUseCase;
        this.getCreditApplicationUseCase = getCreditApplicationUseCase;
        this.listCreditApplicationsUseCase = listCreditApplicationsUseCase;
        this.evaluateCreditApplicationUseCase = evaluateCreditApplicationUseCase;
    }
    
    // Controlador trabaja con abstracciones, no con implementaciones concretas
}
```

**Beneficios**:
- ✅ Dependencias explícitas y obligatorias
- ✅ Fácil de testear (inyectar mocks)
- ✅ Inmutabilidad (campos final)

#### 5.6 Configuración de Beans (Wiring)

Spring Boot realiza el wiring automático, pero si fuera necesario configuración explícita:

```java
@Configuration
public class BeanConfig {
    
    // NO hay configure explícito porque Spring Boot auto-detecta
    // @Service y @Component, pero el concepto es:
    
    // Las abstracciones (interfaces) están en domain/ports
    // Las implementaciones están en application y infrastructure
    // Spring inyecta las implementaciones donde se requieren las abstracciones
}
```

### 🎯 Beneficios Observados

- ✅ **Dominio independiente**: Core business no depende de frameworks
- ✅ **Testabilidad total**: Use cases testeables sin infraestructura
- ✅ **Flexibilidad**: Cambiar implementaciones sin afectar lógica de negocio
- ✅ **Arquitectura limpia**: Separación clara de responsabilidades

---

## 📊 Resumen de Aplicación de SOLID

| Principio | Nivel de Aplicación | Evidencias |
|-----------|-------------------|------------|
| **SRP** | ⭐⭐⭐⭐⭐ (Excelente) | Use cases con responsabilidad única, controladores específicos, adaptadores enfocados |
| **OCP** | ⭐⭐⭐⭐☆ (Muy Bueno) | Uso de interfaces, estrategias de mapeo, configuración por perfiles |
| **LSP** | ⭐⭐⭐⭐⭐ (Excelente) | Implementaciones intercambiables, respeto estricto de contratos |
| **ISP** | ⭐⭐⭐⭐⭐ (Excelente) | Puertos segregados por caso de uso, dependencias mínimas |
| **DIP** | ⭐⭐⭐⭐⭐ (Excelente) | Arquitectura hexagonal, dependencias siempre en abstracciones |

---

## 🎯 Conclusiones

El proyecto **CoopCredit System** es un ejemplo ejemplar de aplicación de los principios SOLID:

1. **SRP**: Cada clase tiene una única responsabilidad claramente definida
2. **OCP**: El sistema es extensible sin necesidad de modificar código existente
3. **LSP**: Las implementaciones son totalmente intercambiables
4. **ISP**: Las interfaces son pequeñas y específicas
5. **DIP**: La arquitectura hexagonal invierte las dependencias correctamente

Estos principios, combinados con la arquitectura hexagonal, resultan en un código:

- ✅ **Mantenible**: Cambios localizados y predecibles
- ✅ **Testeable**: Testing unitario, de integración y end-to-end simplificado
- ✅ **Escalable**: Fácil agregar nuevas funcionalidades
- ✅ **Flexible**: Cambio de tecnologías sin afectar el core
- ✅ **Profesional**: Siguiendo las mejores prácticas de la industria

El proyecto demuestra que SOLID + Hexagonal Architecture = Software de Alta Calidad.
\newpage\part{Parte III: Decisiones de Diseño}
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
\newpage\part{Parte IV: Manual de Usuario}
# CoopCredit System - Manual de Usuario

## 📖 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Requisitos del Sistema](#requisitos-del-sistema)
3. [Instalación y Configuración](#instalación-y-configuración)
4. [Inicio del Sistema](#inicio-del-sistema)
5. [Acceso a los Servicios](#acceso-a-los-servicios)
6. [Guía de Uso Paso a Paso](#guía-de-uso-paso-a-paso)
7. [Uso de Postman](#uso-de-postman)
8. [Roles y Permisos](#roles-y-permisos)
9. [Monitoreo y Observabilidad](#monitoreo-y-observabilidad)
10. [Solución de Problemas](#solución-de-problemas)

---

## 1. Introducción

### ¿Qué es CoopCredit System?

CoopCredit System es una plataforma integral para la gestión de solicitudes de crédito en cooperativas. El sistema permite:

- **Gestionar afiliados**: Registro y actualización de miembros
- **Procesar solicitudes de crédito**: Crear y consultar aplicaciones
- **Evaluar riesgos**: Evaluación automática mediante integración con centrales de riesgo
- **Monitorear operaciones**: Dashboards y métricas en tiempo real

### Arquitectura del Sistema

El sistema está compuesto por:

- **Credit Application Service** (Puerto 8080): Servicio principal
- **Risk Central Mock Service** (Puerto 8081): Simulador de central de riesgo
- **MySQL** (Puerto 3307): Base de datos
- **Prometheus** (Puerto 9091): Recolector de métricas
- **Grafana** (Puerto 3000): Dashboards visuales

---

## 2. Requisitos del Sistema

### Hardware Mínimo

- **CPU**: 2 cores
- **RAM**: 4 GB
- **Disco**: 10 GB libres

### Software Necesario

| Software | Versión Mínima | Propósito |
|----------|---------------|-----------|
| **Java JDK** | 21 | Ejecutar aplicaciones Spring Boot |
| **Maven** | 3.8+ | Gestión de dependencias y build |
| **Docker** | 20.10+ | Contenedores de infraestructura |
| **Docker Compose** | 2.0+ | Orquestación de contenedores |
| **Git** | 2.30+ | Control de versiones (opcional) |

### Instalación de Prerequisitos

#### Linux/Mac

```bash
# Verificar Java 21
java -version

# Verificar Maven
mvn -version

# Verificar Docker
docker --version
docker-compose --version
```

#### Si falta algún prerequisito:

**Java 21 (Ubuntu/Debian)**:
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Maven**:
```bash
sudo apt install maven
```

**Docker**:
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

---

## 3. Instalación y Configuración

### Paso 1: Clonar el Repositorio

```bash
cd ~/proyectos
git clone <repository-url>
cd coopcredit-system
```

### Paso 2: Configurar Variables de Entorno (Opcional)

Para producción, crear archivo `.env`:

```bash
# .env
DATABASE_URL=jdbc:mysql://localhost:3307/coopcredit
DATABASE_USERNAME=root
DATABASE_PASSWORD=root

JWT_SECRET=tu-secreto-super-seguro-minimo-256-bits
JWT_EXPIRATION=86400000

RISK_CENTRAL_URL=http://localhost:8081
```

### Paso 3: Permisos de Ejecución para Scripts

```bash
chmod +x start.sh
chmod +x stop.sh
```

### Paso 4: Verificar Puertos Disponibles

Los siguientes puertos deben estar libres:

- **8080**: Credit Application Service
- **8081**: Risk Central Mock Service
- **3307**: MySQL
- **3000**: Grafana
- **9091**: Prometheus

Verificar:

```bash
# Linux/Mac
lsof -i :8080
lsof -i :8081
lsof -i :3307
lsof -i :3000
lsof -i :9091
```

Si algún puerto está ocupado, libérelo o modifique la configuración.

---

## 4. Inicio del Sistema

### Opción 1: Script Automatizado (Recomendado)

```bash
./start.sh
```

Este script:
1. ✅ Libera puertos ocupados
2. ✅ Inicia contenedores Docker (MySQL, Prometheus, Grafana)
3. ✅ Espera que MySQL esté listo
4. ✅ Inicia Risk Central Mock Service en background
5. ✅ Inicia Credit Application Service en foreground

**Salida Esperada**:

```
╔════════════════════════════════════════════════════════════════╗
║           🚀 STARTING COOPCREDIT SYSTEM                        ║
╚════════════════════════════════════════════════════════════════╝

🔄 Releasing ports...
🐳 Starting Docker containers...
⏳ Waiting for MySQL (5 seconds)...
🔧 Starting Risk Central Mock Service...
💳 Starting Credit Application Service...

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.0)
...
Started CreditApplicationServiceApplication in 8.456 seconds
```

### Opción 2: Manual (Solo para Testing)

```bash
# Terminal 1: Iniciar MySQL
docker run -d --name coopcredit-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=coopcredit \
  -p 3307:3307 \
  mysql:8.0 --port=3307

# Terminal 2: Risk Central Service
cd risk-central-mock-service
mvn spring-boot:run

# Terminal 3: Credit Application Service
cd credit-application-service
mvn spring-boot:run
```

### Detener el Sistema

```bash
./stop.sh
```

O manualmente: `Ctrl+C` en la terminal del servicio

---

## 5. Acceso a los Servicios

### URLs Principales

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **API Principal** | http://localhost:8080 | JWT Token required |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Sin auth en "/swagger-ui/**" |
| **Risk Central** | http://localhost:8081 | Sin auth |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9091 | Sin auth |
| **Health Check** | http://localhost:8080/actuator/health | Sin auth |
| **Metrics** | http://localhost:8080/actuator/prometheus | Sin auth |

### Verificar que Todo Funciona

```bash
# Credit Application Service
curl http://localhost:8080/actuator/health
# Esperado: {"status":"UP"}

# Risk Central Service
curl http://localhost:8081/actuator/health
# Esperado: {"status":"UP"}

# MySQL
docker ps | grep coopcredit-mysql
# Esperado: Container running
```

---

## 6. Guía de Uso Paso a Paso

### 6.1 Autenticación

#### Registrar Usuario Administrador

**Endpoint**: `POST /api/auth/register`

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "role": "ADMIN"
  }'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiI...",
  "username": "admin",
  "role": "ADMIN"
}
```

**✅ Guardar el Token**: Necesario para todas las operaciones posteriores.

#### Login

**Endpoint**: `POST /api/auth/login`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response**: Mismo formato que registro, retorna un JWT token.

---

### 6.2 Gestión de Afiliados

#### Crear Afiliado

**Endpoint**: `POST /api/members`

**Autenticación**: Requiere token JWT

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."  # Token obtenido anteriormente

curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "document": "1234567890",
    "name": "Juan Pérez",
    "salary": 5000000,
    "affiliationDate": "2024-01-15"
  }'
```

**Response**:
```json
{
  "id": 1,
  "document": "1234567890",
  "name": "Juan Pérez",
  "salary": 5000000,
  "affiliationDate": "2024-01-15",
  "status": "ACTIVE"
}
```

#### Consultar Afiliado por ID

**Endpoint**: `GET /api/members/{id}`

```bash
curl -X GET http://localhost:8080/api/members/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "id": 1,
  "document": "1234567890",
  "name": "Juan Pérez",
  "salary": 5000000,
  "affiliationDate": "2024-01-15",
  "status": "ACTIVE"
}
```

#### Consultar por Documento

**Endpoint**: `GET /api/members/document/{document}`

```bash
curl -X GET http://localhost:8080/api/members/document/1234567890 \
  -H "Authorization: Bearer $TOKEN"
```

#### Actualizar Afiliado

**Endpoint**: `PUT /api/members/{id}`

```bash
curl -X PUT http://localhost:8080/api/members/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Juan Pérez García",
    "salary": 6000000,
    "status": "ACTIVE"
  }'
```

**Response**:
```json
{
  "id": 1,
  "document": "1234567890",
  "name": "Juan Pérez García",
  "salary": 6000000,
  "affiliationDate": "2024-01-15",
  "status": "ACTIVE"
}
```

---

### 6.3 Solicitudes de Crédito

#### Crear Solicitud

**Endpoint**: `POST /api/credit-applications`

**Prerrequisitos**:
- Afiliado debe estar registrado
- Afiliado debe estar ACTIVO
- Afiliado debe tener mínimo 6 meses de antigüedad

```bash
curl -X POST http://localhost:8080/api/credit-applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "memberId": 1,
    "requestedAmount": 10000000,
    "termMonths": 36,
    "proposedRate": 0.12
  }'
```

**Response**:
```json
{
  "id": 1,
  "memberId": 1,
  "requestedAmount": 10000000,
  "termMonths": 36,
  "proposedRate": 0.12,
  "applicationDate": "2024-12-10",
  "status": "PENDING"
}
```

#### Consultar Solicitud

**Endpoint**: `GET /api/credit-applications/{id}`

```bash
curl -X GET http://localhost:8080/api/credit-applications/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Listar Todas las Solicitudes

**Endpoint**: `GET /api/credit-applications`

```bash
# Todas las solicitudes
curl -X GET http://localhost:8080/api/credit-applications \
  -H "Authorization: Bearer $TOKEN"

# Filtrar por afiliado
curl -X GET "http://localhost:8080/api/credit-applications?memberId=1" \
  -H "Authorization: Bearer $TOKEN"

# Filtrar por estado
curl -X GET "http://localhost:8080/api/credit-applications?status=PENDING" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 6.4 Evaluación de Riesgo

#### Evaluar Solicitud

**Endpoint**: `POST /api/credit-applications/{id}/evaluate`

**Autorización**: Solo roles ANALYST y ADMIN

```bash
curl -X POST http://localhost:8080/api/credit-applications/1/evaluate \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "id": 1,
  "creditApplicationId": 1,
  "score": 720,
  "riskLevel": "MEDIUM",
  "paymentToIncomeRatio": 0.35,
  "meetsSeniority": true,
  "meetsMaxAmount": true,
  "finalDecision": "APPROVED",
  "reason": "All criteria met"
}
```

**Proceso de Evaluación**:

1. ✅ Consulta Risk Central Service (score crediticio)
2. ✅ Calcula ratio cuota/ingreso
3. ✅ Verifica antigüedad (≥6 meses)
4. ✅ Verifica monto máximo (≤4x salario)
5. ✅ Determina decisión final: APPROVED / REJECTED

**Criterios de Aprobación**:

- ✓ Antigüedad ≥ 6 meses
- ✓ Monto solicitado ≤ 4x salario
- ✓ Ratio cuota/ingreso ≤ 40%
- ✓ Riesgo NO sea HIGH

---

### 6.5 Servicios de Observabilidad

#### Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Application Info

```bash
curl http://localhost:8080/actuator/info
```

#### Métricas Prometheus

```bash
curl http://localhost:8080/actuator/prometheus
```

**Métricas disponibles**:
- `http_server_requests_seconds`: Latencia de requests
- `jvm_memory_used_bytes`: Uso de memoria
- `jvm_threads_live`: Threads activos
- `resilience4j_circuitbreaker_state`: Estado circuit breaker

---

## 7. Uso de Postman

### Importar Colección

1. **Abrir Postman**
2. **Import** → **Upload Files**
3. Seleccionar: `postman/CoopCredit_API_Collection.json`
4. **Import**

### Configurar Variables

**Environment Variables**:

| Variable | Valor |
|----------|-------|
| `baseUrl` | `http://localhost:8080` |
| `riskUrl` | `http://localhost:8081` |
| `token` | (se auto-completa al hacer login) |

### Flujo Completo de Testing

#### 1. Authentication

**1.1 Register User (ADMIN)**:
- Ejecutar request
- Copiar token de la response

**1.2 Login (Get JWT Token)**:
- Ejecutar request
- El token se guarda automáticamente en `{{token}}`

#### 2. Members (Afiliados)

**2.1 Create Member**:
- Token se usa automáticamente de `{{token}}`
- Guarda el `id` del response para siguiente paso

**2.2 Get Member by ID**:
- Cambiar el ID en la URL si es necesario

**2.3 Update Member**:
- Actualiza información del afiliado

#### 3. Credit Applications

**3.1 Create Credit Application**:
- Asegúrate que `memberId` existe
- Guarda el `id` de la solicitud

**3.2 Get Credit Application by ID**:
- Consulta la solicitud creada

**3.3 Evaluate Credit Application**:
- Evalúa la solicitud
- Verifica la decisión APPROVED/REJECTED

#### 4. Risk Central Mock Service

Prueba directa al servicio de riesgo:

**4.1 Evaluate Risk**:
- Consulta el score de un documento

#### 5. Actuator (Observability)

**5.1 Health Check**  
**5.2 App Info**  
**5.3 Prometheus Metrics**

#### 6. Validation Tests

Pruebas de validación:

**6.1 Create Member - Invalid (No Document)**:
- Debe fallar con 400 Bad Request

**6.2 Create Member - Invalid (Negative Salary)**:
- Debe fallar con 400 Bad Request

**6.3 Access Without Token**:
- Debe fallar con 401 Unauthorized

---

## 8. Roles y Permisos

### Roles Disponibles

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **ADMIN** | Administrador del sistema | Todos los endpoints |
| **ANALYST** | Analista de crédito | Crear afiliados, Evaluar solicitudes, Consultas |
| **MEMBER** | Afiliado | Crear solicitudes propias, Consultar propias |

### Matriz de Permisos

| Operación | ADMIN | ANALYST | MEMBER |
|-----------|-------|---------|--------|
| **Auth** |
| Register User | ✅ | ❌ | ❌ |
| Login | ✅ | ✅ | ✅ |
| **Members** |
| Create Member | ✅ | ✅ | ❌ |
| Get Member | ✅ | ✅ | ✅ (solo propio) |
| Update Member | ✅ | ✅ | ❌ |
| **Credit Applications** |
| Create Application | ✅ | ✅ | ✅ |
| Get Application | ✅ | ✅ | ✅ (solo propias) |
| List Applications | ✅ | ✅ | ✅ (solo propias) |
| **Evaluation** |
| Evaluate Application | ✅ | ✅ | ❌ |
| **Observability** |
| Health Check | ✅ (público) | ✅ (público) | ✅ (público) |
| Metrics | ✅ | ❌ | ❌ |

### Crear Usuarios con Diferentes Roles

```bash
# Crear ADMIN
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123", "role": "ADMIN"}'

# Crear ANALYST
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "analyst", "password": "analyst123", "role": "ANALYST"}'

# Crear MEMBER
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "member", "password": "member123", "role": "MEMBER"}'
```

---

## 9. Monitoreo y Observabilidad

### Grafana Dashboards

**URL**: http://localhost:3000  
**Credentials**: admin / admin

#### Primer Login

1. Acceder a http://localhost:3000
2. Usuario: `admin`, Password: `admin`
3. (Opcional) Cambiar password en primer login

#### Datasources Pre-configurados

- **Prometheus**: Configurado automáticamente apuntando a http://prometheus:9091

#### Dashboards Disponibles

Los dashboards están pre-provisionados en `grafana_provisioning/dashboards/`:

1. **Application Metrics**: Métricas de Spring Boot
2. **JVM Metrics**: Heap, GC, Threads
3. **HTTP Metrics**: Requests/sec, latency, error rates
4. **Business Metrics**: Credit applications, evaluations

### Prometheus

**URL**: http://localhost:9091

#### Queries Útiles

```promql
# Requests por segundo
rate(http_server_requests_seconds_count[1m])

# Latencia P95
histogram_quantile(0.95, http_server_requests_seconds_bucket)

# Uso de memoria heap
jvm_memory_used_bytes{area="heap"}

# Estado del circuit breaker
resilience4j_circuitbreaker_state{name="riskCentral"}
```

### Logs

Los logs de la aplicación se muestran en la consola:

```bash
# Ver logs del Credit Application Service
tail -f credit-application-service/logs/application.log

# Filtrar por nivel
grep "ERROR" credit-application-service/logs/application.log
```

**Niveles de log**:
- `INFO`: Operaciones normales
- `DEBUG`: Detalles de ejecución (SQL queries, security)
- `WARN`: Advertencias (Risk Central unavailable)
- `ERROR`: Errores que requieren atención

---

## 10. Solución de Problemas

### Problema: Puerto ya en uso

**Síntomas**:
```
Error starting ApplicationContext. Port 8080 was already in use.
```

**Solución**:
```bash
# Ver qué proceso usa el puerto
lsof -i :8080

# Matar el proceso
kill -9 <PID>

# O usar el script
./start.sh  # Automáticamente libera puertos
```

### Problema: MySQL no inicia

**Síntomas**:
```
Unable to obtain JDBC Connection
```

**Solución**:
```bash
# Verificar estado
docker ps -a | grep coopcredit-mysql

# Ver logs
docker logs coopcredit-mysql

# Reiniciar container
docker restart coopcredit-mysql

# O recrear
docker rm -f coopcredit-mysql
./start.sh
```

### Problema: Token JWT inválido

**Síntomas**:
```
401 Unauthorized
```

**Solución**:
1. Verificar que el token no haya expirado (24h)
2. Generar nuevo token haciendo login
3. Verificar header: `Authorization: Bearer {token}`

### Problema: Risk Central Service no responde

**Síntomas**:
```
WARN - Risk Central service unavailable, using fallback
```

**Solución**:

El sistema usa fallback automático (score 600, MEDIUM risk), pero para restaurar el servicio:

```bash
# Verificar si está corriendo
curl http://localhost:8081/actuator/health

# Si no responde, reiniciar
cd risk-central-mock-service
mvn spring-boot:run
```

### Problema: Migraciones Flyway fallan

**Síntomas**:
```
FlywayException: Validate failed
```

**Solución**:
```bash
# Opción 1: Limpiar BD y re-migrar
mysql -h localhost -P 3307 -u root -proot
DROP DATABASE coopcredit;
CREATE DATABASE coopcredit;
exit

# Reiniciar aplicación
./start.sh

# Opción 2: Baseline manual (si ya hay datos)
# Contactar administrador
```

### Problema: Grafana no muestra datos

**Síntomas**:
Dashboards vacíos

**Solución**:
1. Verificar Prometheus está scraping:
   - http://localhost:9091/targets
   - credit-application-service debe estar UP

2. Verificar datasource en Grafana:
   - Configuration → Data Sources → Prometheus
   - Test connection

3. Generar tráfico:
   ```bash
   # Ejecutar requests para generar métricas
   curl http://localhost:8080/actuator/health
   ```

### Problema: Permisos denegados

**Síntomas**:
```
403 Forbidden - Insufficient permissions
```

**Solución**:
- Verificar rol del usuario (JWT token tiene claim "role")
- Verificar endpoint requires ADMIN o ANALYST
- Si eres MEMBER, solo puedes evaluar con rol ANALYST/ADMIN

### Logs de Debugging

Aumentar nivel de logging:

```yaml
# application.yml
logging:
  level:
    com.coopcredit: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

---

## 11. Ejemplos de Integración

### Ejemplo: Flujo Completo de Solicitud

Script bash completo:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# 1. Login y obtener token
echo "=== Paso 1: Login ==="
TOKEN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.token')

echo "Token: $TOKEN"

# 2. Crear afiliado
echo "=== Paso 2: Crear Afiliado ==="
MEMBER=$(curl -s -X POST $BASE_URL/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "document": "1234567890",
    "name": "Juan Pérez",
    "salary": 5000000,
    "affiliationDate": "2024-01-15"
  }')

MEMBER_ID=$(echo $MEMBER | jq -r '.id')
echo "Afiliado creado: ID=$MEMBER_ID"

# 3. Crear solicitud de crédito
echo "=== Paso 3: Crear Solicitud ==="
APPLICATION=$(curl -s -X POST $BASE_URL/api/credit-applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"memberId\": $MEMBER_ID,
    \"requestedAmount\": 10000000,
    \"termMonths\": 36,
    \"proposedRate\": 0.12
  }")

APP_ID=$(echo $APPLICATION | jq -r '.id')
echo "Solicitud creada: ID=$APP_ID, Status=$(echo $APPLICATION | jq -r '.status')"

# 4. Evaluar solicitud
echo "=== Paso 4: Evaluar Solicitud ==="
EVALUATION=$(curl -s -X POST $BASE_URL/api/credit-applications/$APP_ID/evaluate \
  -H "Authorization: Bearer $TOKEN")

echo "Evaluación completada:"
echo "  Decision: $(echo $EVALUATION | jq -r '.finalDecision')"
echo "  Risk Level: $(echo $EVALUATION | jq -r '.riskLevel')"
echo "  Score: $(echo $EVALUATION | jq -r '.score')"
echo "  Reason: $(echo $EVALUATION | jq -r '.reason')"

# 5. Consultar solicitud actualizada
echo "=== Paso 5: Consultar Solicitud Actualizada ==="
UPDATED_APP=$(curl -s -X GET $BASE_URL/api/credit-applications/$APP_ID \
  -H "Authorization: Bearer $TOKEN")

echo "Estado final: $(echo $UPDATED_APP | jq -r '.status')"
```

### Ejemplo: Python Client

```python
import requests
import json

BASE_URL = "http://localhost:8080"

# Login
login_response = requests.post(
    f"{BASE_URL}/api/auth/login",
    json={"username": "admin", "password": "admin123"}
)
token = login_response.json()["token"]

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

# Crear afiliado
member_data = {
    "document": "1234567890",
    "name": "Juan Pérez",
    "salary": 5000000,
    "affiliationDate": "2024-01-15"
}
member_response = requests.post(
    f"{BASE_URL}/api/members",
    headers=headers,
    json=member_data
)
member_id = member_response.json()["id"]

# Crear solicitud
app_data = {
    "memberId": member_id,
    "requestedAmount": 10000000,
    "termMonths": 36,
    "proposedRate": 0.12
}
app_response = requests.post(
    f"{BASE_URL}/api/credit-applications",
    headers=headers,
    json=app_data
)
app_id = app_response.json()["id"]

# Evaluar
eval_response = requests.post(
    f"{BASE_URL}/api/credit-applications/{app_id}/evaluate",
    headers=headers
)
evaluation = eval_response.json()

print(f"Decision: {evaluation['finalDecision']}")
print(f"Risk: {evaluation['riskLevel']}")
```

---

## 12. Mejores Prácticas

### Seguridad

1. ✅ **Nunca compartir tokens JWT**
2. ✅ **Cambiar credenciales por defecto en producción**
3. ✅ **Usar HTTPS en producción**
4. ✅ **Rotar JWT secret regularmente**
5. ✅ **Implementar rate limiting**

### Performance

1. ✅ **Reutilizar conexiones HTTP**
2. ✅ **Implementar caching donde sea apropiado**
3. ✅ **Monitorear métricas de Prometheus**
4. ✅ **Usar BULKLOAD para datos iniciales**

### Operación

1. ✅ **Hacer backups regulares de MySQL**
2. ✅ **Monitorear dashboards de Grafana**
3. ✅ **Revisar logs de errores diariamente**
4. ✅ **Mantener documentación actualizada**

---

## 📞 Soporte

Para reportar problemas:

1. **Revisar logs**: `logs/application.log`
2. **Verificar health checks**: `/actuator/health`
3. **Consultar métricas**: Grafana dashboards
4. **Documentación Swagger**: http://localhost:8080/swagger-ui.html

---

## 🎉 ¡Listo para Usar!

El sistema CoopCredit está completamente configurado y listo para gestionar solicitudes de crédito. Sigue esta guía para operación exitosa.

**Happy Coding! 🚀**
\newpage\part{Parte V: Documentación Técnica}
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
