# Sistema de Gestión para Academia de Belleza – Blueprint Técnico

## 1. Visión general de la solución

### 1.1 Descripción general

Este documento define el blueprint técnico de un sistema de escritorio para una academia de belleza, desarrollado en Java (JDK 17+), con interfaz de usuario JavaFX, gestión de datos sobre una base de datos relacional (PostgreSQL o MySQL), persistencia mediante JPA/Hibernate, migraciones de base de datos con Flyway, generación de reportes con JasperReports y pruebas con JUnit 5.

El sistema tiene como objetivo centralizar la gestión académica (estudiantes, maestros, cursos, matrículas), financiera (pagos de estudiantes, deudas, movimientos financieros) y de recursos humanos (pagos a profesores, nómina), proporcionando trazabilidad, seguridad y mantenibilidad.

### 1.2 Objetivos técnicos

- Diseñar una arquitectura en capas limpia, extensible y mantenible.
- Separar claramente las responsabilidades entre UI, lógica de negocio y persistencia.
- Utilizar estándares de la plataforma Java: JPA, JDBC, Maven, JavaFX.
- Habilitar la evolución futura hacia nuevas funcionalidades (nuevos tipos de pago, más reportes, etc.).
- Facilitar la realización de pruebas unitarias y de integración.
- Asegurar trazabilidad mediante auditoría de acciones y registro de movimientos financieros.

### 1.3 Objetivos de negocio

- Centralizar la información académica y administrativa de la academia.
- Reducir errores manuales en matrículas, pagos y nómina.
- Disponer de reportes financieros y académicos confiables.
- Controlar accesos mediante usuarios, roles y permisos.
- Mejorar la visibilidad de deudas de estudiantes y obligaciones con profesores.

### 1.4 Alcance del MVP

El MVP (Producto Mínimo Viable) debe incluir:

- Autenticación básica: login con usuarios y roles mínimos (ADMIN, CAJA, ACADEMICO).
- CRUD de Estudiantes.
- CRUD de Maestros.
- CRUD de Cursos.
- Módulo de Matrículas (inscribir estudiantes en cursos, control de cupos, valor base y descuento simple).
- Módulo de Pagos de Estudiantes (pagos y abonos, estado de cuenta por matrícula).
- Registro básico de Movimientos Financieros (ingresos asociados a pagos de estudiantes).
- Módulo de Nómina básico (al menos una modalidad de pago a profesores, por ejemplo pago por curso o por hora). 
- Algunos reportes clave: estudiantes, cursos, pagos por estudiante, nómina por periodo.
- Auditoría mínima de operaciones críticas (pagos, matrículas, login).

### 1.5 Alcance futuro

Futuras versiones posteriores al MVP podrán incluir:

- Modalidades adicionales de pago de profesores: salario fijo mensual, pago por hora, pago por curso, pago por porcentaje del ingreso del curso (todas convivientes). 
- Configuración avanzada de descuentos para estudiantes (por porcentaje, valor fijo, promociones, becas).
- Reportes avanzados: utilidad por curso, resumen financiero mensual, dashboards gráficos.
- Gestión granular de permisos por acción (ver/crear/editar/anular/aprobar/pagar).
- Integración con sistemas externos (facturación electrónica, notificaciones por correo/SMS).
- Mejoras de UX y diseño visual en JavaFX.

---

## 2. Arquitectura propuesta

### 2.1 Patrón arquitectónico recomendado

Se propone una arquitectura en capas combinada con MVVM (Model–View–ViewModel) para la interfaz JavaFX:

- Capa **UI** (`ui`): JavaFX (FXML, Controllers/ViewModels) – sin lógica de negocio.
- Capa **Service** (`service`): casos de uso y reglas de negocio.
- Capa **Repository** (`repository`): acceso a datos vía JPA/Hibernate.
- Capa **Domain** (`domain`): entidades de dominio y lógica de dominio básica.
- Capa **Security** (`security`): autenticación, autorización, sesión.
- Capa **Reports** (`reports`): integración con JasperReports.
- Capa **Config** (`config`): configuración de JPA, BD, parámetros globales.
- Capa **DTO** (`dto`) y **Mapper** (`mapper`): objetos de transferencia y mapeos entre entidades y DTOs.
- Capa **Util** (`util`): utilidades generales (fechas, dinero, validaciones).

Esta estructura favorece el bajo acoplamiento y alta cohesión. La UI no conoce detalles de persistencia, y la lógica de negocio no depende de JavaFX.

### 2.2 Por qué JavaFX

- Integración nativa con Java y Maven.
- Soporte de bindings y propiedades observables (facilita MVVM).
- Capacidad de crear UIs modernas con CSS.
- Mejor experiencia para nuevas aplicaciones de escritorio frente a Swing.
- Comunidad activa y buen soporte para tablas, formularios y componentes típicos de sistemas administrativos.

### 2.3 Estructura por capas y responsabilidades

- **Domain**
  - Contiene entidades JPA que representan el modelo de negocio (por ejemplo, `Estudiante`, `Curso`, `Matricula`, `PagoEstudiante`, `NominaProfesor`, etc.).
  - Incluye enums para estados y tipos (por ejemplo, `EstadoCurso`, `TipoMovimiento`, `TipoPagoProfesor`).
  - Puede incluir objetos de valor (por ejemplo, `Dinero`, `PeriodoFecha`).

- **Repository**
  - Define interfaces como `EstudianteRepository`, `PagoEstudianteRepository`, `NominaProfesorRepository`.
  - Implementa el acceso a datos mediante JPA/Hibernate.
  - Encapsula el uso del `EntityManager` y JPQL/Criteria.

- **Service**
  - Orquesta casos de uso: matricular estudiante, registrar pago, calcular nómina, aprobar nómina, etc.
  - Implementa la lógica de negocio y validaciones.
  - Maneja transacciones (ya sea mediante anotaciones o manejo explícito).
  - Invoca a la capa `security` para validar permisos cuando corresponda.

- **UI**
  - Controladores JavaFX y ViewModels que interactúan con la capa `service` mediante DTOs.
  - Encargados de validación de entrada de usuario (formato, campos requeridos) y presentación de mensajes.

- **Security**
  - Maneja autenticación (login), verificación de contraseñas (hash) y control de sesión.
  - Implementa verificación de permisos por acción.

- **Reports**
  - Encapsula la lógica de generación de JasperReports.
  - Recibe parámetros y datos desde los servicios; produce reportes en formatos PDF/Excel.

- **Config**
  - Configuración de conexión a base de datos, propiedades de la aplicación, configuración de JPA/Hibernate, configuración de JavaFX si aplica.

- **DTO y Mapper**
  - DTOs evitan exponer entidades JPA directamente a la UI.
  - Mappers (manuales o con MapStruct) manejan conversiones entre entidades y DTOs.

- **Util**
  - Clases utilitarias para reusable logic: formato de fechas, manejo de dinero, validaciones, etc.

### 2.4 Patrones de diseño

- **MVVM / MVC (JavaFX)**: separación clara entre vista y lógica.
- **Service Layer**: define operaciones de negocio reutilizables.
- **Repository**: aísla el acceso a datos.
- **DTO**: separa modelo interno de datos de las vistas.
- **Strategy**: para el cálculo de pagos a profesores según diferentes modalidades.
- **Factory**: para instanciar la estrategia adecuada de pago según configuración.

### 2.5 Flujo entre UI, Services, Repository y DB

Ejemplo de flujo al registrar un pago de estudiante:

1. El usuario ingresa al módulo Caja/Pagos y selecciona un estudiante y una matrícula.
2. La pantalla JavaFX construye un `PagoEstudianteDTO` y llama a `PagoEstudianteService.registrarPago(dto, usuarioActual)`.
3. El servicio valida:
   - Que la matrícula esté ACTIVA/PENDIENTE.
   - Que el monto sea mayor que 0 y no supere el saldo pendiente.
4. El servicio consulta `MatriculaRepository` para obtener la matrícula y el saldo.
5. Crea una entidad `PagoEstudiante` y un `MovimientoFinanciero` correspondiente (INGRESO).
6. Guarda ambas entidades a través de `PagoEstudianteRepository` y `MovimientoFinancieroRepository` en una transacción.
7. Actualiza el saldo de la matrícula.
8. Registra una entrada en la tabla de `Auditoria`.
9. Devuelve el resultado al ViewModel, que actualiza la UI.

