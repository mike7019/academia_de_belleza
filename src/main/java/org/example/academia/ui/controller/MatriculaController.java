package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Matrículas.
 * <p>
 * Se conectará con MatriculaService y sus DTOs en las historias MAT-02/MAT-03.
 */
public class MatriculaController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Matrículas",
                "Pantalla de gestión de matrículas (placeholder)."
        );
    }
}

