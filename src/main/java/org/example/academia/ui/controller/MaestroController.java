package org.example.academia.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.academia.dto.MaestroDTO;
import org.example.academia.mapper.MaestroMapper;
import org.example.academia.service.MaestroService;

/**
 * Controlador para el módulo de Maestros/Profesores.
 * <p>
 * Gestiona el CRUD de maestros y su modalidad de pago (historias MAE-01, MAE-02, MAE-03).
 */
public class MaestroController {

    @FXML
    private TableView<MaestroDTO> maestrosTable;

    @FXML
    private TableColumn<MaestroDTO, String> nombreColumn;

    @FXML
    private TableColumn<MaestroDTO, String> apellidoColumn;

    @FXML
    private TableColumn<MaestroDTO, String> documentoColumn;

    @FXML
    private TableColumn<MaestroDTO, String> telefonoColumn;

    @FXML
    private TableColumn<MaestroDTO, String> tipoPagoColumn;

    @FXML
    private TableColumn<MaestroDTO, Boolean> activoColumn;

    private MaestroService maestroService;
    private ObservableList<MaestroDTO> maestrosList;

    public MaestroController() {
        this.maestroService = new MaestroService();
        this.maestrosList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        // Configurar columnas de la tabla
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        tipoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPagoProfesor"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        maestrosTable.setItems(maestrosList);
        cargarMaestros();
    }

    private void cargarMaestros() {
        maestrosList.clear();
        maestroService.listarMaestrosActivos().forEach(maestro ->
            maestrosList.add(MaestroMapper.toDTO(maestro))
        );
    }
}