---

## 3. Módulos funcionales

### 3.1 Autenticación y seguridad

- **Propósito**: Controlar el acceso al sistema, gestionando usuarios, roles y permisos.
- **Funcionalidades**:
  - Login y logout.
  - Gestión de usuarios (crear, editar, bloquear, resetear contraseña).
  - Asignación de roles y permisos a usuarios.
- **Reglas de negocio**:
  - Usuario bloqueado no puede autenticarse.
  - Las contraseñas se guardan siempre con hash seguro (por ejemplo, BCrypt).
  - Número máximo de intentos fallidos antes de bloqueo temporal.
- **Pantallas sugeridas**:
  - Pantalla de Login.
  - Pantalla de Administración de Usuarios.
  - Pantalla opcional de Roles y Permisos.
- **Validaciones importantes**:
  - `username` único.
  - Complejidad mínima de contraseñas.
- **Dependencias**:
  - Entidades: `Usuario`, `Rol`, `Permiso`, `Auditoria`.
  - Servicios: `UsuarioService`, `SeguridadService`.

### 3.2 Estudiantes

- **Propósito**: Administrar los datos de estudiantes.
- **Funcionalidades**:
  - CRUD de estudiantes.
  - Búsqueda por nombre, documento o estado.
  - Consulta de estado de cuenta (matrículas, pagos, saldo).
- **Reglas de negocio**:
  - Documento de identidad único.
  - Estudiante ACTIVO puede ser matriculado; INACTIVO no.
  - No eliminación física; se usa estado o `activo=false`.
- **Pantallas sugeridas**:
  - Lista de estudiantes (tabla con filtros).
  - Formulario de detalle/edición.
  - Vista de estado de cuenta.
- **Validaciones importantes**:
  - Campos básicos obligatorios: nombre, documento, teléfono.
  - Validar formato de email.
- **Dependencias**:
  - `Matricula`, `PagoEstudiante`, `MovimientoFinanciero`, `Auditoria`.

### 3.3 Maestros (Profesores)

- **Propósito**: Gestionar docentes y sus modalidades de pago.
- **Funcionalidades**:
  - CRUD de maestros.
  - Configurar tipo de pago (fijo, por hora, por curso, porcentaje).
  - Asociar maestro a cursos.
- **Reglas de negocio**:
  - Documento de identidad único.
  - Maestro INACTIVO no se asigna a nuevos cursos.
- **Pantallas sugeridas**:
  - Lista de maestros.
  - Detalle maestro (datos + modalidad de pago).
  - Vista de cursos asignados.
- **Validaciones importantes**:
  - Validar modalidad de pago y tarifas/porcentaje.
- **Dependencias**:
  - `Curso`, `NominaProfesor`, `DetalleNominaProfesor`, `Auditoria`.

### 3.4 Cursos

- **Propósito**: Definir la oferta académica.
- **Funcionalidades**:
  - CRUD de cursos (nombre, descripción, fechas, precio, cupo, estado).
  - Asociar maestro principal y, opcionalmente, horarios.
- **Reglas de negocio**:
  - Precio base obligatorio.
  - Control de cupo máximo.
  - Estado del curso: PLANIFICADO, ABIERTO, CERRADO, CANCELADO.
- **Pantallas sugeridas**:
  - Lista de cursos (con filtros por estado, fecha, maestro).
  - Detalle de curso.
- **Validaciones importantes**:
  - No abrir curso sin maestro asignado (regla configurable).
  - No cambiar estado a CERRADO si hay matrículas pendientes (opcional).
- **Dependencias**:
  - `Maestro`, `Matricula`, `PagoEstudiante`, `NominaProfesor`.

### 3.5 Matrículas

- **Propósito**: Inscribir estudiantes en cursos.
- **Funcionalidades**:
  - Crear y gestionar matrículas estudiante–curso.
  - Definir valor base, aplicar descuentos simples, calcular valor final.
  - Controlar estado de matrícula: PENDIENTE, ACTIVA, CANCELADA, FINALIZADA.
- **Reglas de negocio**:
  - No superar el cupo máximo del curso.
  - Descuentos deben estar autorizados y ser válidos.
  - Cancelaciones pueden generar devoluciones parciales (regla futura).
- **Pantallas sugeridas**:
  - Pantalla de matrícula (con búsqueda de estudiante y selección de curso).
  - Listado de matrículas por curso/estudiante.
- **Validaciones importantes**:
  - Evitar matrículas duplicadas (estudiante + curso).
  - Solo cursos ABIERTO aceptan nuevas matrículas.
- **Dependencias**:
  - `Estudiante`, `Curso`, `PagoEstudiante`, `MovimientoFinanciero`.

### 3.6 Pagos de estudiantes

- **Propósito**: Gestionar cobros a estudiantes.
- **Funcionalidades**:
  - Registrar pagos y abonos ligados a una matrícula.
  - Soportar métodos de pago (efectivo, tarjeta, transferencia, etc.).
  - Anular pagos bajo reglas de negocio.
- **Reglas de negocio**:
  - Monto del pago no puede exceder el saldo pendiente.
  - Anulación de pago solo con permisos y dentro de ventanas definidas.
  - Todo pago genera un `MovimientoFinanciero` de tipo INGRESO.
- **Pantallas sugeridas**:
  - Pantalla de Caja/Pagos.
  - Consulta de pagos por estudiante.
- **Validaciones importantes**:
  - Monto > 0.
  - Método de pago obligatorio.
- **Dependencias**:
  - `Matricula`, `MovimientoFinanciero`, `Auditoria`.

### 3.7 Pagos de profesores

- **Propósito**: Registrar los pagos a profesores (generalmente asociados a una nómina).
- **Funcionalidades**:
  - Registrar pago sobre una `NominaProfesor` en estado APROBADA.
  - Consultar historial de pagos por profesor.
- **Reglas de negocio**:
  - No pagar nóminas en estado distinto de APROBADA.
  - Evitar pagos duplicados para la misma nómina.
- **Pantallas sugeridas**:
  - Listado de nóminas aprobadas pendientes de pago.
  - Formulario simple para confirmar el pago.
- **Validaciones importantes**:
  - El monto pagado debe coincidir con el montoTotal de la nómina (o registrar explícitamente la diferencia).
- **Dependencias**:
  - `NominaProfesor`, `DetalleNominaProfesor`, `MovimientoFinanciero`.

### 3.8 Nómina

- **Propósito**: Calcular y administrar pagos a profesores por periodo.
- **Funcionalidades**:
  - Definir `PeriodoNomina` (fechaInicio, fechaFin, estado).
  - Calcular nómina para cada profesor (modalidades de pago).
  - Administrar estados de nómina: BORRADOR, APROBADA, PAGADA, ANULADA.
- **Reglas de negocio**:
  - Recalcular nómina solo en estado BORRADOR.
  - Bloquear edición de detalles una vez APROBADA.
  - No permitir cambios monetarios en nómina PAGADA, solo ajustes en periodos posteriores.
- **Pantallas sugeridas**:
  - Gestión de periodos de nómina.
  - Vista de nómina por profesor.
  - Pantalla de aprobación.
- **Validaciones importantes**:
  - Periodos de nómina no se solapan.
  - Cada `DetalleNominaProfesor` tiene tipo de concepto y montos válidos.
- **Dependencias**:
  - `Maestro`, `Curso`, `PeriodoNomina`, `NominaProfesor`, `DetalleNominaProfesor`, `MovimientoFinanciero`.

### 3.9 Finanzas

- **Propósito**: Mantener registro de todos los movimientos financieros.
- **Funcionalidades**:
  - Registrar ingresos, egresos y ajustes.
  - Consultar movimientos por tipo, fecha, origen.
- **Reglas de negocio**:
  - Ningún movimiento se elimina; se anula con contramovimientos.
  - Todo pago de estudiante o nómina genera un movimiento financiero.
- **Pantallas sugeridas**:
  - Listado de movimientos financieros con filtros.
- **Validaciones importantes**:
  - Tipo de movimiento válido (INGRESO, EGRESO, AJUSTE).
  - Monto > 0.
