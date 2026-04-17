# 📖 GUÍA DE INTEGRACIÓN - KPIs DE CURSOS

## Propósito

Esta guía proporciona instrucciones detalladas para integrar la funcionalidad de **Métricas de Cursos** en tu aplicación Academia de Belleza.

---

## ✅ Pre-requisitos

- [x] Java 17+ instalado
- [x] PostgreSQL ejecutándose (puerto 5432)
- [x] Maven 3.x instalado
- [x] IDE: IntelliJ IDEA (recomendado)
- [x] GitBash o terminal PowerShell

---

## 📋 Paso a Paso

### PASO 1: Crear/Actualizar CursoRepository

**Archivo**: `src/main/java/org/example/academia/repository/CursoRepository.java`

**Contenido**:
```java
package org.example.academia.repository;

import org.example.academia.domain.enums.EstadoCurso;

public interface CursoRepository {
    long countByEstado(EstadoCurso estado);
    long sumCuposDisponibles();
}
```

✅ **Resultado**: Interfaz con 2 métodos de contrato

---

### PASO 2: Crear CursoRepositoryImpl

**Archivo**: `src/main/java/org/example/academia/repository/CursoRepositoryImpl.java`

**Contenido**:
```java
package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.enums.EstadoCurso;

public class CursoRepositoryImpl implements CursoRepository {

    @Override
    public long countByEstado(EstadoCurso estado) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Curso c WHERE c.estado = :estado",
                    Long.class);
            query.setParameter("estado", estado);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long sumCuposDisponibles() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            var query = em.createNativeQuery(
                    "SELECT COALESCE(SUM(c.cupo_maximo - COALESCE(sub.count_activas, 0)), 0) " +
                    "FROM curso c " +
                    "LEFT JOIN (SELECT curso_id, COUNT(*) as count_activas FROM matricula WHERE estado = 'ACTIVA' GROUP BY curso_id) sub " +
                    "ON c.id = sub.curso_id " +
                    "WHERE c.estado = 'ABIERTO'");
            Long result = ((Number) query.getSingleResult()).longValue();
            return result;
        } finally {
            em.close();
        }
    }
}
```

✅ **Resultado**: Implementación completa con JPA queries

---

### PASO 3: Implementar CursoService

**Archivo**: `src/main/java/org/example/academia/service/CursoService.java`

**Contenido**:
```java
package org.example.academia.service;

import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.repository.CursoRepository;
import org.example.academia.repository.CursoRepositoryImpl;

public class CursoService {

    private final CursoRepository cursoRepository = new CursoRepositoryImpl();

    public long getCursosAbiertos() {
        return cursoRepository.countByEstado(EstadoCurso.ABIERTO);
    }

    public long getTotalCuposDisponibles() {
        return cursoRepository.sumCuposDisponibles();
    }
}
```

✅ **Resultado**: Service que expone métodos públicos al controlador

---

### PASO 4: Actualizar DashboardController

**Archivo**: `src/main/java/org/example/academia/ui/controller/DashboardController.java`

**4.1 - Agregar importaciones**:
```java
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.academia.service.CursoService;
```

**4.2 - Agregar campo de servicio** (después de `@FXML private Label usuarioLabel;`):
```java
// Servicios
private final CursoService cursoService = new CursoService();
```

**4.3 - Agregar método handler**:
```java
@FXML
private void onDashboard() {
    displayCourseKPIs();
}
```

