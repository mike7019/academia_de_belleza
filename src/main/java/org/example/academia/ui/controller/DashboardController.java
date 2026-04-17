package org.example.academia.ui.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.repository.CursoRepositoryImpl;
import org.example.academia.repository.EstudianteRepositoryImpl;
import org.example.academia.security.SessionManager;
import org.example.academia.service.CursoService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador del Dashboard principal (limpio y centralizado para el módulo dashboard).
 */
public class DashboardController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label cursosAbiertosLabel;

    @FXML
    private Label cuposDisponiblesLabel;

    @FXML
    private ListView<String> recentStudentsList;

    @FXML
    private void initialize() {
        if (usuarioLabel != null && SessionManager.getInstance().getCurrentUser() != null) {
            usuarioLabel.setText("Usuario: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }

        Platform.runLater(() -> {
            showDashboardKpis();

            try {
                if (contentPane != null) {
                    Scene s = contentPane.getScene();
                    if (s != null) {
                        java.net.URL cssUrl = getClass().getResource("/ui/styles/dashboard.css");
                        if (cssUrl != null) s.getStylesheets().add(cssUrl.toExternalForm());
                    } else {
                        contentPane.sceneProperty().addListener((obs, oldS, newS) -> {
                            if (newS != null) {
                                java.net.URL cssUrl = getClass().getResource("/ui/styles/dashboard.css");
                                if (cssUrl != null) newS.getStylesheets().add(cssUrl.toExternalForm());
                            }
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    @FXML
    private void onDashboard() {
        showDashboardKpis();
    }

    // ===================== KPI DASHBOARD =====================

    private void showDashboardKpis() {
        if (contentPane == null) return;

        // --- Obtener datos KPI ---
        long estudiantesActivos = 0;
        long cursosAbiertos = 0;
        long cuposDisponibles = 0;
        BigDecimal pagosDelDia = BigDecimal.ZERO;
        long matriculasActivas = 0;
        long estudiantesNuevosMes = 0;

        try {
            var estRepo = new EstudianteRepositoryImpl();
            estudiantesActivos = estRepo.countByActivoTrue();
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            estudiantesNuevosMes = estRepo.countByFechaRegistroBetween(inicioMes, LocalDate.now());
        } catch (Exception ex) { ex.printStackTrace(); }

        try {
            CursoService cursoService = new CursoService(new CursoRepositoryImpl());
            List<Curso> abiertos = cursoService.getCursosAbiertos();
            cursosAbiertos = abiertos.size();
            cuposDisponibles = cursoService.getTotalCuposDisponibles();
        } catch (Exception ex) { ex.printStackTrace(); }

        try {
            EntityManager em = DatabaseConfig.createEntityManager();
            try {
                // Pagos VIGENTES del día
                LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
                LocalDateTime finDia = inicioDia.plusDays(1);
                TypedQuery<BigDecimal> q = em.createQuery(
                        "SELECT COALESCE(SUM(p.monto), 0) FROM PagoEstudiante p WHERE p.estado = 'VIGENTE' AND p.fecha >= :inicio AND p.fecha < :fin",
                        BigDecimal.class);
                q.setParameter("inicio", inicioDia);
                q.setParameter("fin", finDia);
                pagosDelDia = q.getSingleResult();

                // Matrículas activas
                TypedQuery<Long> qm = em.createQuery(
                        "SELECT COUNT(m) FROM Matricula m WHERE m.estado = 'ACTIVA'", Long.class);
                matriculasActivas = qm.getSingleResult();
            } finally {
                em.close();
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        // Actualizar labels laterales si existen
        if (cursosAbiertosLabel != null) cursosAbiertosLabel.setText(String.valueOf(cursosAbiertos));
        if (cuposDisponiblesLabel != null) cuposDisponiblesLabel.setText(String.valueOf(cuposDisponibles));

        // --- Construir UI de KPIs ---
        VBox root = new VBox(16);
        root.getStyleClass().add("content-card");
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setFillWidth(true);
        root.setPadding(new Insets(24));

        Label title = new Label("📊 Dashboard — Indicadores Clave");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Resumen en tiempo real del sistema de la Academia de Belleza");
        subtitle.getStyleClass().add("content-subtitle");
        subtitle.setWrapText(true);

        // Grid de tarjetas KPI (3 columnas x 2 filas)
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setMaxWidth(Double.MAX_VALUE);
        // 3 columnas iguales
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(createKpiCard("👩‍🎓", "Estudiantes Activos", String.valueOf(estudiantesActivos), "#2563eb"), 0, 0);
        grid.add(createKpiCard("📚", "Cursos Abiertos", String.valueOf(cursosAbiertos), "#059669"), 1, 0);
        grid.add(createKpiCard("💳", "Pagos del Día", "$" + pagosDelDia.toPlainString(), "#d97706"), 2, 0);
        grid.add(createKpiCard("📝", "Matrículas Activas", String.valueOf(matriculasActivas), "#7c3aed"), 0, 1);
        grid.add(createKpiCard("🪑", "Cupos Disponibles", String.valueOf(cuposDisponibles), "#0891b2"), 1, 1);
        grid.add(createKpiCard("🆕", "Nuevos Este Mes", String.valueOf(estudiantesNuevosMes), "#e11d48"), 2, 1);

        // Lista de estudiantes recientes
        Label recentTitle = new Label("Últimos estudiantes registrados");
        recentTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-padding: 8 0 0 0;");

        ListView<String> recentList = new ListView<>();
        recentList.setPrefHeight(140);
        recentList.setMaxHeight(160);
        recentList.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        loadRecentStudentsInto(recentList);

        root.getChildren().addAll(title, subtitle, grid, recentTitle, recentList);
        VBox.setVgrow(grid, Priority.ALWAYS);

        setContent(root);
    }

    private VBox createKpiCard(String icon, String label, String value, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + color + "22;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 28px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        nameLbl.setWrapText(true);

        card.getChildren().addAll(iconLbl, valueLbl, nameLbl);

        // Hover animation
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.04); st.setToY(1.04); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        return card;
    }

    private void loadRecentStudentsInto(ListView<String> list) {
        try {
            EntityManager em = DatabaseConfig.createEntityManager();
            try {
                TypedQuery<Estudiante> query = em.createQuery(
                        "SELECT e FROM Estudiante e ORDER BY e.fechaRegistro DESC", Estudiante.class);
                query.setMaxResults(6);
                List<Estudiante> recientes = query.getResultList();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Estudiante s : recientes) {
                    String name = (s.getNombre() != null ? s.getNombre() : "") + " " + (s.getApellido() != null ? s.getApellido() : "");
                    String date = s.getFechaRegistro() != null ? s.getFechaRegistro().format(fmt) : "";
                    list.getItems().add(name.trim() + " — " + date);
                }
            } finally {
                em.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ===================== NAVEGACIÓN =====================

    @FXML
    private void onEstudiantes() {
        loadView("/ui/view/estudiantes.fxml");
    }

    @FXML
    private void onCursos() {
        loadView("/ui/view/cursos.fxml");
    }

    @FXML
    private void onMaestros() {
        loadView("/ui/view/maestros.fxml");
    }

    @FXML
    private void onMatriculas() {
        loadView("/ui/view/matriculas.fxml");
    }

    @FXML
    private void onPagos() {
        loadView("/ui/view/caja.fxml");
    }

    @FXML
    private void onNomina() {
        loadView("/ui/view/nomina.fxml");
    }

    @FXML
    private void onReportes() {
        loadView("/ui/view/reportes.fxml");
    }

    @FXML
    private void onLogout() {
        try {
            SessionManager.getInstance().logout();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/login.fxml")).load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setTitle("Academia de Belleza");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            if (contentPane != null) contentPane.getChildren().setAll(new Label("Error al cerrar sesión"));
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (contentPane != null) {
                setContent(view);
            } else {
                System.err.println("Warning: contentPane is null, cannot set view: " + fxmlPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (contentPane != null) contentPane.getChildren().setAll(new Label("No se pudo cargar la vista: " + fxmlPath));
        }
    }

    private void setContent(Node node) {
        if (contentPane == null || node == null) return;
        node.setOpacity(0);
        contentPane.getChildren().setAll(node);
        if (node instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            region.prefWidthProperty().bind(contentPane.widthProperty());
            region.prefHeightProperty().bind(contentPane.heightProperty());
        }
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    @FXML
    private void onKpiCursosClicked(MouseEvent event) {
        try {
            CursoService cursoService = new CursoService(new CursoRepositoryImpl());
            List<Curso> abiertos = cursoService.getCursosAbiertos();
            ListView<String> listView = new ListView<>();
            abiertos.stream().map(Curso::getNombre).forEach(listView.getItems()::add);
            listView.setPrefHeight(300);
            VBox card = new VBox(10, new Label("Cursos abiertos"), listView);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-radius: 8; -fx-background-radius: 8;");
            if (contentPane != null) contentPane.getChildren().setAll(card);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onKpiCuposClicked(MouseEvent event) {
        try {
            if (cuposDisponiblesLabel != null) {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), cuposDisponiblesLabel);
                st.setFromX(1.0);
                st.setFromY(1.0);
                st.setToX(1.12);
                st.setToY(1.12);
                st.setAutoReverse(true);
                st.setCycleCount(2);
                st.play();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
