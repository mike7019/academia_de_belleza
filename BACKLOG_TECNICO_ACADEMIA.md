# Backlog Técnico – Sistema de Gestión para Academia de Belleza

Este documento define un backlog técnico realista, organizado por épicas y preparado para ser cargado en Jira / GitHub Issues / tableros Scrum.

Convenciones:
- **Prioridad**: Alta / Media / Baja
- **Complejidad**: Baja / Media / Alta (esfuerzo relativo)
- **ID**: identificador sugerido para usar en Issues

---

## Épica 1: Seguridad y acceso

### 1.1 Feature: Infraestructura básica de proyecto y BD

#### Historia SEC-01: Inicializar proyecto Maven y configuración básica

- **Tipo**: Historia técnica  
- **Descripción**: Configurar el proyecto Maven con Java 17, dependencias básicas (JavaFX, Hibernate, PostgreSQL, Flyway, JUnit) y estructura mínima de paquetes.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: Ninguna

**Subtareas sugeridas**
1. Crear proyecto Maven con `pom.xml` base.
2. Agregar dependencias de JavaFX, Hibernate, PostgreSQL, Flyway, JasperReports, Lombok y JUnit.
3. Crear estructura de paquetes `org.example.academia.*`.
4. Crear clase `MainApp` de JavaFX con ventana simple.

**Criterios de aceptación**
- El proyecto compila sin errores.
- `mvn clean test` se ejecuta sin fallas.
- La clase `MainApp` se puede ejecutar mostrando una ventana básica.

**Definición de Terminado (DoD)**
- Dependencias y plugins Maven documentados en el `README`.
- Proyecto abre sin errores en el IDE.

**Riesgos**
- Versión incompatible de Java/JavaFX en el entorno de desarrollo.

---

### 1.2 Feature: Modelo y repositorio de seguridad

#### Historia SEC-02: Entidades Usuario, Rol y Permiso (JPA)

- **Tipo**: Técnica  
- **Descripción**: Crear entidades `Usuario`, `Rol`, `Permiso` con anotaciones JPA, relaciones N:M y restricciones básicas.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-01

**Subtareas**
1. Crear entidad `Usuario` con campos: `username`, `passwordHash`, `nombreCompleto`, `email`, `activo`.
2. Crear entidad `Rol` con `nombre`, `descripcion`, `activo`.
3. Crear entidad `Permiso` con `codigo`, `descripcion`, `modulo`.
4. Modelar relaciones N:M `Usuario`–`Rol` y `Rol`–`Permiso`.
5. Actualizar migraciones Flyway con tablas `usuario`, `rol`, `permiso` y tablas intermedias.

**Criterios de aceptación**
- `username` y `codigo` de permiso son únicos.
- El esquema de BD generado es coherente con el diseño (ARQUITECTURA_ACADEMIA_BELLEZA.md).

**DoD**
- Tests básicos de persistencia (crear y consultar usuarios, roles, permisos).

**Riesgos**
- Sobrecomplejidad inicial de permisos; puede requerir refactor.

---

#### Historia SEC-03: Repositorios Usuario, Rol y Permiso

- **Tipo**: Técnica  
- **Descripción**: Crear interfaces `UsuarioRepository`, `RolRepository` y `PermisoRepository` con operaciones de consulta típicas.
- **Prioridad**: Alta  
- **Complejidad**: Baja  
- **Dependencias**: SEC-02

**Subtareas**
1. Implementar métodos para buscar usuario por `username`.
2. Implementar método para obtener roles de un usuario.
3. Implementar método para obtener permisos de un rol.

**Criterios de aceptación**
- Se puede recuperar un usuario por `username` con sus roles y permisos asociados.

**DoD**
- Tests unitarios de repositorios (con BD de desarrollo o memoria).

**Riesgos**
- Cargas ansiosas (eager) innecesarias que afecten el rendimiento.

---

### 1.3 Feature: Autenticación (login) y hashing de contraseñas

#### Historia SEC-04: Servicio de seguridad con hash de contraseñas

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `SeguridadService` y `PasswordEncoder` para registrar y verificar contraseñas con hash seguro.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-03

