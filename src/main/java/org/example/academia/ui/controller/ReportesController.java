package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import org.example.academia.ui.controller.components.ModuleCardController;

/**
 * Controlador placeholder para el módulo de Reportes.
 * <p>
 * Se integrará con JasperReportService y los servicios de dominio para
 * generar reportes de estudiantes, pagos, nómina e ingresos (REP-01 a REP-05).
 */
public class ReportesController {

    @FXML
    private ModuleCardController moduleCardController;

    @FXML
    private void initialize() {
        moduleCardController.setTexts(
                "Módulo Reportes",
                "Pantalla de reportes (placeholder)."
        );
    }
}

