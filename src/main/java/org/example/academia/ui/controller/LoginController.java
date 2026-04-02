package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Lógica temporal hasta implementar SeguridadService
        if ("admin".equals(username) && "admin".equals(password)) {
            messageLabel.setText("Login correcto (mock)");
        } else {
            messageLabel.setText("Usuario o contraseña incorrectos");
        }
    }
}

