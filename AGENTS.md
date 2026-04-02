# AGENTS.md – Academia de Belleza System Guide

AI agents working on this codebase should understand these critical architectural and operational patterns.

## Architecture Overview

This is a **JavaFX desktop application** for a beauty academy management system, using a **layered architecture** with MVVM for UI:

- **UI Layer** (`ui/controller`, `ui/view/*.fxml`): JavaFX controllers + FXML templates (no business logic)
- **Service Layer** (`service/*.java`): Business logic, validation, transaction orchestration
- **Repository Layer** (`repository/*.java`): JPA/Hibernate data access (queries, CRUD)
- **Domain Layer** (`domain/entity/*.java`, `domain/enums/*.java`): JPA entities, state machines
- **Security Layer** (`security/`): Authentication, authorization, session management
- **DTO/Mapper Layer** (`dto/`, `mapper/`): Domain ↔ UI data transfer objects
- **Config Layer** (`config/`): Database, JPA, application properties

See `ARQUITECTURA_ACADEMIA_BELLEZA.md` sections 2–3 for full architecture details.

## Critical Build & Test Commands

```bash
# Maven compilation and validation
mvn clean compile               # Compile sources
mvn clean test                  # Run JUnit 5 tests
mvn validate                    # Check pom.xml and structure
mvn package -DskipTests         # Build JAR (tests optional)

# Project setup (first-time)
mvn clean install               # Full build with local repo setup

# JavaFX execution
mvn clean javafx:run            # Run application directly
```

**Key Build Notes:**
- Java 17+ required; set IDE to JDK 17 (`pom.xml` enforces this)
- JavaFX runtime must be available; if missing, add `--add-modules javafx.controls,javafx.fxml` to VM options
- PostgreSQL driver included; change in `pom.xml` if using MySQL

## Core Data Flow Patterns

### 1. Creating/Modifying Entities (Example: Registering Payment)

UI Controller → Service.registrarPago(DTO) → Service validates → Repository.save() + create MovimientoFinanciero + update Matrícula saldo + audit → return DTO

**Key files involved:**
- `service/PagoEstudianteService.java` (business rules: monto > 0, ≤ saldo)
- `repository/PagoEstudianteRepository.java`, `MovimientoFinancieroRepository.java`
- `domain/entity/PagoEstudiante.java`, `MovimientoFinanciero.java` (enums: `EstadoPago`, `TipoMovimiento`)

### 2. Authorization Checkpoints

Every service method that modifies data should validate permissions:
```java
authorizationService.requirePermission("PAGO_REGISTRAR"); // throws AuthException if denied
```

See `BACKLOG_TECNICO_ACADEMIA.md` sec 1.4 (SEC-06, SEC-07) for permission codes by role.

### 3. Financial Transactions (Critical)

Payments and payroll must be **atomic**:
- Create `PagoEstudiante` + `MovimientoFinanciero` + update `Matricula.saldoPendiente` in one transaction
- If any step fails, rollback all; no orphaned records
- Always audit the operation with before/after values

### 4. State Machine Examples

**Course states:** PLANIFICADO → ABIERTO → (CERRADO | CANCELADO) – no backwards transitions  
**Matrícula states:** PENDIENTE → ACTIVA → (CANCELADA | FINALIZADA)  
**Payroll states:** BORRADOR → APROBADA → PAGADA (no edits once APROBADA)  

See `domain/enums/` for all enum definitions.

## Project-Specific Conventions

### 1. DTO Naming & Mapping

Use `*DTO` suffix (e.g., `EstudianteDTO`, `PagoEstudianteDTO`). **Never expose JPA entities to UI controllers.** Use `*Mapper` classes for conversion:

```java
// In UI controller:
EstudianteDTO dto = mapper.toDTO(entity);
Estudiante entity = mapper.toEntity(dto);
```

### 2. Validation Pattern

- **UI validation** (input format, required fields): in Controller before calling Service
- **Business validation** (duplicate doc, cupo exceeded, saldo limits): in Service, throw `BusinessException` with user-friendly message

### 3. Service Layer Responsibilities

- Orchestrate multi-step operations (e.g., matrícula creation checks student activo → curso ABIERTO → cupo available)
- Apply permission checks via `AuthorizationService`
- Register audit logs for sensitive operations
- Wrap database calls in transactions (`@Transactional` or explicit transaction management)

### 4. Soft Delete Pattern

**No physical deletion** of records with history:
- Estudiante, Maestro: use `activo` flag + `fechaBaja` date
- Pagos, Movimientos, Nómina: use `estado = ANULADO` instead of DELETE
- This preserves audit trail and financial records

### 5. Date/Number Formatting

Use utilities in `util/` package:
- `DateUtils.parse()`, `DateUtils.format()` for consistent date handling
- `MoneyUtils.*()` for BigDecimal money calculations (never use double for money)
- Store dates as `DATE` (not TIMESTAMP) unless time-of-day matters

