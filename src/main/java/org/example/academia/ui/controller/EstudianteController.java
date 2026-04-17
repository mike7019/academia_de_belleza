package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Estudiantes.
 * <p>
 * En futuras historias (EST-02, EST-03) se integrará con EstudianteService
 * y EstudianteDTO para gestionar el CRUD completo de estudiantes.
 */
public class EstudianteController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Estudiantes",
                "Pantalla de gestión de estudiantes (placeholder)."
        );
    }
}

