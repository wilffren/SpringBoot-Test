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