**Subtareas**
1. Integrar una librería de hashing (por ejemplo, BCrypt) o implementar un wrapper.
2. Implementar `encode(plainPassword)` y `matches(plainPassword, hash)`.
3. Implementar `SeguridadService.login(username, password)` usando `UsuarioRepository` y `PasswordEncoder`.
4. Actualizar entidad `Usuario` para guardar solo `passwordHash`.

**Criterios de aceptación**
- Contraseñas nunca se almacenan en texto plano.
- Login exitoso solo si `matches` devuelve true.

**DoD**
- Tests unitarios que validen hashing y verificación.
- Documentación breve del esquema de seguridad.

**Riesgos**
- Errores de implementación exponen contraseñas o permiten bypass.

---

#### Historia SEC-05: Pantalla de login conectada a SeguridadService

- **Tipo**: Funcional UI  
- **Descripción**: Conectar `LoginController` de JavaFX a `SeguridadService` para autenticar contra la BD y abrir el Dashboard.
- **Prioridad**: Alta  
- **Complejidad**: Baja  
- **Dependencias**: SEC-04

**Subtareas**
1. Inyectar o acceder a `SeguridadService` desde `LoginController`.
2. Sustituir lógica mock (admin/admin) por validación real.
3. Navegar a `dashboard.fxml` en caso de éxito.
4. Manejar y mostrar mensajes de error en caso de fallo.

**Criterios de aceptación**
- Usuario válido abre la pantalla de Dashboard.
- Usuario o contraseña incorrectos muestran mensaje adecuado.

**DoD**
- Pruebas manuales con varios usuarios.

**Riesgos**
- Manejo de sesión duplicado si no se centraliza en un `SessionManager`.

---

### 1.4 Feature: Autorización por roles y permisos

#### Historia SEC-06: Implementar AuthorizationService y SessionManager

- **Tipo**: Técnica  
- **Descripción**: Crear `AuthorizationService` y `SessionManager` para gestionar el usuario autenticado y verificar permisos.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-05

**Subtareas**
1. Implementar `SessionManager` con métodos `login`, `logout`, `getCurrentUser`.
2. Implementar `AuthorizationService.hasPermission` y `requirePermission`.
3. Integrar `SessionManager` con `SeguridadService`.

**Criterios de aceptación**
- Es posible consultar el usuario actual en cualquier parte de la app.
- `requirePermission` lanza excepción de autorización si el usuario no tiene permiso.

**DoD**
- Tests unitarios de autorización con diferentes combinaciones de roles y permisos.

**Riesgos**
- Omisión de checks de permiso en servicios críticos.

---

#### Historia SEC-07: Integrar autorización en servicios clave (MVP)

- **Tipo**: Técnica/Funcional  
- **Descripción**: Añadir validaciones de permisos mínimas en servicios de Estudiantes, Maestros, Cursos, Matrículas y Pagos.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-06, más servicios de otros módulos

**Subtareas**
1. Añadir `requirePermission` en operaciones de creación/edición de entidades principales.
2. Añadir `requirePermission` en anulación de pagos.
3. Definir mapa inicial de permisos por rol (ADMIN, CAJA, ACADEMICO).

**Criterios de aceptación**
- Intentos de realizar operaciones sin permiso resultan en errores controlados.

**DoD**
- Casos de prueba manual de operaciones denegadas.

**Riesgos**
- UX frustrante si no se comunican bien los errores de permiso.

---

## Épica 2: Estudiantes

### 2.1 Feature: Modelo y repositorio de Estudiantes

#### Historia EST-01: Entidad Estudiante y repositorio

- **Tipo**: Técnica  
- **Descripción**: Crear entidad `Estudiante` con atributos definidos y `EstudianteRepository`.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-01

**Subtareas**
1. Implementar entidad `Estudiante` con campos: nombre, apellido, tipoDocumento, numeroDocumento, teléfono, email, dirección, fechaRegistro, activo.
2. Definir constraint única en `numeroDocumento`.
3. Crear `EstudianteRepository` con método para buscar por documento.
4. Actualizar migraciones Flyway.

**Criterios de aceptación**
- CRUD de Estudiante funciona a nivel de repositorio.

**DoD**
- Tests de repositorio para crear, leer y actualizar estudiantes.

**Riesgos**
- Falta de campos de negocio que luego sean requeridos.

---

### 2.2 Feature: Servicio de Estudiantes

