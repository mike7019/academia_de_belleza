# USER STORY 117: Consultar Matrículas

## 📋 DESCRIPCIÓN

Como usuario, quiero visualizar y filtrar matrículas para consultar información de inscripciones.

## ✅ CRITERIOS DE ACEPTACIÓN

1. ✅ Se listan todas las matrículas
2. ✅ Filtros por ESTADO (Todos, PENDIENTE, ACTIVA, CANCELADA, FINALIZADA)
3. ✅ Filtros por ESTUDIANTE
4. ✅ Filtros por CURSO
5. ✅ Se muestra ESTADO de cada matrícula
6. ✅ Se muestra VALORES (base, descuento, final)
7. ✅ Se muestra información del ESTUDIANTE (nombre, documento)
8. ✅ Se muestra información del CURSO

## 🔄 IMPLEMENTACIÓN

### Cambios en el Código

#### 1. MatriculaController.java

**Nuevos campos @FXML:**
```java
@FXML
private ComboBox<EstudianteDTO> filtroEstudianteCombo;

@FXML
private ComboBox<CursoDTO> filtroCursoCombo;
```

**Nuevos métodos:**
```java
@FXML
private void onLimpiarFiltros()           // Limpiar todos los filtros
    
private void cargarCombosDeEstudiantes()  // Cargar estudiantes para filtro
    
private void cargarCombosDeCursos()       // Cargar cursos para filtro
```

**Actualización de initialize():**
- Ahora carga los combos de filtros
- Agrega listeners para recargar tabla cuando cambian filtros

**Actualización de cargarMatriculas():**
- Ahora obtiene valores de los 3 combos de filtro
- Pasa estudianteId, cursoId y estado al servicio

#### 2. matriculas.fxml

**Nuevo bloque de filtros:**
```xml
<HBox spacing="10.0" alignment="CENTER_LEFT">
    <Label text="Estado:"/>
    <ComboBox fx:id="filtroEstadoCombo" prefWidth="120"/>
    <Label text="Estudiante:"/>
    <ComboBox fx:id="filtroEstudianteCombo" prefWidth="150"/>
    <Label text="Curso:"/>
    <ComboBox fx:id="filtroCursoCombo" prefWidth="150"/>
    <Button text="Limpiar" onAction="#onLimpiarFiltros"/>
</HBox>
```

### Flujo de Consulta

```
initialize()
  ├─ Cargar combo de estados
  ├─ Cargar combo de estudiantes
  ├─ Cargar combo de cursos
  └─ cargarMatriculas(0)
       ├─ Obtener filtroEstado
       ├─ Obtener filtroEstudiante
       ├─ Obtener filtroCurso
       ├─ Llamar: matriculaService.listarMatriculas(est, cur, estado, null, null)
       ├─ Llenar tabla
       └─ Actualizar paginación

Usuario cambia filtro:
  └─ Listener dispara cargarMatriculas(0)
```

## 🎨 INTERFAZ USUARIO

### Filtros Disponibles

```
┌────────────────────────────────────────────────────────┐
│ Gestión de Matrículas                                  │
├────────────────────────────────────────────────────────┤
│ [Nueva] [Ver] [Cancelar] [Refrescar]                   │
│                                                         │
│ Estado: [Todos ▼]  Estudiante: [Todos ▼]              │
│ Curso: [Todos ▼]   [Limpiar]                           │
├────────────────────────────────────────────────────────┤
│ Estudiante | Doc. | Curso | Fecha | Estado | V.Base...│
├────────────────────────────────────────────────────────┤
│ (tabla con resultados filtrados)                       │
└────────────────────────────────────────────────────────┘
```

### Información Mostrada en Tabla

| Columna | Origen | Tipo |
|---------|--------|------|
| Estudiante | MatriculaDTO.estudianteNombre | String |
| Documento | MatriculaDTO.estudianteDocumento | String |
| Curso | MatriculaDTO.cursoNombre | String |
| Fecha | MatriculaDTO.fecha | LocalDate |
| Estado | MatriculaDTO.estado | EstadoMatricula |
| Valor Base | MatriculaDTO.valorBase | BigDecimal |
| Descuento | MatriculaDTO.descuento | BigDecimal |
| Valor Final | MatriculaDTO.valorFinal | BigDecimal |

## 💾 SERVICIO (REUTILIZADO)

**MatriculaService.listarMatriculas()**
```java
public List<MatriculaDTO> listarMatriculas(
    Long estudianteId,          // null = todos
    Long cursoId,               // null = todos
    EstadoMatricula estado,     // null = todos
    LocalDate fechaDesde,       // null = no usar
    LocalDate fechaHasta        // null = no usar
)
```

Requiere permiso: `MATRICULA_VER`

## 🔍 CASOS DE USO

### Caso 1: Ver todas las matrículas
```
1. Iniciar aplicación
2. Ir a módulo Matrículas
3. Todos los filtros en "Todos"
4. Sistema muestra todas las matrículas (máx 15 por página)
```

### Caso 2: Filtrar por estudiante
```
1. Combo "Estudiante": Seleccionar "Juan Pérez"
2. Tabla refresca automáticamente
3. Muestra solo matrículas de Juan Pérez
```