- **Dependencias**:
  - `PagoEstudiante`, `NominaProfesor`, `Auditoria`.

### 3.10 Reportes

- **Propósito**: Proveer reportes operativos y financieros.
- **Funcionalidades**:
  - Generar reportes Jasper a partir de consultas.
  - Exportar a PDF y Excel según aplique.
- **Pantallas sugeridas**:
  - Módulo de reportes con selección de reporte y filtros.
- **Dependencias**:
  - Usa datos de los servicios y repositorios de todos los módulos.

### 3.11 Administración del sistema

- **Propósito**:Gestionar aspectos globales de configuración.
- **Funcionalidades**:
  - Administración de usuarios, roles y permisos.
  - Gestión de catálogos (métodos de pago, tipos de documentos, etc.).
  - Configuración de parámetros de negocio (p.ej. políticas de descuentos, ventanas de anulación).
- **Pantallas sugeridas**:
  - Pantalla de configuración general.
  - Gestión de catálogos.
- **Dependencias**:
  - `Usuario`, `Rol`, `Permiso`, catálogos de dominio, `Auditoria`.

---

## 4. Reglas de negocio

### 4.1 Estudiantes

- Documento de identidad único por estudiante.
- Estado: ACTIVO/INACTIVO.
- Estudiantes INACTIVOS no pueden ser matriculados.
- No se elimina físicamente un estudiante con historial; se marca como INACTIVO.

### 4.2 Maestros

- Documento de identidad único por maestro.
- Maestro debe tener definida su modalidad de pago antes de asignarle cursos.
- Maestro INACTIVO no puede ser asignado a cursos nuevos.

### 4.3 Cursos

- Cada curso tiene:
  - `precioBase` mayor que 0.
  - `cupoMaximo` mayor que 0.
  - `estado` en {PLANIFICADO, ABIERTO, CERRADO, CANCELADO}.
- Solo cursos en estado ABIERTO aceptan nuevas matrículas.
- El número de matrículas activas no puede exceder `cupoMaximo`.

### 4.4 Matrículas

- Una matrícula vincula un estudiante con un curso.
- La combinación (estudiante, curso) no se puede repetir en estado ACTIVA (regla configurable).
- Estados de matrícula: PENDIENTE, ACTIVA, CANCELADA, FINALIZADA.
- `valorFinal = valorBase - descuento + recargos (si aplican)`. En MVP, recargos pueden omitirse.

### 4.5 Pagos

- Todo pago se registra en `PagoEstudiante` y genera un `MovimientoFinanciero` de tipo INGRESO.
- El monto del pago no puede ser mayor que el saldo pendiente de la matrícula.
- Los pagos no se eliminan, solo se marcan como ANULADOS cuando corresponda.

### 4.6 Descuentos

- Tipos: porcentaje sobre valor base o valor fijo.
- Solo usuarios autorizados pueden aplicar descuentos.
- Descuentos deben estar vigentes en cuanto a fecha y monto máximo permitido.

### 4.7 Saldo pendiente

- `saldoPendiente = valorFinalMatricula - suma(pagosVigentes)`.
- La anulación o creación de pagos modifica el saldo pendiente en tiempo real.

### 4.8 Anulación de pagos

- Solo usuarios con permiso específico pueden anular pagos.
- La anulación se permite únicamente durante una ventana de tiempo o antes de cierre contable (regla configurable).
- La anulación genera:
  - Cambio de estado de PagoEstudiante a ANULADO.
  - Movimiento financiero de reverso (EGRESO o ajuste negativo).
  - Registro en `Auditoria` con detalle.

### 4.9 Generación de nómina

- Se genera para un `PeriodoNomina` definido (fechaInicio, fechaFin).
- Solo se calculan nóminas para maestros activos en el periodo.
- Se consideran horas trabajadas, cursos impartidos e ingresos del curso según la modalidad.
- Estados de Nómina: BORRADOR, APROBADA, PAGADA, ANULADA.

### 4.10 Pago por hora

- Variables:
  - `tarifaHora`: tarifa configurada en `Maestro`.
  - `horasTrabajadasPeriodo`: suma de horas impartidas en el periodo.
- Fórmula:
  - `monto = tarifaHora * horasTrabajadasPeriodo`.

### 4.11 Pago fijo mensual

- Variables:
  - `salarioMensual`: valor configurado en Maestro.
  - `proporcionPeriodo`: proporción si el periodo es menor a un mes (ej. díasPeriodo/díasMes).
- Fórmula:
  - `monto = salarioMensual * proporcionPeriodo`.

### 4.12 Pago por curso

- Variables:
  - `tarifaPorCurso`: valor fijo por curso en Maestro.
  - `cursosImpartidosPeriodo`: número de cursos impartidos por el maestro en el periodo.
- Fórmula:
  - `monto = tarifaPorCurso * cursosImpartidosPeriodo`.

### 4.13 Pago por porcentaje del ingreso del curso

- Variables:
  - `porcentaje`: porcentaje configurado en Maestro.
  - `ingresosCursoPeriodo`: suma de pagos de estudiantes del curso en el periodo (VIGENTES).
- Fórmula:
  - `monto = (porcentaje / 100) * ingresosCursoPeriodo`.

### 4.14 Auditoría

- Se registra usuario, fecha/hora, acción, entidad afectada e identificador.
- Para operaciones críticas se almacenan valores antes y después (por JSON u otro formato texto).
- Acciones mínimas: LOGIN, LOGOUT, CREAR/EDITAR/ANULAR PAGO, CREAR/EDITAR/CANCELAR MATRÍCULA, CAMBIO DE ESTADO EN NÓMINA, CAMBIOS DE USUARIO/ROL.

### 4.15 Permisos

- Permisos por módulo y acción, por ejemplo:
  - `ESTUDIANTE_VER`, `ESTUDIANTE_CREAR`, `ESTUDIANTE_EDITAR`, `ESTUDIANTE_INACTIVAR`.
  - `PAGO_REGISTRAR`, `PAGO_ANULAR`.
  - `NOMINA_CALCULAR`, `NOMINA_APROBAR`, `NOMINA_PAGAR`.
- Los roles agrupan permisos y se asignan a usuarios.
- La capa de servicios valida los permisos para cada operación sensible.

---

## 5. Modelo de dominio

### 5.1 Usuario

- **Propósito**: Representar cuentas de usuario del sistema.
- **Atributos sugeridos**:
  - `id`: identificador interno.
  - `username`: nombre de usuario (único).
  - `passwordHash`: contraseña hasheada.
  - `nombreCompleto`.
  - `email`.
  - `activo`: boolean.
  - `intentosFallidos`.
  - `ultimoAcceso`: fecha/hora.
- **Relaciones**:
  - N:M con `Rol` (tabla `usuario_rol`).
- **Observaciones**:
  - No almacenar contraseñas en texto plano.

### 5.2 Rol

- **Propósito**: Agrupar permisos y definir perfiles de acceso.
- **Atributos**:
  - `id`, `nombre` (único), `descripcion`, `activo`.
- **Relaciones**:
  - N:M con `Permiso` (tabla `rol_permiso`).
  - N:M con `Usuario`.

### 5.3 Permiso

- **Propósito**: Representar una acción concreta habilitable.
- **Atributos**:
  - `id`, `codigo` (por ejemplo: `ESTUDIANTE_CREAR`), `descripcion`, `modulo`.
- **Relaciones**:
  - N:M con `Rol`.

### 5.4 Estudiante

- **Propósito**: Representar a un estudiante de la academia.
- **Atributos**:
  - `id`, `nombre`, `apellido`, `tipoDocumento`, `numeroDocumento`, `telefono`, `email`, `direccion`, `fechaRegistro`, `activo`, `fechaBaja`.
- **Relaciones**:
  - 1:N con `Matricula`.
  - 1:N con `PagoEstudiante` (indirectamente vía `Matricula`).
- **Observaciones**:
  - `numeroDocumento` con restricción única.

### 5.5 Maestro

- **Propósito**: Representar a un profesor/maestro.
- **Atributos**:
  - `id`, `nombre`, `apellido`, `tipoDocumento`, `numeroDocumento`, `telefono`, `email`, `direccion`, `activo`.
  - `tipoPagoProfesor` (FIJO_MENSUAL, POR_HORA, POR_CURSO, PORCENTAJE).
  - `tarifaHora`, `salarioMensual`, `tarifaPorCurso`, `porcentajePorCurso`.