#### Historia EST-02: EstudianteService con reglas de negocio

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `EstudianteService` para alta, edición, inactivación y búsqueda, con validaciones de documento único y estado.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: EST-01, SEC-07

**Subtareas**
1. Implementar método `crearEstudiante(EstudianteDTO)` con validación de documento.
2. Implementar método `actualizarEstudiante`.
3. Implementar método `inactivarEstudiante`.
4. Añadir validaciones de negocio (no borrar físicamente, solo inactivar).

**Criterios de aceptación**
- Intento de crear estudiante con documento duplicado produce error de negocio.
- Inactivación no elimina datos, solo cambia flag.

**DoD**
- Tests de servicio cubriendo alta, actualización, inactivación y duplicados.

**Riesgos**
- Duplicación de validaciones en UI y servicio.

---

### 2.3 Feature: UI de Estudiantes

#### Historia EST-03: Pantalla de gestión de estudiantes

- **Tipo**: UI  
- **Descripción**: Crear pantalla JavaFX para listar, filtrar, crear, editar e inactivar estudiantes.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: EST-02, SEC-05

**Subtareas**
1. Crear FXML y controlador para listado.
2. Crear formulario de alta/edición.
3. Integrar con `EstudianteService`.

**Criterios de aceptación**
- Se pueden realizar operaciones CRUD desde la UI.
- Filtros por nombre, documento y estado funcionan.

**DoD**
- Pruebas manuales de todo el flujo.

**Riesgos**
- Controlador con demasiadas responsabilidades.

---

## Épica 3: Maestros

### 3.1 Feature: Modelo y repositorio de Maestros

#### Historia MAE-01: Entidad Maestro y repositorio

- **Tipo**: Técnica  
- **Descripción**: Crear entidad `Maestro` (docente) y `MaestroRepository` con campos de modalidad de pago.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: SEC-01

**Subtareas**
1. Implementar campos personales (similar a Estudiante).
2. Implementar campos de pago: tipoPagoProfesor, tarifaHora, salarioMensual, tarifaPorCurso, porcentajePorCurso.
3. Constraint única sobre `numeroDocumento`.
4. Actualizar migraciones Flyway.

**Criterios de aceptación**
- CRUD básico de Maestro funciona.

**DoD**
- Tests de repositorio.

**Riesgos**
- Configuración de modalidad de pago difícil de entender para usuario.

---

### 3.2 Feature: Servicio y UI de Maestros

#### Historia MAE-02: MaestroService con validaciones de modalidad de pago

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `MaestroService` para alta/edición con validación de modalidad de pago.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: MAE-01, SEC-07

**Subtareas**
1. Validar que los campos de pago requeridos estén presentes según `tipoPagoProfesor`.
2. Implementar inactivación segura (sin borrar maestros con historial).

**Criterios de aceptación**
- No se puede guardar un maestro con modalidad de pago inconsistente.

**DoD**
- Tests de servicio para combinaciones de tipos de pago.

**Riesgos**
- Cambios futuros en reglas de pago requieran refactor.

---

#### Historia MAE-03: Pantalla de gestión de maestros

- **Tipo**: UI  
- **Descripción**: UI para CRUD de maestros y configuración de modalidad de pago.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: MAE-02, SEC-05

**Subtareas**
1. Crear vista (tabla y formulario) para maestros.
2. Integrar con `MaestroService`.
3. Mostrar/ocultar campos de pago según modalidad elegida.

**Criterios de aceptación**
- CRUD desde la UI funciona.
- Modalidad de pago seleccionada se refleja en campos.

**DoD**
- Pruebas manuales de flujos.

**Riesgos**
- UI confusa si muestra demasiados campos simultáneamente.

---

## Épica 4: Cursos

### 4.1 Feature: Modelo y repositorio de Cursos

#### Historia CUR-01: Entidad Curso y repositorio

- **Tipo**: Técnica  
- **Descripción**: Crear entidad `Curso` con precio, cupo, estado y relación con Maestro, y `CursoRepository`.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: MAE-01

**Subtareas**
1. Implementar `Curso` con campos: nombre, descripcion, precioBase, cupoMaximo, estado, fechaInicio, fechaFin.
2. Relación N:1 con `Maestro`.
3. Actualizar migraciones Flyway.