### Caso 3: Filtrar por curso
```
1. Combo "Curso": Seleccionar "Corte Básico"
2. Tabla refresca automáticamente
3. Muestra solo matrículas del curso Corte Básico
```

### Caso 4: Filtrar por estado
```
1. Combo "Estado": Seleccionar "ACTIVA"
2. Tabla refresca automáticamente
3. Muestra solo matrículas activas
```

### Caso 5: Combinación de filtros
```
1. Estado: "ACTIVA"
2. Estudiante: "Ana Gómez"
3. Curso: "Maquillaje"
4. Tabla muestra: matrículas activas de Ana en Maquillaje
```

### Caso 6: Limpiar filtros
```
1. Modificar varios filtros
2. Click "Limpiar"
3. Todos los combos vuelven a "Todos"
4. Tabla muestra todas las matrículas
```

## 📊 EJEMPLOS DE DATOS

```
Matrícula 1:
  Estudiante: Juan Pérez (CC: 100000001)
  Curso: Corte Básico
  Fecha: 2026-05-04
  Estado: ACTIVA
  Valor Base: 800000.00
  Descuento: 100000.00
  Valor Final: 700000.00

Matrícula 2:
  Estudiante: Ana Gómez (CC: 100000002)
  Curso: Maquillaje Profesional
  Fecha: 2026-05-04
  Estado: PENDIENTE
  Valor Base: 900000.00
  Descuento: 0.00
  Valor Final: 900000.00

Matrícula 3:
  Estudiante: Juan Pérez (CC: 100000001)
  Curso: Maquillaje Profesional
  Fecha: 2026-05-03
  Estado: CANCELADA
  Valor Base: 900000.00
  Descuento: 0.00
  Valor Final: 900000.00
```

## 🧪 TEST CASES

### Test 1: Cargar tabla vacía/llena
**Setup:** No hay filtros activos
**Resultado esperado:**
- ✅ Tabla carga todas las matrículas
- ✅ Paginación correcta
- ✅ Resumen muestra "Mostrando 1-X de Y"

### Test 2: Filtrar por estado ACTIVA
**Setup:** Combo Estado = "ACTIVA"
**Resultado esperado:**
- ✅ Tabla solo muestra ACTIVA
- ✅ Otros estados no aparecen
- ✅ Datos son exactos

### Test 3: Filtrar por estudiante
**Setup:** Combo Estudiante = "Juan Pérez"
**Resultado esperado:**
- ✅ Tabla solo muestra matrículas de Juan
- ✅ Otros estudiantes no aparecen

### Test 4: Filtrar por curso
**Setup:** Combo Curso = "Corte Básico"
**Resultado esperado:**
- ✅ Tabla solo muestra matrículas de Corte
- ✅ Otros cursos no aparecen

### Test 5: Combinación de filtros
**Setup:** Estado = ACTIVA, Estudiante = Juan, Curso = Corte
**Resultado esperado:**
- ✅ Intersección correcta de filtros
- ✅ Solo resultados que cumplen los 3 criterios

### Test 6: Limpiar filtros
**Setup:** Todos los filtros configurados
**Resultado esperado:**
- ✅ Click "Limpiar"
- ✅ Todos combos vuelven a valor inicial
- ✅ Tabla refresca con todos los registros

### Test 7: Paginación con filtro
**Setup:** Filtro que retorna 45 matrículas
**Resultado esperado:**
- ✅ Página 1: registros 1-15
- ✅ Página 2: registros 16-30
- ✅ Página 3: registros 31-45
- ✅ Resumen actualizado

### Test 8: Valores mostrados correctamente
**Setup:** Abrir fila de matrícula
**Resultado esperado:**
- ✅ Valor Base = Precio del curso
- ✅ Descuento = Valor descontado
- ✅ Valor Final = Base - Descuento
- ✅ Todos en formato decimal (12,2)

## 🔐 SEGURIDAD

- ✅ Requiere permiso: `MATRICULA_VER`
- ✅ Sin autorización: AuthException
- ✅ Filtros no alteran validaciones

## 🔄 RELACIÓN CON OTRAS HISTORIAS

- **US 115 (Crear):** Las matrículas creadas aparecen en tabla
- **US 116 (Cancelar):** Las canceladas cambian de estado
- **US 117 (Consultar):** ← Está aquí
- **US 118 (Reportes):** Usa datos de consulta

## 📈 MEJORAS FUTURAS

- Exportar a Excel
- Búsqueda por texto libre
- Ordenamiento por columnas
- Rango de fechas
- Gráficos de distribución por estado
- Reportes PDF

## ✨ CARACTERÍSTICAS

- ✅ Filtros en tiempo real (sin lag)
- ✅ Combos cargados automáticamente
- ✅ Opción "Todos" por defecto
- ✅ Paginación eficiente (15 x página)
- ✅ Visualización clara de valores
- ✅ Estados con colores (futuro)

---

**Status:** ✅ IMPLEMENTADO  
**Fecha:** 2026-05-04  
**Versión:** 1.0