- **Relaciones**:
  - 1:N con `Curso` (maestro principal).
  - 1:N con `NominaProfesor`.
- **Observaciones**:
  - Permite configurar múltiples modalidades, pero deberán validarse reglas para evitar combinaciones inválidas.

### 5.6 Curso

- **Propósito**: Definir la unidad académica.
- **Atributos**:
  - `id`, `nombre`, `descripcion`, `precioBase`, `cupoMaximo`, `estado`, `fechaInicio`, `fechaFin`.
- **Relaciones**:
  - N:1 con `Maestro`.
  - 1:N con `Matricula`.
- **Observaciones**:
  - Índices por `estado` y `fechaInicio` para consultas.

### 5.7 Matricula

- **Propósito**: Inscripción de un estudiante a un curso.
- **Atributos**:
  - `id`, `fecha`, `estado`, `valorBase`, `descuento`, `valorFinal`, `observaciones`.
- **Relaciones**:
  - N:1 con `Estudiante`.
  - N:1 con `Curso`.
  - 1:N con `PagoEstudiante`.
- **Observaciones**:
  - Unique optional constraint sobre (`estudiante_id`, `curso_id`).

### 5.8 PagoEstudiante

- **Propósito**: Registrar pagos y abonos de estudiantes.
- **Atributos**:
  - `id`, `fecha`, `monto`, `metodoPago`, `estado` (VIGENTE, ANULADO), `referencia`, `observaciones`.
- **Relaciones**:
  - N:1 con `Matricula`.
  - 1:1 o N:1 con `MovimientoFinanciero` (vía campo de enlace o mediante `origen/idOrigen`).
- **Observaciones**:
  - Ante anulación se conserva el registro, cambiando el estado.

### 5.9 PeriodoNomina

- **Propósito**: Agrupar nóminas por intervalo de tiempo.
- **Atributos**:
  - `id`, `nombre`, `fechaInicio`, `fechaFin`, `estado` (ABIERTO, CERRADO).
- **Relaciones**:
  - 1:N con `NominaProfesor`.

### 5.10 NominaProfesor

- **Propósito**: Representar la nómina de un profesor para un periodo.
- **Atributos**:
  - `id`, `estado` (BORRADOR, APROBADA, PAGADA, ANULADA), `montoTotal`, `fechaCalculo`, `fechaAprobacion`, `fechaPago`.
- **Relaciones**:
  - N:1 con `Maestro`.
  - N:1 con `PeriodoNomina`.
  - 1:N con `DetalleNominaProfesor`.
  - N:1 con `MovimientoFinanciero` (para pago).

### 5.11 DetalleNominaProfesor

- **Propósito**: Representar cada componente del cálculo de nómina.
- **Atributos**:
  - `id`, `tipoConcepto` (HORAS, CURSO, PORCENTAJE, FIJO, DESCUENTO, BONIFICACION, ANTICIPO),
  - `descripcion`, `cantidad`, `tarifa`, `monto`, `referenciaExterna` (id de curso, sesión, etc.).
- **Relaciones**:
  - N:1 con `NominaProfesor`.

### 5.12 MovimientoFinanciero

- **Propósito**: Registrar cualquier ingreso, egreso o ajuste.
- **Atributos**:
  - `id`, `fecha`, `tipo` (INGRESO, EGRESO, AJUSTE), `monto`, `concepto`,
  - `origen` (PAGO_ESTUDIANTE, NOMINA, OTRO), `idOrigen`, `estado` (VIGENTE, ANULADO).
- **Relaciones**:
  - Referenciado por `PagoEstudiante` o `NominaProfesor` según el origen.

### 5.13 Auditoria

- **Propósito**: Mantener trazabilidad de acciones relevantes.
- **Atributos**:
  - `id`, `fecha`, `usuario`, `accion`, `entidad`, `idEntidad`,
  - `detalleAntes`, `detalleDespues`, `ipMaquina` (opcional).
- **Relaciones**:
  - N:1 con `Usuario`.

---

## 6. Diseño de base de datos

### 6.1 Tablas principales

A modo de resumen, se definen las tablas principales y sus claves:

- `usuario` (`id` PK)
- `rol` (`id` PK)
- `permiso` (`id` PK)
- `usuario_rol` (PK compuesta `usuario_id`, `rol_id`)
- `rol_permiso` (PK compuesta `rol_id`, `permiso_id`)
- `estudiante` (`id` PK)
- `maestro` (`id` PK)
- `curso` (`id` PK)
- `matricula` (`id` PK)
- `pago_estudiante` (`id` PK)
- `periodo_nomina` (`id` PK)
- `nomina_profesor` (`id` PK)
- `detalle_nomina_profesor` (`id` PK)
- `movimiento_financiero` (`id` PK)
- `auditoria` (`id` PK)

### 6.2 Campos y tipos sugeridos (ejemplo PostgreSQL)

Ejemplo parcial:

- `usuario`
  - `id` BIGSERIAL PRIMARY KEY
  - `username` VARCHAR(50) UNIQUE NOT NULL
  - `password_hash` VARCHAR(255) NOT NULL
  - `nombre_completo` VARCHAR(100) NOT NULL
  - `email` VARCHAR(100)
  - `activo` BOOLEAN NOT NULL
  - `intentos_fallidos` INT NOT NULL DEFAULT 0
  - `ultimo_acceso` TIMESTAMP

- `estudiante`
  - `id` BIGSERIAL PRIMARY KEY
  - `nombre` VARCHAR(50) NOT NULL
  - `apellido` VARCHAR(50) NOT NULL
  - `tipo_documento` VARCHAR(20) NOT NULL
  - `numero_documento` VARCHAR(30) NOT NULL UNIQUE
  - `telefono` VARCHAR(20)
  - `email` VARCHAR(100)
  - `direccion` VARCHAR(150)
  - `fecha_registro` DATE NOT NULL
  - `activo` BOOLEAN NOT NULL
  - `fecha_baja` DATE

- `maestro`
  - Campos similares a `estudiante`
  - `tipo_pago_profesor` VARCHAR(20) NOT NULL
  - `tarifa_hora` NUMERIC(12,2)
  - `salario_mensual` NUMERIC(12,2)
  - `tarifa_por_curso` NUMERIC(12,2)
  - `porcentaje_por_curso` NUMERIC(5,2)

- `curso`
  - `id` BIGSERIAL PRIMARY KEY
  - `nombre` VARCHAR(100) NOT NULL
  - `descripcion` TEXT
  - `precio_base` NUMERIC(12,2) NOT NULL
  - `cupo_maximo` INT NOT NULL
  - `estado` VARCHAR(20) NOT NULL
  - `fecha_inicio` DATE NOT NULL
  - `fecha_fin` DATE NOT NULL
  - `maestro_id` BIGINT REFERENCES `maestro`(`id`)

- `matricula`
  - `id` BIGSERIAL PRIMARY KEY
  - `fecha` DATE NOT NULL
  - `estado` VARCHAR(20) NOT NULL
  - `valor_base` NUMERIC(12,2) NOT NULL
  - `descuento` NUMERIC(12,2) NOT NULL DEFAULT 0
  - `valor_final` NUMERIC(12,2) NOT NULL
  - `observaciones` TEXT
  - `estudiante_id` BIGINT NOT NULL REFERENCES `estudiante`(`id`)
  - `curso_id` BIGINT NOT NULL REFERENCES `curso`(`id`)
  - UNIQUE (`estudiante_id`, `curso_id`)

- `pago_estudiante`
  - `id` BIGSERIAL PRIMARY KEY
  - `fecha` TIMESTAMP NOT NULL
  - `monto` NUMERIC(12,2) NOT NULL
  - `metodo_pago` VARCHAR(20) NOT NULL
  - `estado` VARCHAR(20) NOT NULL
  - `referencia` VARCHAR(50)
  - `observaciones` TEXT
  - `matricula_id` BIGINT NOT NULL REFERENCES `matricula`(`id`)