**Criterios de aceptación**
- CRUD funcional a nivel repositorio.

**DoD**
- Tests de repositorio.

**Riesgos**
- Cambios futuros que requieran múltiples maestros por curso.

---

### 4.2 Feature: Servicio de Cursos y cambios de estado

#### Historia CUR-02: CursoService con reglas de estado y cupo

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `CursoService` con lógica para abrir/cerrar/cancelar cursos y validar cupos.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: CUR-01, SEC-07

**Subtareas**
1. Implementar cambio de estado PLANIFICADO → ABIERTO (validar maestro asignado).
2. Implementar cambio ABIERTO → CERRADO / CANCELADO.
3. Proveer consultas por estado y rango de fechas.

**Criterios de aceptación**
- No se puede abrir curso sin maestro (regla configurable).

**DoD**
- Tests para todas las transiciones de estado.

**Riesgos**
- Lógica de estados se complique si se agregan más casos especiales.

---

#### Historia CUR-03: Pantalla de gestión de cursos

- **Tipo**: UI  
- **Descripción**: UI para listar, crear, editar cursos y gestionar su estado.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: CUR-02, SEC-05

**Subtareas**
1. Crear tabla de cursos con filtros.
2. Formulario CRUD.
3. Acciones para cambiar estado.

**Criterios de aceptación**
- CRUD y cambios de estado desde la UI funcionan correctamente.

**DoD**
- Prueba manual completa documentada.

**Riesgos**
- UX de estados confusa para usuario no técnico.

---

## Épica 5: Matrículas

### 5.1 Feature: Modelo y repositorio de Matrículas

#### Historia MAT-01: Entidad Matricula y repositorio

- **Tipo**: Técnica  
- **Descripción**: Crear `Matricula` enlazando Estudiante–Curso, con valores económicos y estado.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: EST-01, CUR-01

**Subtareas**
1. Implementar campos: fecha, estado, valorBase, descuento, valorFinal, observaciones.
2. Relación N:1 con `Estudiante` y `Curso`.
3. Constraint opcional única (`estudiante_id`, `curso_id`).

**Criterios de aceptación**
- CRUD y consultas por estudiante/curso funcionen a nivel repositorio.

**DoD**
- Tests unitarios para persistencia.

**Riesgos**
- Regla de duplicidad de matrícula cambie según negocio.

---

### 5.2 Feature: Servicio de Matrículas

#### Historia MAT-02: MatriculaService con reglas de cupo y valor

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `MatriculaService` para crear, cancelar y gestionar matrículas, calculando valor y controlando cupos.
- **Prioridad**: Alta  
- **Complejidad**: Alta  
- **Dependencias**: MAT-01, CUR-02, EST-02, SEC-07

**Subtareas**
1. Implementar creación de matrícula verificando estado del curso y cupos.
2. Calcular `valorFinal` a partir de `precioBase` y `descuento`.
3. Implementar cancelación de matrícula.

**Criterios de aceptación**
- No se permite matricular en curso sin cupo o no ABIERTO.
- Valor final y estados se calculan correctamente.

**DoD**
- Tests cubriendo escenarios de cupo lleno, descuentos y cancelaciones.

**Riesgos**
- Futuras reglas de descuentos hagan compleja la lógica.

---

#### Historia MAT-03: Pantalla de Matrículas

- **Tipo**: UI  
- **Descripción**: UI para inscribir estudiantes en cursos y listar matrículas.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: MAT-02, SEC-05

**Subtareas**
1. Vista para seleccionar estudiante y curso.
2. Mostrar precios, descuentos y valor final.
3. Crear matrícula vía `MatriculaService`.

**Criterios de aceptación**
- Matrículas se crean correctamente desde la UI.

**DoD**
- Pruebas manuales del flujo de matrícula (seleccionar estudiante, curso, aplicar descuento, confirmar).

**Riesgos**
- UX confusa si la selección de estudiante/curso no es clara.

---

## Épica 6: Pagos de estudiantes

### 6.1 Feature: Modelo de PagoEstudiante y MovimientoFinanciero

#### Historia PAG-01: Entidades PagoEstudiante y MovimientoFinanciero

- **Tipo**: Técnica  
- **Descripción**: Crear entidades `PagoEstudiante` y `MovimientoFinanciero` con relaciones y atributos según el diseño financiero.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: MAT-01

