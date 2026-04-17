package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.academia.security.SessionManager;
import org.example.academia.repository.CursoRepositoryImpl;
import org.example.academia.service.CursoService;
import org.example.academia.domain.entity.Curso;

import java.io.IOException;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Estudiante;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import javafx.scene.control.ListView;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Controlador del Dashboard principal.
 * <p>
 * Muestra el menú de módulos y carga en la región central las vistas FXML
 * correspondientes a Estudiantes, Cursos, Matrículas, Pagos, Nómina y Reportes.
 * No contiene lógica de negocio; solo coordina navegación de UI.
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

        // Para evitar problemas de inyección o timing al cargar vistas
        // (en algunos entornos el ContentPane puede no estar listo exactamente
        // en el momento de initialize), ejecutamos las cargas en el hilo de UI
        // posterior.
        Platform.runLater(() -> {
            try {
                // Cargar vista por defecto (Estudiantes) al abrir el Dashboard
                loadView("/ui/view/estudiantes.fxml");

                // Cargar KPIs de cursos (lista de cursos abiertos y cupos disponibles)
                loadKpiCursos();
                // Cargar lista de estudiantes recientes en el header
                loadRecentStudents();

                // Asegurar que la hoja de estilos del dashboard esté presente en la escena
                try {
                    if (contentPane != null && contentPane.getScene() != null) {
                        java.net.URL cssUrl = getClass().getResource("/ui/styles/dashboard.css");
                        if (cssUrl != null) {
                            String css = cssUrl.toExternalForm();
                            if (!contentPane.getScene().getStylesheets().contains(css)) {
                                contentPane.getScene().getStylesheets().add(css);
                            }
                        }
                    } else if (contentPane != null) {
                        // si la escena aún no está lista, escuchar cuando esté disponible
                        contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                            if (newScene != null) {
                                try {
                                    java.net.URL cssUrl = getClass().getResource("/ui/styles/dashboard.css");
                                    if (cssUrl != null) {
                                        String css = cssUrl.toExternalForm();
                                        if (!newScene.getStylesheets().contains(css)) {
                                            newScene.getStylesheets().add(css);
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    // No bloquear la inicialización por temas de estilos
                    System.err.println("No se pudo aplicar stylesheet del dashboard: " + ex.getMessage());
                }
            } catch (Exception ex) {
                System.err.println("Error inicializando Dashboard: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }
    /**
     * Carga los estudiantes más recientes en la lista del header.
     */
    private void loadRecentStudents() {
        try {
            if (recentStudentsList == null) return;

            EntityManager em = DatabaseConfig.createEntityManager();
            try {
                TypedQuery<Estudiante> query = em.createQuery("SELECT e FROM Estudiante e ORDER BY e.fechaRegistro DESC", Estudiante.class);
                query.setMaxResults(6);
                java.util.List<Estudiante> recientes = query.getResultList();

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
            System.err.println("Error cargando estudiantes recientes: " + ex.getMessage());
        }
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
        // Limpiar usuario en sesión y volver a la pantalla de login
        SessionManager.getInstance().logout();
        try {
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/login.fxml")).load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setTitle("Academia de Belleza");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cerrar sesión");
            contentPane.getChildren().setAll(errorLabel);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            } else {
                // Si por alguna razón contentPane no está disponible, lo registramos
                System.err.println("Warning: contentPane is null, cannot set view: " + fxmlPath);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar FXML: " + fxmlPath + " -> " + e.getMessage());
            e.printStackTrace(System.err);
            // En caso de error, mostramos un mensaje simple en el área central (si está disponible)
            if (contentPane != null) {
                Label errorLabel = new Label("No se pudo cargar la vista: " + fxmlPath);
                contentPane.getChildren().setAll(errorLabel);
            }
        }
    }

    /**
     * Carga los KPIs de cursos desde la capa de servicio y actualiza las labels
     * definidas en el FXML (se busca por id en la escena para evitar acoplar
     * campos nuevos en la clase y mantener compatibilidad).
     */
    private void loadKpiCursos() {
        try {
            CursoService cursoService = new CursoService(new CursoRepositoryImpl());
            java.util.List<Curso> abiertos = cursoService.getCursosAbiertos();
            long totalCupos = cursoService.getTotalCuposDisponibles();

            // Actualizar labels inyectadas por FXML (si están presentes)
            if (cursosAbiertosLabel != null) {
                cursosAbiertosLabel.setText("Cursos abiertos: " + abiertos.size());
                // pequeño efecto visual
                DropShadow ds = new DropShadow(8, Color.web("#2ecc71"));
                cursosAbiertosLabel.setEffect(ds);
            }
            if (cuposDisponiblesLabel != null) {
                cuposDisponiblesLabel.setText("Cupos disponibles: " + totalCupos);
                // animación de entrada
                ScaleTransition st = new ScaleTransition(Duration.millis(600), cuposDisponiblesLabel);
                st.setFromX(0.8);
                st.setFromY(0.8);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onKpiCursosClicked(MouseEvent event) {
        try {
            CursoService cursoService = new CursoService(new CursoRepositoryImpl());
            java.util.List<Curso> abiertos = cursoService.getCursosAbiertos();

            ListView<String> listView = new ListView<>();
            abiertos.stream().map(Curso::getNombre).forEach(listView.getItems()::add);
            listView.setPrefHeight(300);

            Button cerrar = new Button("Volver");
            cerrar.setOnAction(ae -> loadView("/ui/view/estudiantes.fxml"));

            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 10, 0, 0, 2);");
            Label title = new Label("Cursos abiertos");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            card.getChildren().addAll(title, listView, cerrar);

            // animación de fade-in
            FadeTransition ft = new FadeTransition(Duration.millis(400), card);
            ft.setFromValue(0);
            ft.setToValue(1);
                    if (contentPane != null) {
                        contentPane.getChildren().setAll(card);
                    } else {
                        System.err.println("contentPane is null when attempting to show cursos abiertos card");
                    }
            ft.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onKpiCuposClicked(MouseEvent event) {
        try {
            // animación destacada cuando se hace click en cupos
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