- `periodo_nomina`
  - `id` BIGSERIAL PRIMARY KEY
  - `nombre` VARCHAR(50) NOT NULL
  - `fecha_inicio` DATE NOT NULL
  - `fecha_fin` DATE NOT NULL
  - `estado` VARCHAR(20) NOT NULL

- `nomina_profesor`
  - `id` BIGSERIAL PRIMARY KEY
  - `periodo_nomina_id` BIGINT REFERENCES `periodo_nomina`(`id`)
  - `maestro_id` BIGINT REFERENCES `maestro`(`id`)
  - `estado` VARCHAR(20) NOT NULL
  - `monto_total` NUMERIC(12,2) NOT NULL
  - `fecha_calculo` TIMESTAMP
  - `fecha_aprobacion` TIMESTAMP
  - `fecha_pago` TIMESTAMP
  - `movimiento_financiero_id` BIGINT REFERENCES `movimiento_financiero`(`id`)

- `detalle_nomina_profesor`
  - `id` BIGSERIAL PRIMARY KEY
  - `nomina_profesor_id` BIGINT NOT NULL REFERENCES `nomina_profesor`(`id`)
  - `tipo_concepto` VARCHAR(20) NOT NULL
  - `descripcion` VARCHAR(200)
  - `cantidad` NUMERIC(12,2) NOT NULL
  - `tarifa` NUMERIC(12,2) NOT NULL
  - `monto` NUMERIC(12,2) NOT NULL
  - `referencia_externa` BIGINT

- `movimiento_financiero`
  - `id` BIGSERIAL PRIMARY KEY
  - `fecha` TIMESTAMP NOT NULL
  - `tipo` VARCHAR(20) NOT NULL
  - `monto` NUMERIC(12,2) NOT NULL
  - `concepto` VARCHAR(200) NOT NULL
  - `origen` VARCHAR(20) NOT NULL
  - `id_origen` BIGINT
  - `estado` VARCHAR(20) NOT NULL

- `auditoria`
  - `id` BIGSERIAL PRIMARY KEY
  - `fecha` TIMESTAMP NOT NULL
  - `usuario_id` BIGINT REFERENCES `usuario`(`id`)
  - `accion` VARCHAR(50) NOT NULL
  - `entidad` VARCHAR(50) NOT NULL
  - `id_entidad` BIGINT
  - `detalle_antes` TEXT
  - `detalle_despues` TEXT

### 6.3 Unique constraints, índices y normalización

- `usuario(username)` UNIQUE
- `usuario(email)` UNIQUE (opcional)
- `estudiante(numero_documento)` UNIQUE
- `maestro(numero_documento)` UNIQUE
- `matricula(estudiante_id, curso_id)` UNIQUE
- Índices recomendados:
  - `curso(estado, fecha_inicio)`
  - `pago_estudiante(matricula_id, estado)`
  - `movimiento_financiero(fecha)`
  - `movimiento_financiero(origen, id_origen)`

La base de datos está aproximadamente en 3FN, con entidades bien separadas y evitando redundancias.

### 6.4 Soft delete

Para entidades donde se requiere histórico, se utilizará soft delete:

- `estudiante`: campos `activo`, `fecha_baja`.
- `maestro`: idem.
- Otros: control mediante estados (por ejemplo, `estado` en cursos, matrículas, pagos, nómina).

### 6.5 Auditoría, trazabilidad e integridad

- Auditoría a través de tabla `auditoria`.
- Campos estándar en entidades clave: `fecha_creacion`, `creado_por`, `fecha_modificacion`, `modificado_por` (manejo en capa de servicio).
- FKs y restricciones CHECK para garantizar integridad (por ejemplo, `monto > 0`, tipos válidos).
- Transacciones atómicas para operaciones que involucran múltiples tablas (por ejemplo, pagos + movimientos financieros).

---

## 7. Estructura del proyecto (Maven)

Estructura sugerida de paquetes bajo `src/main/java` para el proyecto `academia_de_belleza`:

```text
org.example.academia
├── config
│   ├── DatabaseConfig.java
│   ├── JpaConfig.java
│   └── AppProperties.java
├── domain
│   ├── entity
│   │   ├── Usuario.java
│   │   ├── Rol.java
│   │   ├── Permiso.java
│   │   ├── Estudiante.java
│   │   ├── Maestro.java
│   │   ├── Curso.java
│   │   ├── Matricula.java
│   │   ├── PagoEstudiante.java
│   │   ├── PeriodoNomina.java
│   │   ├── NominaProfesor.java
│   │   ├── DetalleNominaProfesor.java
│   │   ├── MovimientoFinanciero.java
│   │   └── Auditoria.java
│   ├── enums
│   │   ├── EstadoCurso.java
│   │   ├── EstadoMatricula.java
│   │   ├── EstadoPago.java
│   │   ├── EstadoNomina.java
│   │   ├── TipoMovimiento.java
│   │   └── TipoPagoProfesor.java
│   └── valueobject
│       └── Dinero.java (opcional)
├── repository
│   ├── EstudianteRepository.java
│   ├── MaestroRepository.java
│   ├── CursoRepository.java
│   ├── MatriculaRepository.java
│   ├── PagoEstudianteRepository.java
│   ├── NominaProfesorRepository.java
│   ├── MovimientoFinancieroRepository.java
│   └── UsuarioRepository.java
├── service
│   ├── EstudianteService.java
│   ├── MaestroService.java
│   ├── CursoService.java
│   ├── MatriculaService.java
│   ├── PagoEstudianteService.java
│   ├── NominaService.java
│   ├── UsuarioService.java
│   ├── SeguridadService.java
│   └── nomina
│       ├── PagoProfesorStrategy.java
│       ├── PagoFijoStrategy.java
│       ├── PagoPorHoraStrategy.java
│       ├── PagoPorCursoStrategy.java
│       ├── PagoPorcentajeStrategy.java
│       └── PagoProfesorStrategyFactory.java
├── dto
│   ├── EstudianteDTO.java
│   ├── MaestroDTO.java
│   ├── CursoDTO.java
│   ├── MatriculaDTO.java
│   ├── PagoEstudianteDTO.java
│   ├── NominaProfesorDTO.java
│   └── UsuarioDTO.java
├── mapper
│   ├── EstudianteMapper.java
│   ├── MaestroMapper.java
│   ├── CursoMapper.java
│   ├── MatriculaMapper.java
│   ├── PagoEstudianteMapper.java
│   └── UsuarioMapper.java
├── security
│   ├── PasswordEncoder.java
│   ├── SessionManager.java
│   └── AuthorizationService.java
├── ui
│   ├── MainApp.java
│   ├── controller
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── EstudianteController.java
│   │   ├── MaestroController.java
│   │   ├── CursoController.java
│   │   ├── MatriculaController.java
│   │   ├── CajaController.java
│   │   ├── NominaController.java
│   │   ├── ReportesController.java
│   │   └── AdminController.java
│   └── view (FXML)
│       ├── login.fxml
│       ├── dashboard.fxml
│       ├── estudiantes.fxml
│       ├── maestros.fxml
│       ├── cursos.fxml
│       ├── matriculas.fxml
│       ├── caja.fxml
│       ├── nomina.fxml
│       ├── reportes.fxml
│       └── administracion.fxml
├── reports
│   ├── jasper
│   │   ├── estudiantes.jrxml
│   │   ├── cursos.jrxml
│   │   ├── pagos_estudiante.jrxml
│   │   ├── nomina_periodo.jrxml
│   │   └── ingresos_fecha.jrxml
│   └── JasperReportService.java
└── util
    ├── DateUtils.java
    ├── MoneyUtils.java
    └── ValidationUtils.java
```

En `src/main/resources` se ubicarán:

- Archivos FXML de JavaFX (`/ui/view`).
- Archivos de configuración (`application.properties` o similar).
- Archivos de reportes Jasper (`/reports/jasper`).
- Scripts Flyway (`/db/migration`).

En `src/test/java`, se organizarán pruebas por paquete espejo (`service`, `repository`, etc.).

---

## 8. Diseño de interfaz (JavaFX)

### 8.1 Pantalla de Login

- **Objetivo**: Autenticar al usuario.
- **Componentes**:
  - Campos de texto: usuario, contraseña.
  - Botón "Iniciar sesión".
  - Etiqueta para mensajes de error.