**Subtareas**
1. Implementar `PagoEstudiante` con campos: fecha, monto, metodoPago, estado, referencia, observaciones.
2. Implementar `MovimientoFinanciero` con campos: fecha, tipo, monto, concepto, origen, idOrigen, estado.
3. Relacionar `PagoEstudiante` con `Matricula` y con `MovimientoFinanciero` (directamente o por origen/idOrigen).
4. Actualizar migraciones Flyway.

**Criterios de aceptación**
- Se pueden crear pagos y movimientos financieros asociados en la BD.

**DoD**
- Tests de persistencia para pagos y movimientos.

**Riesgos**
- Diseño inadecuado del origen de movimientos complique reportes futuros.

---

### 6.2 Feature: Servicio de pagos de estudiantes

#### Historia PAG-02: PagoEstudianteService (registro de pagos y actualización de saldo)

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar `PagoEstudianteService` para registrar pagos de estudiantes, actualizar saldos de matrículas y generar `MovimientoFinanciero` de tipo INGRESO.
- **Prioridad**: Alta  
- **Complejidad**: Alta  
- **Dependencias**: PAG-01, MAT-02, SEC-07

**Subtareas**
1. Implementar método `registrarPago(matriculaId, PagoEstudianteDTO)` validando que el monto sea > 0 y no exceda el saldo.
2. Calcular el nuevo saldo de la matrícula.
3. Crear `MovimientoFinanciero` de tipo INGRESO ligado al pago.
4. Encapsular operación en una transacción.

**Criterios de aceptación**
- No se permite registrar pagos con monto mayor al saldo pendiente.
- Saldos de matrícula se actualizan correctamente tras uno o varios pagos.

**DoD**
- Tests de servicio con escenarios de pago total, parcial y múltiples pagos.

**Riesgos**
- Doble pago en situaciones de concurrencia o error de usuario.

---

#### Historia PAG-03: Anulación de pagos

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar anulación de pagos respetando reglas de negocio y actualizando saldos y movimientos financieros.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: PAG-02

**Subtareas**
1. Implementar método `anularPago(pagoId)` con validación de permisos y ventana de anulación.
2. Marcar pago como ANULADO.
3. Crear movimiento financiero inverso (EGRESO o ajuste negativo).
4. Recalcular saldo de la matrícula.

**Criterios de aceptación**
- Pagos anulados no se cuentan para el saldo.
- Cada anulación deja un rastro financiero y de auditoría claro.

**DoD**
- Tests de servicio para anulación de pagos.

**Riesgos**
- Descuadre contable si falla la transacción durante el proceso.

---

#### Historia PAG-04: Pantalla de Caja/Pagos

- **Tipo**: UI  
- **Descripción**: Crear pantalla de caja para registrar pagos de estudiantes, visualizar saldos y anular pagos.
- **Prioridad**: Alta  
- **Complejidad**: Media  
- **Dependencias**: PAG-02, MAT-03, SEC-05

**Subtareas**
1. Implementar búsqueda de estudiante y selección de matrícula.
2. Formulario para registrar pago (monto, método, fecha, observaciones).
3. Tabla de historial de pagos por matrícula.
4. Acción para anular pago (con permisos y confirmación).

**Criterios de aceptación**
- El usuario puede ver el saldo de cada matrícula y registrar pagos correctamente.
- La anulación de pagos solo está disponible para usuarios autorizados.

**DoD**
- Pruebas manuales del flujo completo de caja.

**Riesgos**
- Búsqueda de estudiantes/matrículas lenta o poco intuitiva.

---

## Épica 7: Nómina y pagos a profesores

### 7.1 Feature: Modelo de nómina

#### Historia NOM-01: Entidades PeriodoNomina, NominaProfesor y DetalleNominaProfesor

- **Tipo**: Técnica  
- **Descripción**: Modelar las entidades de nómina con estados, relaciones y atributos necesarios para el cálculo de pagos a profesores.
- **Prioridad**: Media-Alta  
- **Complejidad**: Media  
- **Dependencias**: MAE-01, CUR-01, PAG-01

