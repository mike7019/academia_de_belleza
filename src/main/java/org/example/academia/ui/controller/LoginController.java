package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.academia.security.AuthException;
import org.example.academia.security.SeguridadService;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final SeguridadService seguridadService = new SeguridadService();

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            seguridadService.login(username, password);
            messageLabel.setText("Login correcto");

            // Navegar al dashboard principal
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/dashboard.fxml")).load();
            stage.setTitle("Academia de Belleza - Dashboard");
            // Tamaño mediano-grande para el dashboard
            stage.setScene(new Scene(root, 1100, 700));
        } catch (AuthException ex) {
            messageLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            messageLabel.setText("Error al iniciar sesión");
            ex.printStackTrace();
        }
    }

    @FXML
    private void onRegister() {
        try {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/registro.fxml")).load();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error al abrir la pantalla de registro");
        }
    }
}