- **Acciones disponibles**:
  - Validar credenciales y abrir el Dashboard.
- **Navegación**:
  - En éxito: ir a `dashboard.fxml`.

### 8.2 Dashboard

- **Objetivo**: Presentar una vista general y acceso a módulos.
- **Componentes**:
  - Menú lateral o barra superior con accesos a Estudiantes, Maestros, Cursos, Matrículas, Caja, Nómina, Reportes, Administración.
  - Tarjetas/resúmenes: número de estudiantes activos, cursos abiertos, pagos del día, etc.
- **Acciones**:
  - Navegar a cada módulo.

### 8.3 Gestión de Estudiantes

- **Objetivo**: CRUD y consulta.
- **Componentes**:
  - Tabla con columnas: nombre, documento, teléfono, estado.
  - Filtros de búsqueda.
  - Botones: Nuevo, Editar, Inactivar, Ver estado de cuenta.
  - Formulario modal o sección lateral para edición.
- **Acciones**:
  - Crear/editar estudiante.
  - Inactivar estudiante.
  - Ver matrículas y pagos asociados.

### 8.4 Gestión de Maestros

- Similar a Estudiantes, incorporando campos de modalidad de pago y asignación de cursos.

### 8.5 Gestión de Cursos

- **Objetivo**: CRUD de cursos.
- **Componentes**:
  - Tabla de cursos (nombre, maestro, estado, fechas, cupo).
  - Filtros (estado, fechas, maestro).
  - Formulario de detalle.
- **Acciones**:
  - Crear/editar curso.
  - Cambiar estado.
  - Ver estudiantes matriculados.

### 8.6 Matrícula

- **Objetivo**: Inscribir estudiantes en cursos.
- **Componentes**:
  - Búsqueda de estudiante.
  - Selección de curso (solo abiertos).
  - Campos de valor base, descuento, valor final.
- **Acciones**:
  - Crear matrícula.
  - Ver historial de matrículas.

### 8.7 Caja / Pagos

- **Objetivo**: Registrar pagos de estudiantes.
- **Componentes**:
  - Búsqueda de estudiante.
  - Tabla de matrículas con saldo.
  - Formulario de pago (monto, método, fecha, observaciones).
- **Acciones**:
  - Registrar pago.
  - Anular pago (con confirmación y permisos).

### 8.8 Nómina

- **Objetivo**: Calcular, aprobar y pagar nómina de profesores.
- **Componentes**:
  - Lista de periodos de nómina.
  - Tabla de `NominaProfesor` por periodo.
  - Panel de detalle (`DetalleNominaProfesor`).
- **Acciones**:
  - Calcular nómina.
  - Recalcular (BORRADOR).
  - Aprobar.
  - Registrar pago (PAGADA).

### 8.9 Reportes

- **Objetivo**: Generar reportes Jasper.
- **Componentes**:
  - Combo de selección de reporte.
  - Panel de filtros (fechas, curso, profesor, estudiante, etc.).
  - Botones: "Ver PDF", "Exportar Excel".

### 8.10 Administración

- **Objetivo**: Configuración global y gestión de usuarios/roles.
- **Componentes**:
  - Tablas de usuarios y roles.
  - Formularios de alta/edición.
  - Configuración de catálogos.

---

## 9. Casos de uso clave

### 9.1 Registrar estudiante

- **Actor**: Usuario con rol ACADEMICO o ADMIN.
- **Precondiciones**:
  - Usuario autenticado.
- **Flujo principal**:
  1. Usuario abre "Gestión de estudiantes".
  2. Hace clic en "Nuevo".
  3. Ingresa datos obligatorios.
  4. El sistema valida unicidad de documento.
  5. El sistema guarda el estudiante como ACTIVO.
  6. El sistema registra auditoría.
- **Flujos alternos**:
  - Documento ya existe → se muestra error y no se guarda.
- **Postcondiciones**:
  - Estudiante disponible para matrículas.

### 9.2 Registrar maestro

- **Actor**: ADMIN.
- **Precondiciones**:
  - Usuario autenticado.
- **Flujo principal**:
  1. Abrir "Gestión de maestros".
  2. "Nuevo" maestro.
  3. Ingresar datos y modalidad de pago.
  4. Validar documento único.
  5. Guardar maestro.
- **Postcondiciones**:
  - Maestro listo para ser asignado a cursos.

### 9.3 Crear curso

- **Actor**: ACADEMICO/ADMIN.
- **Precondiciones**:
  - Usuario autenticado.
- **Flujo principal**:
  1. Abrir "Gestión de cursos".
  2. "Nuevo curso".
  3. Ingresar nombre, fechas, precio, cupo, maestro (opcional en MVP).
  4. Guardar en estado PLANIFICADO.
- **Postcondiciones**:
  - Curso creado y visible en listados.

### 9.4 Asignar maestro a curso

- **Actor**: ACADEMICO/ADMIN.
- **Flujo principal**:
  1. Abrir detalle de un curso.
  2. Seleccionar maestro.
  3. Guardar cambios.
- **Reglas**:
  - No permitir cambios en cursos CERRADOS (o dejar traza en auditoría).

### 9.5 Matricular estudiante

- **Actor**: ACADEMICO/CAJA.
- **Precondiciones**:
  - Estudiante ACTIVO.
  - Curso ABIERTO con cupo disponible.
- **Flujo principal**:
  1. Abrir pantalla de matrícula.
  2. Buscar estudiante.
  3. Seleccionar curso.
  4. Sistema muestra precio base y permite aplicar descuento.
  5. Confirmar matrícula.
- **Flujos alternos**:
  - Curso sin cupo → error.
  - Matrícula duplicada → error.

### 9.6 Registrar pago de estudiante

- **Actor**: CAJA.
- **Precondiciones**:
  - Matricula con saldo pendiente.
- **Flujo principal**:
  1. Abrir "Caja".
  2. Buscar estudiante y seleccionar matrícula.
  3. Ingresar monto, método de pago y fecha.
  4. Validar que el monto ≤ saldo pendiente.
  5. Guardar `PagoEstudiante` y `MovimientoFinanciero` en una transacción.
  6. Actualizar saldo pendiente de matrícula.
- **Flujos alternos**:
  - Monto > saldo → error.

### 9.7 Consultar estado de cuenta

- **Actor**: CAJA/ACADEMICO.
- **Precondiciones**:
  - Estudiante existente.
- **Flujo principal**:
  1. Buscar estudiante.
  2. Ver listado de matrículas con saldos y pagos.

### 9.8 Calcular nómina

- **Actor**: ADMIN/FINANZAS.
- **Precondiciones**:
  - `PeriodoNomina` ABIERTO.
- **Flujo principal**:
  1. Seleccionar periodo de nómina.
  2. Ejecutar acción "Calcular nómina".
  3. Sistema obtiene maestros activos y sus datos (horas, cursos, ingresos según modalidad).
  4. Aplica estrategia de cálculo según `tipoPagoProfesor`.
  5. Crea/actualiza `NominaProfesor` en estado BORRADOR y sus detalles.

### 9.9 Aprobar nómina

- **Actor**: ADMIN/FINANZAS.
- **Precondiciones**:
  - `NominaProfesor` en BORRADOR.
- **Flujo principal**:
  1. Revisar detalle de nómina.
  2. Confirmar aprobación.
  3. Cambiar estado a APROBADA y registrar fecha/usuario.

### 9.10 Pagar profesor

- **Actor**: CAJA/FINANZAS.
- **Precondiciones**:
  - `NominaProfesor` en APROBADA.
- **Flujo principal**:
  1. Seleccionar nómina aprobada.
  2. Confirmar pago.
  3. Crear `MovimientoFinanciero` tipo EGRESO asociado.
  4. Cambiar estado a PAGADA.

### 9.11 Generar reporte financiero

- **Actor**: ADMIN/FINANZAS.
- **Precondiciones**:
  - Movimientos y pagos registrados.
- **Flujo principal**:
  1. Abrir módulo de reportes.
  2. Seleccionar reporte (por ejemplo, "Ingresos por rango de fechas").
  3. Ingresar filtros (fechas, tipo de movimiento).
  4. Generar reporte (vista previa en PDF o exportación a Excel).

