package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
    private void initialize() {
        // Mostrar usuario actual si está disponible en la sesión
        if (usuarioLabel != null && SessionManager.getInstance().getCurrentUser() != null) {
            usuarioLabel.setText("Usuario: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }

        // Cargar vista por defecto (Estudiantes) al abrir el Dashboard
        loadView("/ui/view/estudiantes.fxml");
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
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error, mostramos un mensaje simple en el área central
            Label errorLabel = new Label("No se pudo cargar la vista: " + fxmlPath);
            contentPane.getChildren().setAll(errorLabel);
        }
    }
}

