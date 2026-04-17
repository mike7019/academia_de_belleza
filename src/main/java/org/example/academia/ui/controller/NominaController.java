package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Nómina.
 * <p>
 * Se conectará con NominaService y las estrategias de pago a profesores
 * en las historias NOM-01 a NOM-05.
 */
public class NominaController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Nómina",
                "Pantalla de gestión de nómina de profesores (placeholder)."
        );
    }
}