---

## 10. Diseño de nómina y lógica financiera

### 10.1 Estrategia de cálculo (Strategy)

Se define una interfaz `PagoProfesorStrategy` con un método, por ejemplo:

- `BigDecimal calcularPago(Maestro maestro, PeriodoNomina periodo, DatosNominaContext contexto);`

Las implementaciones:

- `PagoFijoStrategy`
- `PagoPorHoraStrategy`
- `PagoPorCursoStrategy`
- `PagoPorcentajeStrategy`

Un `PagoProfesorStrategyFactory` elige la implementación adecuada según `maestro.getTipoPagoProfesor()`.

### 10.2 Modalidades de pago y fórmulas

- **Fijo mensual**: `monto = salarioMensual * proporcionPeriodo`.
- **Por hora**: `monto = tarifaHora * horasTrabajadasPeriodo`.
- **Por curso**: `monto = tarifaPorCurso * cursosImpartidosPeriodo`.
- **Por porcentaje**: `monto = (porcentaje / 100) * ingresosCursoPeriodo`.

### 10.3 Validaciones

- Tarifas y porcentajes > 0.
- Horas trabajadas no negativas.
- Ingresos del curso basados en pagos VIGENTES.

### 10.4 Persistencia de cálculos

- Para cada maestro y periodo:
  - Crear o actualizar `NominaProfesor` con `montoTotal` y estado BORRADOR.
  - Crear registros de `DetalleNominaProfesor` para cada componente (horas, cursos, porcentajes, descuentos, bonificaciones).

### 10.5 Evitar pago doble

- Regla en `NominaService`:
  - No permitir pagar (`estado` → PAGADA) si ya existe `NominaProfesor` PAGADA para el mismo maestro y periodo.
  - Comprobar que `movimiento_financiero_id` no esté ya asignado.

### 10.6 Anticipos, descuentos y bonificaciones

- Anticipos se registran como `MovimientoFinanciero` tipo EGRESO y como `DetalleNominaProfesor` con `tipoConcepto = ANTICIPO` y monto negativo.
- Descuentos y bonificaciones se modelan como detalles adicionales con montos negativos o positivos.

### 10.7 Históricos por periodo y estados

- `PeriodoNomina` y `NominaProfesor` no se borran ni se modifican retroactivamente una vez PAGADAS.
- Cualquier corrección se realiza a través de un nuevo periodo o concepto de ajuste.

### 10.8 Movimientos financieros

- Al pagar una nómina:
  - Crear `MovimientoFinanciero` tipo EGRESO con `origen = NOMINA`, `id_origen = nomina.id`.
  - Asociar su `id` en `nomina_profesor.movimiento_financiero_id`.

---

## 11. Seguridad

### 11.1 Login y autenticación

- Formulario de login con usuario y contraseña.
- Validación en `SeguridadService` usando `UsuarioRepository`.
- Contraseñas verificadas con `PasswordEncoder` (por ejemplo, BCrypt).

### 11.2 Hash de contraseñas

- NUNCA almacenar contraseñas en texto plano.
- Al crear usuario: `passwordHash = passwordEncoder.encode(plainPassword)`.
- Al autenticar: `passwordEncoder.matches(plainPassword, passwordHash)`.

### 11.3 Control de sesión

- `SessionManager` mantiene el usuario autenticado actual.
- Métodos:
  - `login(username, password)`.
  - `logout()`.
  - `getCurrentUser()`.

### 11.4 Control por roles y permisos

- `AuthorizationService` expone:
  - `hasPermission(codigoPermiso)`.
  - `requirePermission(codigoPermiso)` (lanza excepción si no tiene permiso).
- La capa de servicios aplica `requirePermission` en operaciones sensibles.

### 11.5 Auditoría de acciones y bitácora

- Cada servicio registra acciones críticas en `Auditoria`.
- Ejemplos:
  - Registro/edición/anulación de pagos.
  - Creación/cancelación de matrículas.
  - Cambio de estado de nómina.
  - Creación/edición de usuarios y roles.

### 11.6 Protección contra eliminación accidental

- No se realiza `DELETE` físico en entidades críticas (pagos, movimientos, nómina, estudiantes, maestros).
- Se usa cambio de estado (por ejemplo, ANULADO, INACTIVO) y creación de movimientos de reverso.

---

## 12. Reportes

Tabla resumen de reportes sugeridos:

| Reporte                      | Filtros                                | Origen de datos                              | Formato recomendado | Exportación |
|------------------------------|-----------------------------------------|----------------------------------------------|---------------------|------------|
| Estudiantes                  | Nombre, documento, estado               | Tabla `estudiante`                           | Detallado           | PDF, Excel |
| Maestros                     | Nombre, documento, estado               | Tabla `maestro`                              | Detallado           | PDF, Excel |
| Cursos                       | Estado, fechas, maestro                 | `curso`, `maestro`                           | Detallado           | PDF, Excel |
| Estudiantes por curso        | Curso, estado matrícula                 | `curso`, `matricula`, `estudiante`           | Listado             | PDF, Excel |
| Pagos por estudiante         | Estudiante, fecha inicio/fin           | `pago_estudiante`, `matricula`, `estudiante` | Detallado           | PDF, Excel |
| Deudas pendientes            | Curso, estudiante, rango de saldo       | `matricula`, `pago_estudiante`               | Listado             | PDF, Excel |
| Pagos a profesores           | Periodo, profesor                       | `nomina_profesor`, `detalle_nomina_profesor` | Resumen             | PDF, Excel |
| Nómina por periodo           | Periodo                                 | `periodo_nomina`, `nomina_profesor`          | Resumen             | PDF        |
| Ingresos por rango de fechas | Fecha inicio/fin, tipo movimiento       | `movimiento_financiero` (INGRESO)            | Agregado            | PDF, Excel |
| Utilidad por curso           | Curso, periodo                          | Ingresos - egresos por curso                 | Agregado            | PDF, Excel |
| Resumen financiero mensual   | Mes/año                                 | `movimiento_financiero`                      | Agregado            | PDF, Excel |

---

## 13. Roadmap de implementación

### Fase 1: Fundaciones y núcleo académico

- **Objetivos**:
  - Configurar proyecto Maven, conexión a BD, migraciones Flyway.
  - Implementar entidades básicas y CRUD de Estudiantes, Maestros y Cursos.
  - Implementar autenticación básica.
- **Módulos incluidos**:
  - Security básico, Estudiantes, Maestros, Cursos.
- **Dependencias**:
  - Configuración JPA/Hibernate, JavaFX base.
- **Riesgos**:
  - Ajustes futuros en el modelo de dominio.
- **Criterio de finalización**:
  - Login funcional y CRUDs operativos contra BD real.

### Fase 2: Matrículas y Pagos de estudiantes

- **Objetivos**:
  - Implementar matrículas y pagos.
  - Gestionar saldos y estado de cuenta.
- **Módulos incluidos**:
  - Matrículas, Pagos estudiantes, MovimientoFinanciero (INGRESOS).
- **Dependencias**:
  - Entidades de Estudiantes, Maestros y Cursos.
- **Riesgos**:
  - Complejidad en cálculo de saldos.
- **Criterio de finalización**:
  - Crear matrícula, registrar pagos, ver saldo correcto.

### Fase 3: Nómina básica

- **Objetivos**:
  - Implementar cálculo de nómina básica para al menos una modalidad.
- **Módulos incluidos**:
  - PeriodoNomina, NominaProfesor, DetalleNominaProfesor, MovimientoFinanciero (EGRESOS).
- **Dependencias**:
  - Datos académicos y de pagos estudiantes.
- **Riesgos**:
  - Errores en fórmulas o falta de datos (horas, ingresos).
- **Criterio de finalización**:
  - Crear periodo, calcular, aprobar y pagar nómina.

### Fase 4: Reportes y seguridad avanzada

- **Objetivos**:
  - Implementar reportes claves y permisos granulares.
- **Módulos incluidos**:
  - Reportes (JasperReports), permisos avanzados, auditoría visible.
- **Dependencias**:
  - Módulos de datos operando correctamente.
- **Riesgos**:
  - Consultas pesadas en reportes.
- **Criterio de finalización**:
  - Reportes operativos, permisos por módulo configurables.