**Subtareas**
1. Implementar `PeriodoNomina` con nombre, fechaInicio, fechaFin y estado.
2. Implementar `NominaProfesor` con referencia a Maestro y Periodo, montos y estados.
3. Implementar `DetalleNominaProfesor` con tipoConcepto, descripcion, cantidad, tarifa, monto, referenciaExterna.
4. Actualizar migraciones Flyway con las tablas de nómina.

**Criterios de aceptación**
- CRUD básico de entidades de nómina disponible a nivel de repositorio.

**DoD**
- Tests básicos de persistencia.

**Riesgos**
- Estructura de detalle demasiado rígida para futuras necesidades.

---

### 7.2 Feature: Estrategias de cálculo de pago a profesores

#### Historia NOM-02: Implementar PagoProfesorStrategy y factory

- **Tipo**: Técnica  
- **Descripción**: Implementar interfaz `PagoProfesorStrategy` y estrategias concretas para pago fijo mensual, por hora, por curso y por porcentaje.
- **Prioridad**: Media  
- **Complejidad**: Alta  
- **Dependencias**: NOM-01, MAE-02, PAG-02

**Subtareas**
1. Definir interfaz `PagoProfesorStrategy` y DTO de contexto de cálculo.
2. Implementar `PagoFijoStrategy`, `PagoPorHoraStrategy`, `PagoPorCursoStrategy`, `PagoPorcentajeStrategy`.
3. Implementar `PagoProfesorStrategyFactory` que devuelva la estrategia según `tipoPagoProfesor`.

**Criterios de aceptación**
- Cada estrategia calcula el monto según las fórmulas definidas.

**DoD**
- Tests unitarios con casos de prueba para cada modalidad.

**Riesgos**
- Errores sutiles en fórmulas afecten confianza del negocio.

---

### 7.3 Feature: Servicio de nómina

#### Historia NOM-03: Calcular nómina (estado BORRADOR)

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar servicio que calcule `NominaProfesor` en estado BORRADOR para un periodo determinado utilizando las estrategias de pago.
- **Prioridad**: Media-Alta  
- **Complejidad**: Alta  
- **Dependencias**: NOM-02

**Subtareas**
1. Implementar método para seleccionar maestros activos en el periodo.
2. Agregar lógica para sumar horas, cursos e ingresos por curso.
3. Crear/actualizar `NominaProfesor` y `DetalleNominaProfesor` en estado BORRADOR.
4. Permitir recalcular en BORRADOR (borrando detalles anteriores y regenerándolos).

**Criterios de aceptación**
- Para un periodo dado, se generan nóminas en BORRADOR para maestros aplicables.

**DoD**
- Tests de servicio con escenarios simples de cálculo de nómina.

**Riesgos**
- Consultas pesadas si se calcula sobre grandes volúmenes de datos.

---

#### Historia NOM-04: Aprobar y pagar nómina

- **Tipo**: Técnica/Funcional  
- **Descripción**: Implementar la transición de estados BORRADOR → APROBADA → PAGADA y la creación de `MovimientoFinanciero` para el pago de nómina.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: NOM-03, PAG-01

**Subtareas**
1. Implementar método para aprobar nómina, validando que esté en BORRADOR.
2. Implementar método para registrar el pago, creando MovimientoFinanciero tipo EGRESO.
3. Evitar doble pago de la misma nómina.

**Criterios de aceptación**
- No se puede pagar nómina no aprobada ni pagarla más de una vez.

**DoD**
- Tests que cubran aprobación y pago exitoso y casos de error.

**Riesgos**
- Necesidad futura de anular nómina pagada complique el modelo.

---

#### Historia NOM-05: Pantalla de gestión de nómina

- **Tipo**: UI  
- **Descripción**: UI para gestionar `PeriodoNomina`, calcular, aprobar y pagar nóminas por profesor.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: NOM-03, NOM-04, SEC-05

**Subtareas**
1. Vista para listar periodos de nómina.
2. Vista de nóminas por periodo (NominaProfesor) con montos y estados.
3. Vista de detalles de nómina por profesor.
4. Botones/acciones para Calcular, Aprobar y Pagar.

**Criterios de aceptación**
- Flujos de cálculo, aprobación y pago pueden realizarse desde la UI, respetando permisos.

**DoD**
- Pruebas manuales completas para al menos un periodo de nómina.

