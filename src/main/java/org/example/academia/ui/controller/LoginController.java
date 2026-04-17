package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.academia.domain.entity.Usuario;
import org.example.academia.security.AuthException;
import org.example.academia.security.SessionManager;
import org.example.academia.security.SeguridadService;

public class LoginController {

    @FXML
    private StackPane contentPane;

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

            // Verificar si el usuario debe cambiar su contraseña temporal
            Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
            if (usuarioActual != null && usuarioActual.isRequiereCambioContrasena()) {
                boolean cambioExitoso = mostrarFormularioCambioContrasena(usuarioActual.getUsername());
                if (!cambioExitoso) {
                    // Si cancela, cerrar sesión y no avanzar
                    SessionManager.getInstance().logout();
                    messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    messageLabel.setText("Debe cambiar su contraseña temporal para continuar.");
                    return;
                }
            }

            Stage stage = (Stage) contentPane.getScene().getWindow();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/dashboard.fxml")).load();
            stage.setTitle("Academia de Belleza - Dashboard");
            stage.setScene(new Scene(root, 1100, 700));
        } catch (AuthException ex) {
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            messageLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            messageLabel.setText("Error al iniciar sesión");
            ex.printStackTrace();
        }
    }

    /**
     * Muestra un diálogo obligatorio para cambiar la contraseña temporal.
     * Retorna true si el cambio fue exitoso, false si el usuario canceló.
     */
    private boolean mostrarFormularioCambioContrasena(String username) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cambio de contraseña obligatorio");
        dialog.setHeaderText("Tu contraseña es temporal.\nDebes crear una nueva contraseña para continuar.");

        ButtonType cambiarBtn = new ButtonType("Cambiar contraseña", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cambiarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        PasswordField nuevaField = new PasswordField();
        nuevaField.setPromptText("Nueva contraseña");
        nuevaField.setPrefWidth(220);
        PasswordField confirmarField = new PasswordField();
        confirmarField.setPromptText("Confirmar nueva contraseña");
        confirmarField.setPrefWidth(220);
        Label errorLabel = new Label();
        errorLabel.setTextFill(javafx.scene.paint.Color.RED);
        errorLabel.setWrapText(true);

        grid.add(new Label("Nueva contraseña:"), 0, 0);
        grid.add(nuevaField, 1, 0);
        grid.add(new Label("Confirmar:"), 0, 1);
        grid.add(confirmarField, 1, 1);
        grid.add(errorLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Validar antes de cerrar el diálogo
        dialog.getDialogPane().lookupButton(cambiarBtn).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    String nueva = nuevaField.getText();
                    String confirmar = confirmarField.getText();

                    if (nueva == null || nueva.isBlank()) {
                        errorLabel.setText("La nueva contraseña es obligatoria");
                        event.consume();
                        return;
                    }
                    if (nueva.length() < 4) {
                        errorLabel.setText("La contraseña debe tener al menos 4 caracteres");
                        event.consume();
                        return;
                    }
                    if (!nueva.equals(confirmar)) {
                        errorLabel.setText("Las contraseñas no coinciden");
                        event.consume();
                        return;
                    }

                    try {
                        seguridadService.cambiarContrasena(username, nueva);
                    } catch (Exception ex) {
                        errorLabel.setText(ex.getMessage());
                        event.consume();
                    }
                }
        );

        var resultado = dialog.showAndWait();
        return resultado.isPresent() && resultado.get() == cambiarBtn;
    }

    @FXML
    private void onRegister() {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            Parent root = new FXMLLoader(getClass().getResource("/ui/view/registro.fxml")).load();
            double width = stage.getWidth() > 0 ? stage.getWidth() : 1040;
            double height = stage.getHeight() > 0 ? stage.getHeight() : 862;
            stage.setScene(new Scene(root, width, height));
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            messageLabel.setText("Error al abrir la pantalla de registro");
        }
    }

    @FXML
    private void onRecuperarContrasena() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recuperar contraseña");
        dialog.setHeaderText("Ingresa tu correo electrónico registrado.\nSe enviará una contraseña temporal a tu correo.");

        ButtonType enviarBtn = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enviarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");
        emailField.setPrefWidth(260);

        grid.add(new Label("Correo electrónico:"), 0, 0);
        grid.add(emailField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == enviarBtn) {
                String email = emailField.getText();

                if (email == null || email.isBlank()) {
                    messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    messageLabel.setText("Ingrese su correo electrónico");
                    return;
                }

                try {
                    seguridadService.recuperarContrasena(email.trim());
                    messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    messageLabel.setText("Se envió una contraseña temporal a " + email.trim());
                } catch (AuthException ex) {
                    messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    messageLabel.setText(ex.getMessage());
                } catch (Exception ex) {
                    messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                    messageLabel.setText("Error al recuperar contraseña: " + ex.getMessage());
                }
            }
        });
    }
}
