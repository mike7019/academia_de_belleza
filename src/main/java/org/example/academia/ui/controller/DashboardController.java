package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.academia.security.SessionManager;
import org.example.academia.service.EstudianteService;

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
    private Label totalActivosLabel;

    @FXML
    private Label nuevosRegistradosLabel;

    private final EstudianteService estudianteService = new EstudianteService();

    @FXML
    private void initialize() {
        // Mostrar usuario actual si está disponible en la sesión
        if (usuarioLabel != null && SessionManager.getInstance().getCurrentUser() != null) {
            usuarioLabel.setText("Usuario: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }

        // Cargar KPIs de estudiantes
        loadKPIs();

        // KPIs are shown by default; views loaded on button clicks
    }

    private void loadKPIs() {
        try {
            long totalActivos = estudianteService.getTotalEstudiantesActivos();
            long nuevosRegistrados = estudianteService.getNuevosEstudiantesRegistradosMesActual();
            totalActivosLabel.setText(String.valueOf(totalActivos));
            nuevosRegistradosLabel.setText(String.valueOf(nuevosRegistrados));
        } catch (Exception e) {
            e.printStackTrace();
            totalActivosLabel.setText("Error");
            nuevosRegistradosLabel.setText("Error");
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

    @FXML
    private void onDashboard() {
        // Reload KPIs
        loadKPIs();
        // Since KPIs are the initial content, but to refresh, recreate the view
        VBox kpiBox = createKPIBox();
        contentPane.getChildren().setAll(kpiBox);
    }

    private VBox createKPIBox() {
        VBox kpiBox = new VBox(20.0);
        kpiBox.setAlignment(javafx.geometry.Pos.CENTER);
        kpiBox.setStyle("-fx-padding: 20;");

        Label title = new Label("Estadísticas de Estudiantes");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox hbox = new HBox(20.0);
        hbox.setAlignment(javafx.geometry.Pos.CENTER);

        VBox totalBox = new VBox(10.0);
        totalBox.setAlignment(javafx.geometry.Pos.CENTER);
        totalBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #ecf0f1;");
        Label totalLabel = new Label("Total Estudiantes Activos");
        totalLabel.setStyle("-fx-font-size: 14px;");
        totalActivosLabel.setText(String.valueOf(estudianteService.getTotalEstudiantesActivos()));
        totalBox.getChildren().addAll(totalLabel, totalActivosLabel);

        VBox nuevosBox = new VBox(10.0);
        nuevosBox.setAlignment(javafx.geometry.Pos.CENTER);
        nuevosBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: #ecf0f1;");
        Label nuevosLabel = new Label("Nuevos Registrados (Mes Actual)");
        nuevosLabel.setStyle("-fx-font-size: 14px;");
        nuevosRegistradosLabel.setText(String.valueOf(estudianteService.getNuevosEstudiantesRegistradosMesActual()));
        nuevosBox.getChildren().addAll(nuevosLabel, nuevosRegistradosLabel);

        hbox.getChildren().addAll(totalBox, nuevosBox);
        kpiBox.getChildren().addAll(title, hbox);

        return kpiBox;
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
