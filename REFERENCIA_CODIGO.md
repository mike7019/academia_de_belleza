# REFERENCIA RÁPIDA - Código Final KPIs de Cursos

## 1️⃣ CursoRepository.java
```java
package org.example.academia.repository;

import org.example.academia.domain.enums.EstadoCurso;

public interface CursoRepository {
    long countByEstado(EstadoCurso estado);
    long sumCuposDisponibles();
}
```

## 2️⃣ CursoRepositoryImpl.java
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

## 3️⃣ CursoService.java
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

## 4️⃣ DashboardController.java - Métodos Nuevos
```java
// Agregar estas importaciones
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.academia.service.CursoService;

// Agregar este campo
private final CursoService cursoService = new CursoService();

// Agregar estos métodos
@FXML
private void onDashboard() {
    displayCourseKPIs();
}

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

        VBox cursosCard = createMetricCard("Cursos Abiertos", String.valueOf(cursosAbiertos), "#e74c3c");
        metricsBox.getChildren().add(cursosCard);

        VBox cuposCard = createMetricCard("Total Cupos Disponibles", String.valueOf(totalCuposDisponibles), "#9b59b6");
        metricsBox.getChildren().add(cuposCard);

        kpiContainer.getChildren().addAll(title, metricsBox);
        contentPane.getChildren().setAll(kpiContainer);
    } catch (Exception e) {
        e.printStackTrace();
        Label errorLabel = new Label("Error al cargar métricas de cursos");
        contentPane.getChildren().setAll(errorLabel);
    }
}

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

## 5️⃣ dashboard.fxml - Cambio en Menú Lateral
```xml
<left>
    <VBox spacing="8.0" style="-fx-background-color: #ecf0f1; -fx-padding: 10;">
        <Label text="Módulos" style="-fx-font-weight: bold; -fx-padding: 0 0 5 0;"/>
        <Button text="Dashboard" onAction="#onDashboard" maxWidth="Infinity"/>
        <Button text="Estudiantes" onAction="#onEstudiantes" maxWidth="Infinity"/>
        <Button text="Maestros" onAction="#onMaestros" maxWidth="Infinity"/>
        <Button text="Cursos" onAction="#onCursos" maxWidth="Infinity"/>
        <Button text="Matrículas" onAction="#onMatriculas" maxWidth="Infinity"/>
        <Button text="Pagos" onAction="#onPagos" maxWidth="Infinity"/>
        <Button text="Nómina" onAction="#onNomina" maxWidth="Infinity"/>
        <Button text="Reportes" onAction="#onReportes" maxWidth="Infinity"/>
    </VBox>
</left>
```

## 6️⃣ EstudianteService.java - Reparado
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

## 7️⃣ EstudianteRepositoryImpl.java - Implementado
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

---

## 📝 Notas de Integración

- Todos los archivos usan **EntityManager directo** (patrón consistente)
- PostgreSQL compatible con las queries
- Sin dependencias nuevas (usa solo JPA/Hibernate existente)
- Compila correctamente con warnings de best practices (ignorables)

