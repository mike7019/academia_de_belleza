package org.example.academia.ui.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.repository.CursoRepositoryImpl;
import org.example.academia.security.SessionManager;
import org.example.academia.service.CursoService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.IOException;
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
        // Mostrar usuario actual si está disponible en la sesión
        if (usuarioLabel != null && SessionManager.getInstance().getCurrentUser() != null) {
            usuarioLabel.setText("Usuario: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }

        // Ejecutar carga inicial en el hilo de UI para evitar problemas de timing
        Platform.runLater(() -> {
            // Cargar vista por defecto (Estudiantes)
            loadView("/ui/view/estudiantes.fxml");
            // Actualizar KPIs y lista reciente
            loadKpiCursos();
            loadRecentStudents();

            // Aplicar stylesheet del dashboard a la escena si está disponible
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

    private void loadKpiCursos() {
        try {
            CursoService cursoService = new CursoService(new CursoRepositoryImpl());
            List<Curso> abiertos = cursoService.getCursosAbiertos();
            long totalCupos = cursoService.getTotalCuposDisponibles();

            if (cursosAbiertosLabel != null) {
                cursosAbiertosLabel.setText(String.valueOf(abiertos.size()));
            }
            if (cuposDisponiblesLabel != null) {
                cuposDisponiblesLabel.setText(String.valueOf(totalCupos));
                ScaleTransition st = new ScaleTransition(Duration.millis(400), cuposDisponiblesLabel);
                st.setFromX(0.9);
                st.setFromY(0.9);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadRecentStudents() {
        try {
            if (recentStudentsList == null) return;
            EntityManager em = DatabaseConfig.createEntityManager();
            try {
                TypedQuery<Estudiante> query = em.createQuery("SELECT e FROM Estudiante e ORDER BY e.fechaRegistro DESC", Estudiante.class);
                query.setMaxResults(6);
                List<Estudiante> recientes = query.getResultList();
                recentStudentsList.getItems().clear();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Estudiante s : recientes) {
                    String name = (s.getNombre() != null ? s.getNombre() : "") + " " + (s.getApellido() != null ? s.getApellido() : "");
                    String date = s.getFechaRegistro() != null ? s.getFechaRegistro().format(fmt) : "";
                    recentStudentsList.getItems().add(name + " — " + date);
                }
            } finally {
                em.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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




