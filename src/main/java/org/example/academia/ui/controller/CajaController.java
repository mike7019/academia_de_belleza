package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Caja/Pagos.
 * <p>
 * Más adelante se integrará con PagoEstudianteService y MatriculaService
 * para registrar pagos, mostrar saldos y anular pagos (PAG-02, PAG-03, PAG-04).
 */
public class CajaController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Pagos / Caja",
                "Pantalla de caja y pagos de estudiantes (placeholder)."
        );
    }
}

