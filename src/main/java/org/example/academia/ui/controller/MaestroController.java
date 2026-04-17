package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Maestros/Profesores.
 * <p>
 * Más adelante se integrará con MaestroService y los DTOs correspondientes
 * para gestionar el CRUD de maestros y su modalidad de pago (historias MAE-01, MAE-02, MAE-03).
 */
public class MaestroController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Maestros",
                "Pantalla de gestión de maestros/profesores (placeholder)."
        );
    }
}

