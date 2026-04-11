package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.example.academia.service.UsuarioService;

import java.io.IOException;

/**
 * Controlador de la pantalla de registro de usuario.
 *
 * Implementa el front de registro y delega la lógica de persistencia en UsuarioService.
 */
public class RegistroController {

    @FXML
    private TextField nombreCompletoField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    private void onRegister() {
        String nombreCompleto = nombreCompletoField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validaciones básicas de front
        if (nombreCompleto == null || nombreCompleto.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            messageLabel.setText("Nombre, usuario y contraseñas son obligatorios");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Las contraseñas no coinciden");
            return;
        }

        try {
            usuarioService.registrarUsuario(nombreCompleto, email, username, password);
            messageLabel.setText("Registro exitoso. Ahora puede iniciar sesión.");
        } catch (IllegalArgumentException ex) {
            messageLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Error al registrar usuario");
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/login.fxml")).load();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error al volver al login");
        }
    }
}


