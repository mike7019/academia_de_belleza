package org.example.academia.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.academia.config.DatabaseConfig;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializar la configuración de base de datos (Flyway + JPA) al arrancar la aplicación
        // La primera llamada a DatabaseConfig fuerza la creación del EntityManagerFactory.
        // En entornos de desarrollo puede fallar (p. ej. DB no disponible). Capturamos
        // la excepción para permitir que la UI se inicie y facilitar diagnóstico.
        try {
            DatabaseConfig.getEntityManagerFactory();
        } catch (Exception ex) {
            System.err.println("Advertencia: fallo inicializando la base de datos: " + ex.getMessage());
            ex.printStackTrace();
            // Mostrar mensaje mínimo al usuario podría hacerse aquí; evitamos bloquear el arranque.
        }

        Parent root = new FXMLLoader(getClass().getResource("/ui/view/login.fxml")).load();
        primaryStage.setTitle("Academia de Belleza");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        // Cerrar recursos de JPA al cerrar la aplicación (protegido por si no se inicializó)
        try {
            DatabaseConfig.shutdown();
        } catch (Exception ex) {
            System.err.println("Advertencia: fallo cerrando recursos JPA: " + ex.getMessage());
        }
        super.stop();
    }
}

