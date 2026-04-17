package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Cursos.
 * <p>
 * Se integrará con CursoService y los DTOs correspondientes para
 * gestionar la administración de cursos (CUR-02, CUR-03).
 */
public class CursoController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Cursos",
                "Pantalla de gestión de cursos (placeholder)."
        );
    }
}

