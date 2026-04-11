package org.example.academia.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.example.academia.dto.EstudianteDTO;
import org.example.academia.repository.EstudianteRepositoryImpl;
import org.example.academia.repository.AuditoriaRepositoryImpl;
import org.example.academia.security.AuthorizationService;
import org.example.academia.service.EstudianteService;
import org.example.academia.service.BusinessException;

/**
 * Controlador para el módulo de Estudiantes: formulario simple y lista.
 */
public class EstudianteController {

    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private TextField tfNumeroDocumento;
    @FXML private TextField tfTelefono;
    @FXML private TextField tfEmail;
    @FXML private CheckBox chActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnNuevo;
    @FXML private Label lblMensaje;

    @FXML private TableView<EstudianteDTO> tblEstudiantes;
    @FXML private TableColumn<EstudianteDTO, Long> colId;
    @FXML private TableColumn<EstudianteDTO, String> colNombre;
    @FXML private TableColumn<EstudianteDTO, String> colApellido;
    @FXML private TableColumn<EstudianteDTO, String> colDocumento;
    @FXML private TableColumn<EstudianteDTO, String> colEmail;
    @FXML private TableColumn<EstudianteDTO, String> colTelefono;

    private EstudianteService estudianteService;
    private ObservableList<EstudianteDTO> estudiantes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Inicializar servicios
        estudianteService = new EstudianteService(new EstudianteRepositoryImpl(), new AuthorizationService(), new AuditoriaRepositoryImpl());

        cbTipoDocumento.setItems(FXCollections.observableArrayList("DNI", "PASAPORTE", "LIBRETA"));

        // Configurar columnas (usamos properties directas por nombres de getters)
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        colApellido.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getApellido()));
        colDocumento.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeroDocumento()));
        colEmail.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
        colTelefono.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTelefono()));

        tblEstudiantes.setItems(estudiantes);

        btnGuardar.setOnAction(e -> onGuardar());
        btnGuardar.setDefaultButton(true); // permite pulsar Enter para activar el botón por defecto
        btnNuevo.setOnAction(e -> limpiarFormulario());

        // Actualizar tabla y aplicar filtro en tiempo real al escribir en los campos relevantes
        tfNombre.textProperty().addListener((obs, oldV, newV) -> filtrar());
        tfApellido.textProperty().addListener((obs, oldV, newV) -> filtrar());
        tfNumeroDocumento.textProperty().addListener((obs, oldV, newV) -> filtrar());

        // Permitir guardar también con la tecla Enter en los campos del formulario
        KeyCode[] enterFields = new KeyCode[]{};
        tfNombre.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfApellido.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfNumeroDocumento.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfTelefono.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfEmail.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });

        cargarTodos();
        filtrar();
    }

    private void cargarTodos() {
        estudiantes.setAll(estudianteService.findAll());
    }

    private void onGuardar() {
        lblMensaje.setText("");
        try {
            EstudianteDTO dto = new EstudianteDTO();
            dto.setNombre(tfNombre.getText());
            dto.setApellido(tfApellido.getText());
            dto.setTipoDocumento(cbTipoDocumento.getValue());
            dto.setNumeroDocumento(tfNumeroDocumento.getText());
            dto.setTelefono(tfTelefono.getText());
            dto.setEmail(tfEmail.getText());
            dto.setActivo(chActivo.isSelected());

            EstudianteDTO saved = estudianteService.save(dto);
            lblMensaje.setText("Guardado correctamente.");
            limpiarFormulario();
            cargarTodos();
            filtrar();
        } catch (BusinessException ex) {
            lblMensaje.setText(ex.getMessage());
        } catch (Exception ex) {
            lblMensaje.setText("Error al guardar: " + ex.getMessage());
        }
    }

    /**
     * Filtra la lista mostrada en la tabla según los campos del formulario (nombre, apellido, documento).
     * Se realiza en memoria consultando al servicio.
     */
    private void filtrar() {
        String nombreQ = tfNombre.getText() == null ? "" : tfNombre.getText().trim().toLowerCase();
        String apellidoQ = tfApellido.getText() == null ? "" : tfApellido.getText().trim().toLowerCase();
        String docQ = tfNumeroDocumento.getText() == null ? "" : tfNumeroDocumento.getText().trim().toLowerCase();

        estudiantes.setAll(estudianteService.findAll().stream()
                .filter(e -> (nombreQ.isEmpty() || (e.getNombre() != null && e.getNombre().toLowerCase().contains(nombreQ))))
                .filter(e -> (apellidoQ.isEmpty() || (e.getApellido() != null && e.getApellido().toLowerCase().contains(apellidoQ))))
                .filter(e -> (docQ.isEmpty() || (e.getNumeroDocumento() != null && e.getNumeroDocumento().toLowerCase().contains(docQ))))
                .toList());
    }

    private void limpiarFormulario() {
        tfNombre.clear();
        tfApellido.clear();
        cbTipoDocumento.getSelectionModel().clearSelection();
        tfNumeroDocumento.clear();
        tfTelefono.clear();
        tfEmail.clear();
        chActivo.setSelected(true);
    }
}

