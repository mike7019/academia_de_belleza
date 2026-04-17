package org.example.academia.ui.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador reutilizable para la tarjeta placeholder de modulos.
 */
public class ModuleCardController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    public void setTexts(String title, String subtitle) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        if (subtitleLabel != null) {
            subtitleLabel.setText(subtitle);
        }
    }
}