### Fase 5: Funcionalidades avanzadas y mejoras

- **Objetivos**:
  - Añadir modalidades adicionales de pago, descuentos complejos, mejoras de UX.
- **Módulos incluidos**:
  - Estrategias adicionales de nómina, configuración avanzada, posibles integraciones externas.
- **Dependencias**:
  - Fases previas estables.
- **Riesgos**:
  - Sobrecarga de complejidad.
- **Criterio de finalización**:
  - Sistema estable y aceptado por usuarios clave.

---

## 14. Backlog técnico inicial

### Épica: Autenticación y seguridad

1. **Historia**: Configurar JPA, BD y Flyway
   - **Descripción**: Configurar conexión a BD, crear script inicial y activar migraciones Flyway.
   - **Prioridad**: Alta.
   - **Dependencias**: Ninguna.
   - **Criterios de aceptación**: Aplicación se conecta a BD; migración inicial se ejecuta sin errores.
   - **Definición de Terminado (DoD)**: Scripts en `src/main/resources/db/migration`, pruebas de conexión manual.

2. **Historia**: Entidades Usuario/Rol/Permiso y repositorios
   - **Prioridad**: Alta.
   - **Dependencias**: Configuración JPA.
   - **Criterios de aceptación**: CRUD básico de usuarios, roles y permisos con pruebas unitarias.
   - **DoD**: Tests de repositorio con JUnit.

3. **Historia**: Login con hashing de contraseñas
   - **Prioridad**: Alta.
   - **Dependencias**: Entidad Usuario.
   - **Criterios de aceptación**: Usuario válido entra; usuario inválido ve mensaje de error.
   - **DoD**: Pruebas unitarias y validación manual en UI.

### Épica: Estudiantes

1. **Historia**: Crear entidad Estudiante y repositorio
   - **Prioridad**: Alta.
   - **Dependencias**: JPA configurado.
   - **Criterios de aceptación**: CRUD sobre Estudiante operativo.
   - **DoD**: Tests para operaciones básicas.

2. **Historia**: EstudianteService con validación de documento único
   - **Prioridad**: Alta.
   - **Dependencias**: Repositorio de Estudiante.
   - **Criterios de aceptación**: No permite duplicar documentos.

3. **Historia**: Pantalla JavaFX de gestión de estudiantes
   - **Prioridad**: Alta.
   - **Dependencias**: Servicio Estudiante.
   - **Criterios de aceptación**: Alta/edición y listado funcionando.

### Épica: Maestros

- Historias análogas a Estudiantes, incluyendo modalidad de pago.

### Épica: Cursos

1. **Historia**: Entidad Curso y repositorio.
2. **Historia**: CursoService con validación de estado y cupos.
3. **Historia**: UI para CRUD de cursos.

### Épica: Matrículas

1. **Historia**: Entidad Matricula y repositorio.
2. **Historia**: MatriculaService (creación, cancelación, validación de cupos).
3. **Historia**: Pantalla de matrícula.

### Épica: Pagos de estudiantes

1. **Historia**: Entidad PagoEstudiante y MovimientoFinanciero.
2. **Historia**: PagoEstudianteService (registro, anulación, saldo).
3. **Historia**: Pantalla de Caja.

### Épica: Nómina

1. **Historia**: Modelar PeriodoNomina, NominaProfesor, DetalleNominaProfesor.
2. **Historia**: Implementar estrategias de pago (Strategy + Factory).
3. **Historia**: Servicio de cálculo de nómina y UI básica de nómina.

### Épica: Reportes

1. **Historia**: Integrar JasperReports y crear reporte básico de estudiantes.
2. **Historia**: Reporte de pagos de estudiantes.
3. **Historia**: Reporte de nómina por periodo.
4. **Historia**: Reporte de ingresos por rango de fechas.

Cada historia debe incluir siempre:

- Criterios de aceptación claros.
- Definición de Terminado (DoD) con pruebas y revisión de código.

---

## 15. Riesgos técnicos y decisiones críticas

### 15.1 Riesgos de modelado

- Representación insuficiente de escenarios reales (p.ej. varios profesores por curso).
- **Mitigación**: Diseñar el modelo para permitir ampliaciones (por ejemplo, entidades adicionales `CursoMaestro`, `SesionCurso`).

### 15.2 Riesgos de concurrencia

- Modificaciones concurrentes de matrículas, pagos o nómina.
- **Mitigación**: Uso de transacciones y bloqueo optimista (versión en entidades JPA); validación de saldo en BD.

### 15.3 Riesgos de cálculos financieros incorrectos

- Errores en fórmulas de nómina o saldo de estudiantes.
- **Mitigación**: Pruebas unitarias exhaustivas para cálculos; revisión de reglas con usuarios de negocio; doble verificación en ambiente de prueba.

### 15.4 Riesgos de duplicidad de pagos

- Doble clic en botón de pago o repetición de operación.
- **Mitigación**: Deshabilitar botón tras primera acción; clave de idempotencia usando referencia de pago; validaciones en BD y capa de servicio.

### 15.5 Riesgos de integridad referencial

- Borrado de registros con hijos o sin respetar FKs.
- **Mitigación**: Definir FKs estrictas; evitar DELETE físico en datos con relaciones significativas; usar estados lógicos.

### 15.6 Riesgos de UX

- Interfaz confusa para personal administrativo.
- **Mitigación**: Prototipos de pantallas; pilotos con usuarios finales; ajustes iterativos de la UI.

### 15.7 Riesgos de seguridad

- Contraseñas débiles, usuarios compartiendo credenciales.
- **Mitigación**: Política de contraseñas; capacitación básica; log de auditoría para acciones críticas.

### 15.8 Riesgos de escalabilidad futura

- Crecimiento de datos y necesidad de separar backend.
- **Mitigación**: Mantener capas bien separadas; diseño de servicios claramente definidos; considerar futura migración a cliente-servidor si fuera necesario.

---

## 16. MVP recomendado

### 16.1 Funcionalidades obligatorias en MVP

- Login básico con usuarios y roles.
- CRUD de Estudiantes, Maestros y Cursos.
- Gestión de Matrículas con control de cupo y cálculo sencillo del valor final.
- Registro de Pagos de estudiantes y estado de cuenta.
- Registro básico de Movimientos Financieros de ingresos.
- Cálculo simple de Nómina (al menos una modalidad de pago de profesor).
- Reportes básicos: estudiantes, cursos, pagos por estudiante, nómina por periodo.
- Auditoría mínima de operaciones críticas.

### 16.2 Funcionalidades para etapas posteriores

- Descuentos complejos y promociones.
- Todas las modalidades de pago de profesor (porcentaje, fijo mensual detallado, mezcla de conceptos).
- Reportes avanzados (utilidad por curso, resumen financiero mensual detallado).
- Administración granular de permisos por acción.
- Integraciones externas y notificaciones.

---

## 17. Entregables y recomendaciones finales

### 17.1 Entregables concretos

1. **Resumen ejecutivo**: contenido de la sección 1.
2. **Arquitectura propuesta**: sección 2, con capas, patrones y flujo.
3. **Módulos funcionales**: sección 3.
4. **Entidades y relaciones**: secciones 5 y 6 (modelo de dominio y BD).
5. **Diseño de base de datos**: detalle de tablas, claves y restricciones.
6. **Estructura del proyecto**: sección 7 con paquetes y clases ejemplo.
7. **Pantallas**: diseño de UI en sección 8.
8. **Reglas de negocio**: sección 4.
9. **Lógica de nómina**: sección 10.
10. **Roadmap**: sección 13.
11. **Backlog técnico inicial**: sección 14.
12. **Riesgos**: sección 15.
13. **MVP recomendado**: sección 16.

### 17.2 Recomendaciones finales

- Mantener el enfoque en la simplicidad y claridad del MVP antes de añadir complejidad.
- Invertir en pruebas de lógica financiera (saldos, nómina) desde etapas tempranas.
- Documentar claramente supuestos de negocio para evitar ambigüedades (especialmente en descuentos y cálculo de nómina).
- Mantener la estructura de paquetes y naming consistente para facilitar mantenimiento.
- Usar este documento como base viva de arquitectura, actualizándolo al introducir cambios significativos en el sistema.

