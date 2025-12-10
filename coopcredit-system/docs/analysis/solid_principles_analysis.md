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