**4.4 - Agregar método principal de KPIs**:
```java
private void displayCourseKPIs() {
    try {
        long cursosAbiertos = cursoService.getCursosAbiertos();
        long totalCuposDisponibles = cursoService.getTotalCuposDisponibles();

        VBox kpiContainer = new VBox(20.0);
        kpiContainer.setAlignment(javafx.geometry.Pos.CENTER);
        kpiContainer.setStyle("-fx-padding: 20;");

        Label title = new Label("Métricas de Cursos");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox metricsBox = new HBox(20.0);
        metricsBox.setAlignment(javafx.geometry.Pos.CENTER);

        VBox cursosCard = createMetricCard("Cursos Abiertos", 
            String.valueOf(cursosAbiertos), "#e74c3c");
        metricsBox.getChildren().add(cursosCard);

        VBox cuposCard = createMetricCard("Total Cupos Disponibles", 
            String.valueOf(totalCuposDisponibles), "#9b59b6");
        metricsBox.getChildren().add(cuposCard);

        kpiContainer.getChildren().addAll(title, metricsBox);
        contentPane.getChildren().setAll(kpiContainer);
    } catch (Exception e) {
        e.printStackTrace();
        Label errorLabel = new Label("Error al cargar métricas de cursos");
        contentPane.getChildren().setAll(errorLabel);
    }
}
```

**4.5 - Agregar método helper**:
```java
private VBox createMetricCard(String label, String value, String color) {
    VBox card = new VBox(10.0);
    card.setAlignment(javafx.geometry.Pos.CENTER);
    card.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #ecf0f1;");

    Label labelText = new Label(label);
    labelText.setStyle("-fx-font-size: 14px;");

    Label valueText = new Label(value);
    valueText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

    card.getChildren().addAll(labelText, valueText);
    return card;
}
```

✅ **Resultado**: Controller con 3 nuevos métodos (onDashboard, displayCourseKPIs, createMetricCard)

---

### PASO 5: Actualizar dashboard.fxml

**Archivo**: `src/main/resources/ui/view/dashboard.fxml`

**Ubicación**: Encuentra la sección `<left>` y agrega el botón "Dashboard" como PRIMER botón:

**Antes**:
```xml
<left>
    <VBox spacing="8.0" style="-fx-background-color: #ecf0f1; -fx-padding: 10;">
        <Label text="Módulos" style="-fx-font-weight: bold; -fx-padding: 0 0 5 0;"/>
        <Button text="Estudiantes" onAction="#onEstudiantes" maxWidth="Infinity"/>
        <!-- ... resto de botones ... -->
    </VBox>
</left>
```

**Después**:
```xml
<left>
    <VBox spacing="8.0" style="-fx-background-color: #ecf0f1; -fx-padding: 10;">
        <Label text="Módulos" style="-fx-font-weight: bold; -fx-padding: 0 0 5 0;"/>
        <Button text="Dashboard" onAction="#onDashboard" maxWidth="Infinity"/>
        <Button text="Estudiantes" onAction="#onEstudiantes" maxWidth="Infinity"/>
        <!-- ... resto de botones ... -->
    </VBox>
</left>
```

✅ **Resultado**: Menú con nuevo botón "Dashboard"

---

### PASO 6: Reparar EstudianteService (si está dañado)

**Archivo**: `src/main/java/org/example/academia/service/EstudianteService.java`

**Verificar que tenga esta estructura**:
```java
package org.example.academia.service;

import org.example.academia.repository.EstudianteRepository;
import org.example.academia.repository.EstudianteRepositoryImpl;
import java.time.LocalDate;

public class EstudianteService {
    private final EstudianteRepository estudianteRepository = new EstudianteRepositoryImpl();

    public long getTotalEstudiantesActivos() {
        return estudianteRepository.countByActivoTrue();
    }

    public long getNuevosEstudiantesRegistradosMesActual() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return estudianteRepository.countByFechaRegistroBetween(startOfMonth, endOfMonth);
    }
}
```

✅ **Resultado**: Service con métodos correctamente dentro de la clase

---

### PASO 7: Implementar EstudianteRepositoryImpl

**Archivo**: `src/main/java/org/example/academia/repository/EstudianteRepositoryImpl.java`

**Contenido**:
```java
package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import java.time.LocalDate;

public class EstudianteRepositoryImpl implements EstudianteRepository {

    @Override
    public long countByActivoTrue() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.activo = true",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByFechaRegistroBetween(LocalDate start, LocalDate end) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.fechaRegistro BETWEEN :start AND :end",
                    Long.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
```

✅ **Resultado**: Implementación JPA del repositorio de estudiantes

---

## 🧪 Validación

### Verificar Compilación