### 6. Strategic Payment Calculation (Critical for Payroll)

Payroll uses **Strategy pattern** (`service/nomina/PagoProfesorStrategy*`):

```
PagoProfesorStrategyFactory.getStrategy(maestro.getTipoPagoProfesor())
  → PagoFijoStrategy | PagoPorHoraStrategy | PagoPorCursoStrategy | PagoPorcentajeStrategy
  → calcularPago(maestro, periodo, contexto) → returns BigDecimal
```

**Formulas** in `ARQUITECTURA_ACADEMIA_BELLEZA.md` sec 10.10–10.13:
- **Fijo:** `salarioMensual * proporcionPeriodo`
- **Por hora:** `tarifaHora * horasTrabajadasPeriodo`
- **Por curso:** `tarifaPorCurso * cursosImpartidosPeriodo`
- **Porcentaje:** `(porcentaje/100) * ingresosCursoPeriodo` (ingresos = VIGENTE student payments only)

Test each strategy independently with fixed test data.

## Cross-Module Integration Points

| Source | Target | Pattern | File Reference |
|--------|--------|---------|-----------------|
| Matrícula creation | Curso validation | Curso must be ABIERTO, cupo available | `MatriculaService`, `CursoService` |
| Pago registration | Matrícula saldo | Decrement `saldoPendiente` atomically | `PagoEstudianteService` |
| Nómina calculation | Curso data | Count courses imparted in period | `NominaService`, `CursoRepository` queries |
| Login | Rol/Permiso | Load user roles and permisos lazily in session | `SessionManager`, `UsuarioRepository` |

## Development Workflow – Build, Test, Debug

1. **Before pushing code:**
   ```bash
   mvn clean compile          # Catch syntax errors
   mvn test                   # Run all tests (must pass)
   mvn javafx:run             # Manual smoke test of UI flow
   ```

2. **Test scope:** Unit tests in `src/test/java/` using JUnit 5 + mock repositories (no DB required for most tests)

3. **Common debugging:**
   - Set breakpoints in Service layer methods to trace business logic
   - Check `Auditoria` table for who/when/what changes happened
   - Query `MovimientoFinanciero` to verify financial record integrity

## Key Files by Role

| Role | Must Read | Reference |
|------|-----------|-----------|
| **Backend/Service Dev** | `ARQUITECTURA_ACADEMIA_BELLEZA.md` (sec 3–4), `service/*`, `domain/entity/*` | Backlog: SEC, EST, PAG, NOM epics |
| **UI/Controller Dev** | `ARQUITECTURA_ACADEMIA_BELLEZA.md` (sec 8), `ui/controller/*`, DTOs, Mappers | Backlog: EST-03, CUR-03, PAG-04, NOM-05 |
| **Database/Migration Dev** | `ARQUITECTURA_ACADEMIA_BELLEZA.md` (sec 6), pom.xml (Flyway), `src/main/resources/db/migration/` | Backlog: SEC-01, SEC-02 |
| **Security/Auth Dev** | `security/*`, `domain/entity/Usuario.java`, `SessionManager`, `AuthorizationService` | Backlog: SEC-02 through SEC-07 |

## Known MVP Constraints

- **Single maestro per curso** (N:1 relation; future may support multiple via join table)
- **Simple descuentos** (fixed amount or percentage; no complex promociones yet)
- **No transaction rollback UI indicators** (errors logged but recovery workflow TBD)
- **Payroll limited to one strategy per maestro** (future: mixed strategies per period)

## Extending the System

1. **Adding a new service:** Create `NewFeatureService` in `service/`, add repository interface in `repository/`, implement business logic, add authorization checks, register audit hooks
2. **Adding entities:** Create in `domain/entity/`, add enum for states in `domain/enums/` if needed, create repository interface, add Flyway migration in `src/main/resources/db/migration/`
3. **Adding UI screen:** Create FXML in `src/main/resources/ui/view/`, controller in `ui/controller/`, wire to `MainApp` scene navigation, use DTOs (never raw entities)
4. **Adding report:** Create JRXML template in `reports/jasper/`, add query method to relevant repository, call `JasperReportService.generate()` from controller

## Troubleshooting

- **Hibernate lazy loading errors:** Ensure relationships loaded within transaction or use `@Transactional` on service methods
- **Saldo calculation mismatch:** Audit recent payments/anulations; query `MovimientoFinanciero` separately to verify  
- **Permission denied messages:** Check user's roles/permisos in `usuario_rol` and `rol_permiso` tables
- **UI not updating:** Verify DTOs returned from Service have correct values; check JavaFX binding/observable properties
- **Build fails with module errors:** Ensure JavaFX modules available: `javafx.controls`, `javafx.fxml`

