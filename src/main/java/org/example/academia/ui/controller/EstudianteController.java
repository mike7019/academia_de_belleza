package org.example.academia.ui.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import javafx.collections.ListChangeListener;
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

    @FXML private TableView<EstudianteDTO> tblEstudiantes;
    @FXML private TableColumn<EstudianteDTO, Long> colId;
    @FXML private TableColumn<EstudianteDTO, String> colNombre;
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

    // Paginación
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalPages = 1;

    @FXML
    private void initialize() {
        // Inicializar servicios (authorization service local)
        org.example.academia.security.AuthorizationService authorizationService = new org.example.academia.security.AuthorizationService();
        estudianteService = new EstudianteService(new EstudianteRepositoryImpl(), authorizationService, new AuditoriaRepositoryImpl());

        // Habilitar/deshabilitar acciones según permisos del usuario actual
        try {
            boolean puedeCrear = authorizationService.hasPermission("ESTUDIANTE_CREAR");
            btnGuardar.setDisable(!puedeCrear);
            if (!puedeCrear) {
                // Mensaje más informativo: indicar si no hay sesión o si el usuario no tiene el permiso
                Usuario current = SessionManager.getInstance().getCurrentUser();
                if (current == null) {
                    lblMensaje.setText("No tiene permiso para crear estudiantes. Por favor inicie sesión.");
                } else {
                    lblMensaje.setText("Usuario '" + current.getUsername() + "' no tiene permiso: ESTUDIANTE_CREAR");
                }
            }
        } catch (Exception ex) {
            // En caso de fallo en autorización, deshabilitar guardado y mostrar mensaje genérico
            btnGuardar.setDisable(true);
            lblMensaje.setText("No autorizado");
        }

        cbTipoDocumento.setItems(FXCollections.observableArrayList("DNI", "PASAPORTE", "LIBRETA"));

        // Configurar columnas (usamos properties directas por nombres de getters)
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        colApellido.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getApellido()));
        colDocumento.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeroDocumento()));
        colEmail.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
        colDireccion.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDireccion()));
        colTelefono.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTelefono()));

        tblEstudiantes.setItems(estudiantes);

        btnGuardar.setOnAction(e -> onGuardar());
        btnGuardar.setDefaultButton(true); // permite pulsar Enter para activar el botón por defecto
        btnNuevo.setOnAction(e -> limpiarFormulario());

        // Actualizar tabla y aplicar filtro en tiempo real al escribir en los campos relevantes
        // Usar debounce (PauseTransition) para evitar llamadas en cada pulsación
        PauseTransition pauseNombre = new PauseTransition(Duration.millis(300));
        tfNombre.textProperty().addListener((obs, oldV, newV) -> {
            pauseNombre.stop();
            pauseNombre.setOnFinished(e -> { currentPage = 0; filtrar(); });
            pauseNombre.playFromStart();
        });

        PauseTransition pauseApellido = new PauseTransition(Duration.millis(300));
        tfApellido.textProperty().addListener((obs, oldV, newV) -> {
            pauseApellido.stop();
            pauseApellido.setOnFinished(e -> { currentPage = 0; filtrar(); });
            pauseApellido.playFromStart();
        });

        PauseTransition pauseDocumento = new PauseTransition(Duration.millis(300));
        tfNumeroDocumento.textProperty().addListener((obs, oldV, newV) -> {
            pauseDocumento.stop();
            pauseDocumento.setOnFinished(e -> { currentPage = 0; filtrar(); });
            pauseDocumento.playFromStart();
        });

        // Mantener filtrado inmediato para dirección (menos usado para búsqueda rápida)
        tfDireccion.textProperty().addListener((obs, oldV, newV) -> { currentPage = 0; filtrar(); });

        // Filtrado por activo: el checkbox `chFiltrarActivos` controla si se muestran sólo activos.
        chFiltrarActivos.selectedProperty().addListener((obs, oldV, newV) -> { currentPage = 0; filtrar(); });

        // Escuchar cambios en el orden de la tabla para aplicar orden en la búsqueda
        tblEstudiantes.getSortOrder().addListener((ListChangeListener<TableColumn<EstudianteDTO, ?>>) c -> { currentPage = 0; filtrar(); });

        // Paginación UI
        cbPageSize.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        cbPageSize.setValue(pageSize);
        cbPageSize.setOnAction(e -> { Integer v = cbPageSize.getValue(); if (v != null) { pageSize = v; currentPage = 0; filtrar(); } });
        btnPrev.setOnAction(e -> prevPage());
        btnNext.setOnAction(e -> nextPage());

        // Permitir guardar también con la tecla Enter en los campos del formulario
        tfNombre.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfApellido.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfNumeroDocumento.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfTelefono.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfEmail.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });
        tfDireccion.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onGuardar(); });

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
            dto.setDireccion(tfDireccion.getText());
            dto.setEmail(tfEmail.getText());
            dto.setActivo(chActivo.isSelected());

            estudianteService.save(dto);
            lblMensaje.setText("Guardado correctamente.");
            limpiarFormulario();
            cargarTodos();
            filtrar();
        } catch (AuthException ex) {
            // Mensaje claro para errores de autorización
            lblMensaje.setText(ex.getMessage());
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
        // Usar búsqueda en servidor (repositorio) para eficiencia en grandes volúmenes.
        String nombreQ = tfNombre.getText() == null ? null : tfNombre.getText().trim();
        String apellidoQ = tfApellido.getText() == null ? null : tfApellido.getText().trim();
        String docQ = tfNumeroDocumento.getText() == null ? null : tfNumeroDocumento.getText().trim();

        try {
            // Determinar filtro por activo según control explícito
            Boolean activoFilter = chFiltrarActivos != null && chFiltrarActivos.isSelected() ? Boolean.TRUE : null;

            // Determinar sortBy y asc a partir de la columna ordenada en la tabla
            TableColumn<EstudianteDTO, ?> primary = tblEstudiantes.getSortOrder().isEmpty() ? null : tblEstudiantes.getSortOrder().get(0);
            String sortBy = "id";
            boolean asc = true;
            if (primary != null) {
                if (primary == colNombre) sortBy = "nombre";
                else if (primary == colApellido) sortBy = "apellido";
                else if (primary == colDocumento) sortBy = "numeroDocumento";
                else if (primary == colEmail) sortBy = "email";
                else if (primary == colId) sortBy = "id";
                // otras columnas no permitidas en lista blanca usarán id por defecto
                asc = primary.getSortType() == TableColumn.SortType.ASCENDING;
            }

            // Usar currentPage y pageSize para la paginación
            PaginatedResult<EstudianteDTO> result = estudianteService.search(nombreQ, apellidoQ, docQ, activoFilter, currentPage, pageSize, sortBy, asc);
            estudiantes.setAll(result.getItems());
            // Actualizar indicadores de paginación
            long totalItems = result.getTotal();
            totalPages = (int) Math.max(1, (totalItems + pageSize - 1) / pageSize);
            lblPageInfo.setText("Página " + (currentPage + 1) + " de " + totalPages + " (total: " + totalItems + ")");
            btnPrev.setDisable(currentPage <= 0);
            btnNext.setDisable(currentPage >= totalPages - 1);
        } catch (Exception ex) {
            lblMensaje.setText("Error buscando estudiantes: " + ex.getMessage());
        }
    }

    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            filtrar();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            filtrar();
        }
    }

    private void limpiarFormulario() {
        tfNombre.clear();
        tfApellido.clear();
        cbTipoDocumento.getSelectionModel().clearSelection();
        tfNumeroDocumento.clear();
        tfTelefono.clear();
        tfDireccion.clear();
        tfEmail.clear();
        chActivo.setSelected(true);
    }
}