```bash
cd C:\Users\Lenovo\IdeaProjects\academia_de_belleza
mvn clean compile
```

**Esperado**: `BUILD SUCCESS`

### Ejecutar Aplicación

```bash
mvn clean javafx:run
```

**Esperado**: 
- Aplicación se abre
- Pantalla de login visible
- Sin errores en console

### Probar Feature

1. **Inicia sesión** con credenciales válidas
2. **Haz clic** en "Dashboard" (menú lateral izquierdo)
3. **Debería mostrar**:
   - Título: "Métricas de Cursos"
   - Tarjeta 1: "Cursos Abiertos" con número en rojo
   - Tarjeta 2: "Total Cupos Disponibles" con número en púrpura

---

## 🐛 Troubleshooting

### Error: "Cannot resolve symbol 'CursoService'"
**Solución**: Verifica que `CursoService.java` esté en `src/main/java/org/example/academia/service/`

### Error: "FXMLLoader.load() cannot resolve"
**Solución**: Verifica que `DashboardController` tenga el método `onDashboard()` con anotación `@FXML`

### Error: "SELECT COUNT from Curso" fails
**Solución**: 
1. Verifica que PostgreSQL esté ejecutándose
2. Verifica que la tabla `curso` exista: `SELECT * FROM curso;`
3. Verifica enum `EstadoCurso` tenga valor `ABIERTO`

### Botón "Dashboard" no aparece en menú
**Solución**: 
1. Verifica que editaste `dashboard.fxml` correctamente
2. Reconstruye: `mvn clean javafx:run`

### Las métricas muestran "Error al cargar..."
**Solución**:
1. Abre console y busca exception
2. Verifica que `DatabaseConfig.createEntityManager()` esté disponible
3. Verifica credenciales de PostgreSQL

---

## 📊 Ejemplo de Datos

### Si la BD tiene:

**Tabla curso**:
```
id | nombre      | estado  | cupo_maximo
1  | Masaje Spa  | ABIERTO | 20
2  | Pedicura    | ABIERTO | 15
3  | Manicura    | CERRADO | 10
4  | Faciales    | ABIERTO | 30
```

**Tabla matricula** (filtrando por estado='ACTIVA'):
```
id | curso_id | estado | ...
1  | 1        | ACTIVA | ...
2  | 1        | ACTIVA | ...
3  | 2        | ACTIVA | ...
4  | 4        | ACTIVA | ...
5  | 4        | ACTIVA | ...
```

### El Dashboard mostrará:

```
Cursos Abiertos: 3
└─ (id 1, 2, 4 tienen estado='ABIERTO')

Total Cupos Disponibles: 53
└─ Curso 1: 20 - 2 = 18
└─ Curso 2: 15 - 1 = 14
└─ Curso 4: 30 - 2 = 28
└─ Total: 18 + 14 + 28 = 60
└─ Nota: El SQL usa LEFT JOIN para cursos sin matriculas
```

---

## ✅ Checklist de Finalización

- [ ] Archivo `CursoRepository.java` creado/actualizado
- [ ] Archivo `CursoRepositoryImpl.java` creado
- [ ] Archivo `CursoService.java` implementado
- [ ] `DashboardController.java` actualizado con 3 métodos
- [ ] `dashboard.fxml` actualizado con botón
- [ ] `EstudianteService.java` reparado
- [ ] `EstudianteRepositoryImpl.java` implementado
- [ ] Compilación exitosa: `mvn clean compile`
- [ ] Aplicación ejecuta: `mvn clean javafx:run`
- [ ] Feature funciona: Botón Dashboard muestra métricas
- [ ] Otras funcionalidades siguen trabajando

---

## 📞 Soporte

Si necesitas ayuda:
1. Revisa `KPI_CURSOS_README.md` para arquitectura completa
2. Revisa `REFERENCIA_CODIGO.md` para código de referencia
3. Busca en `AGENTS.md` sección de troubleshooting

---

**Fecha**: 2026-04-12  
**Versión**: 1.0  
**Status**: ✅ Listo para usar

