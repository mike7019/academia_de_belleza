package org.example.academia.ui.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.HBox;
import java.util.Optional;
import org.example.academia.dto.EstudianteDTO;
import org.example.academia.dto.PaginatedResult;
import org.example.academia.service.EstudianteService;
import org.example.academia.repository.EstudianteRepositoryImpl;
import org.example.academia.repository.AuditoriaRepositoryImpl;
import org.example.academia.security.SessionManager;
import org.example.academia.domain.entity.Usuario;
// authorization service is created locally in initialize()
import org.example.academia.service.BusinessException;
import org.example.academia.security.AuthException;

/**
 * Controlador para el módulo de Estudiantes: formulario simple y lista.
 */
public class EstudianteController {

    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private TextField tfNumeroDocumento;
    @FXML private TextField tfTelefono;
    @FXML private TextField tfDireccion;
    @FXML private TextField tfEmail;
    @FXML private CheckBox chActivo;
    @FXML private CheckBox chFiltrarActivos;
    @FXML private Button btnGuardar;
    @FXML private Button btnNuevo;
    @FXML private Label lblMensaje;
    @FXML private Label lblDocError;
    @FXML private HBox actionButtonsBox;

    @FXML private TableView<EstudianteDTO> tblEstudiantes;
    @FXML private TableColumn<EstudianteDTO, Long> colId;
    @FXML private TableColumn<EstudianteDTO, String> colNombre;
    @FXML private TableColumn<EstudianteDTO, String> colNombreEstado;
    @FXML private TableColumn<EstudianteDTO, String> colApellido;
    @FXML private TableColumn<EstudianteDTO, String> colDocumento;
    @FXML private TableColumn<EstudianteDTO, String> colEmail;
    @FXML private TableColumn<EstudianteDTO, String> colDireccion;
    @FXML private TableColumn<EstudianteDTO, String> colTelefono;

    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private ComboBox<Integer> cbPageSize;
    @FXML private Label lblPageInfo;

    private EstudianteService estudianteService;
    private final ObservableList<EstudianteDTO> estudiantes = FXCollections.observableArrayList();
    // Id del estudiante que se está editando (null si se crea uno nuevo)
    private Long editingId = null;
    // Authorization service promoted to field so other methods / handlers can use it
    private org.example.academia.security.AuthorizationService authorizationService;
    // Botones de acciones globales (arriba/debajo del formulario)
    private Button btnEditarSelectedTop;
    private Button btnInactivarSelectedTop;
    private Button btnReactivarSelectedTop;

    // Paginación
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 1;

    @FXML
    private void initialize() {
        // Inicialización futura de bindings y carga de datos.
    }
}

