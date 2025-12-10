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