**Riesgos**
- Pantalla demasiado cargada si se mezclan muchas funciones sin separar por pestañas.

---

## Épica 8: Reportes

### 8.1 Feature: Infraestructura JasperReports

#### Historia REP-01: Integrar JasperReports y servicio de reportes

- **Tipo**: Técnica  
- **Descripción**: Configurar JasperReports en el proyecto y crear `JasperReportService` para generar reportes en PDF/Excel.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: SEC-01

**Subtareas**
1. Verificar dependencias de JasperReports en `pom.xml`.
2. Crear estructura de carpetas para plantillas JRXML.
3. Implementar `JasperReportService` con carga de plantillas y generación de reportes.

**Criterios de aceptación**
- Se puede generar al menos un reporte de prueba.

**DoD**
- Documentación mínima de cómo agregar nuevos reportes.

**Riesgos**
- Problemas de compatibilidad de librerías o fonts en entornos de cliente.

---

### 8.2 Feature: Reportes MVP

#### Historia REP-02: Reporte de Estudiantes

- **Tipo**: Funcional  
- **Descripción**: Implementar reporte Jasper de listado de estudiantes con filtros por nombre, documento y estado.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: REP-01, EST-03

**Subtareas**
1. Definir consulta para obtener estudiantes con filtros.
2. Diseñar plantilla JRXML para el reporte.
3. Integrar reporte en una pantalla de reportes.

**Criterios de aceptación**
- El reporte se genera en PDF con datos correctos según filtros.

**DoD**
- Pruebas manuales generando reportes con distintos filtros.

**Riesgos**
- Lento si se ejecuta sin paginación o filtros en grandes volúmenes.

---

#### Historia REP-03: Reporte de Pagos por Estudiante

- **Tipo**: Funcional  
- **Descripción**: Implementar reporte de pagos por estudiante, mostrando detalle de matrículas, pagos y saldos.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: REP-01, PAG-02, MAT-03

**Subtareas**
1. Definir consulta que combine estudiante, matrículas y pagos.
2. Diseñar plantilla JRXML.
3. Añadir opción en módulo de reportes.

**Criterios de aceptación**
- Reporte muestra pagos y saldo correctamente.

**DoD**
- Pruebas manuales con casos de estudiantes con múltiples matrículas.

**Riesgos**
- Consulta demasiado compleja o lenta.

---

#### Historia REP-04: Reporte de Nómina por Periodo

- **Tipo**: Funcional  
- **Descripción**: Implementar reporte de nómina por periodo, mostrando montos totales por profesor y conceptos.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: REP-01, NOM-03

**Subtareas**
1. Definir consultas sobre `NominaProfesor` y `DetalleNominaProfesor`.
2. Diseñar plantilla de reporte.
3. Integrar en módulo de reportes.

**Criterios de aceptación**
- Reporte refleja exactamente los montos de nómina calculados en el sistema.

**DoD**
- Comparación manual entre datos de pantalla y reporte.

**Riesgos**
- Diferencias entre lógica de cálculo y lógica de reporte si no se reutiliza código o consultas.

---

#### Historia REP-05: Reporte de Ingresos por rango de fechas

- **Tipo**: Funcional  
- **Descripción**: Implementar reporte de ingresos por rango de fechas usando `MovimientoFinanciero` (tipo INGRESO).
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: REP-01, PAG-02

**Subtareas**
1. Definir consulta sobre `movimiento_financiero` filtrando por tipo INGRESO y fechas.
2. Diseñar reporte agregando montos por día/curso.
3. Integrar en módulo de reportes.

**Criterios de aceptación**
- Montos de ingresos coinciden con los registros de pagos en el sistema.

**DoD**
- Pruebas manuales con rangos de fechas distintos.

**Riesgos**
- Falta de índices adecuados afecte rendimiento en producción.

---

## Épica 9: Dashboard

### 9.1 Feature: Dashboard principal

#### Historia DASH-01: Vista de Dashboard

- **Tipo**: UI  
- **Descripción**: Crear pantalla de inicio con accesos directos a módulos y KPIs básicos (número de estudiantes activos, cursos abiertos, pagos del día, etc.).
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: EST-03, CUR-03, PAG-04

