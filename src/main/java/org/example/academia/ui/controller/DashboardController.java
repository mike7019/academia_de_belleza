package org.example.academia.ui.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.academia.security.SessionManager;

import java.io.IOException;

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
    private Button dashboardButton;

    @FXML
    private Button estudiantesButton;

    @FXML
    private Button maestrosButton;

    @FXML
    private Button cursosButton;

    @FXML
    private Button matriculasButton;

    @FXML
    private Button pagosButton;

    @FXML
    private Button nominaButton;

    @FXML
    private Button reportesButton;

    private Button activeButton;

    @FXML
    private void initialize() {
        // Mostrar usuario actual si está disponible en la sesión
        if (usuarioLabel != null && SessionManager.getInstance().getCurrentUser() != null) {
            usuarioLabel.setText("Usuario: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }

        // Vista por defecto: Dashboard principal
        if (dashboardButton != null) {
            setActiveButton(dashboardButton);
        }
        showDashboardHome();
    }

    @FXML
    private void onDashboard() {
        setActiveButton(dashboardButton);
        showDashboardHome();
    }

    @FXML
    private void onEstudiantes() {
        setActiveButton(estudiantesButton);
        loadView("/ui/view/estudiantes.fxml");
    }

    @FXML
    private void onCursos() {
        setActiveButton(cursosButton);
        loadView("/ui/view/cursos.fxml");
    }

    @FXML
    private void onMaestros() {
        setActiveButton(maestrosButton);
        loadView("/ui/view/maestros.fxml");
    }

    @FXML
    private void onMatriculas() {
        setActiveButton(matriculasButton);
        loadView("/ui/view/matriculas.fxml");
    }

    @FXML
    private void onPagos() {
        setActiveButton(pagosButton);
        loadView("/ui/view/caja.fxml");
    }

    @FXML
    private void onNomina() {
        setActiveButton(nominaButton);
        loadView("/ui/view/nomina.fxml");
    }

    @FXML
    private void onReportes() {
        setActiveButton(reportesButton);
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
            setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error, mostramos un mensaje simple en el área central
            Label errorLabel = new Label("No se pudo cargar la vista: " + fxmlPath);
            setContent(errorLabel);
        }
    }

    /**
     * Muestra un panel simple de bienvenida como contenido del Dashboard.
     */
    private void showDashboardHome() {
        Label title = new Label("Bienvenido a la Academia de Belleza");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("Selecciona un módulo del menú lateral para comenzar.");
        subtitle.getStyleClass().add("content-subtitle");
        subtitle.setWrapText(true);

        VBox card = new VBox(8.0, title, subtitle);
        card.getStyleClass().add("content-card");

        setContent(card);
    }

    /**
     * Aplica transición de desvanecido al reemplazar el contenido central.
     */
    private void setContent(Node node) {
        if (contentPane == null || node == null) {
            return;
        }

        node.setOpacity(0);
        contentPane.getChildren().setAll(node);

        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Marca visualmente el botón activo del menú lateral.
     */
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button-active");
        }
        activeButton = button;
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
}