**Subtareas**
1. Diseñar layout del Dashboard (tarjetas, botones, menú lateral o superior).
2. Implementar llamadas a servicios para obtener KPIs básicos.
3. Configurar navegación a Estudiantes, Cursos, Caja, Nómina y Reportes.

**Criterios de aceptación**
- Dashboard se muestra inmediatamente después del login.
- Los KPIs reflejan datos reales del sistema.

**DoD**
- Pruebas manuales de navegación a todos los módulos.

**Riesgos**
- Incorporar demasiados indicadores y hacer la vista confusa.

---

## Épica 10: Auditoría y configuración

### 10.1 Feature: Auditoría

#### Historia AUD-01: Entidad Auditoria y servicio

- **Tipo**: Técnica  
- **Descripción**: Crear entidad `Auditoria` y `AuditoriaService` para registrar acciones críticas del sistema.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: SEC-06

**Subtareas**
1. Implementar entidad `Auditoria` con campos: fecha, usuario, accion, entidad, idEntidad, detalleAntes, detalleDespues.
2. Crear `AuditoriaService` con métodos para registrar eventos.
3. Actualizar migraciones Flyway con tabla `auditoria`.

**Criterios de aceptación**
- Es posible registrar y consultar eventos de auditoría.

**DoD**
- Tests básicos de registro de auditoría.

**Riesgos**
- Crecimiento grande de la tabla `auditoria` sin políticas de retención.

---

#### Historia AUD-02: Integrar auditoría en servicios críticos

- **Tipo**: Técnica/Funcional  
- **Descripción**: Integrar llamadas a `AuditoriaService` en servicios de pagos, matrículas, cursos, usuarios y nómina.
- **Prioridad**: Media  
- **Complejidad**: Media  
- **Dependencias**: AUD-01, PAG-02, MAT-02, CUR-02, SEC-04, NOM-03

**Subtareas**
1. Registrar auditoría en creación/edición/anulación de pagos.
2. Registrar auditoría en creación/cancelación de matrículas.
3. Registrar auditoría en cambios de estado de cursos y nómina.
4. Registrar auditoría en creación/edición de usuarios y roles.

**Criterios de aceptación**
- Las operaciones críticas dejan trazas completas en la tabla de auditoría.

**DoD**
- Pruebas manuales verificando registros de auditoría asociados a acciones.

**Riesgos**
- Olvidar integrar auditoría en nuevos servicios si no se estandariza el uso.

---

### 10.2 Feature: Configuración y catálogos

#### Historia CFG-01: Modelo de parámetros y catálogos

- **Tipo**: Técnica  
- **Descripción**: Definir entidades o estructuras para parámetros globales (métodos de pago, tipos de documento, ventanas de anulación, etc.).
- **Prioridad**: Baja-Media  
- **Complejidad**: Media  
- **Dependencias**: Módulos principales estables (PAG, EST, MAT)

**Subtareas**
1. Definir tabla o estructura para parámetros globales.
2. Definir tablas de catálogos básicos (métodos de pago, tipos de documento).
3. Integrar lectura de parámetros en servicios relevantes (ej. ventana de anulación).

**Criterios de aceptación**
- Parámetros y catálogos pueden leerse y usarse en lógica de negocio.

**DoD**
- Tests que verifiquen lectura y uso de parámetros.

**Riesgos**
- Sobreingeniería de configuración en fases tempranas.

---

#### Historia CFG-02: Pantalla de administración básica

- **Tipo**: UI  
- **Descripción**: Crear UI para gestión básica de usuarios, roles y catálogos clave.
- **Prioridad**: Media  
- **Complejidad**: Media-Alta  
- **Dependencias**: SEC-03, CFG-01, SEC-05

**Subtareas**
1. Vista para CRUD de usuarios y asignación de roles.
2. Vista para gestión de métodos de pago y tipos de documento.
3. Integrar validaciones de permisos para acceso a esta pantalla (solo ADMIN).

**Criterios de aceptación**
- Administradores pueden gestionar usuarios y catálogos principales.

**DoD**
- Pruebas manuales de administración y verificación de restricciones de acceso.

**Riesgos**
- Mezclar demasiadas responsabilidades en una misma pantalla de administración.

---

Este backlog está diseñado para servir como base directa para creación de épicas e issues en Jira o GitHub, respetando el orden y las prioridades de implementación del sistema de la academia de belleza.

