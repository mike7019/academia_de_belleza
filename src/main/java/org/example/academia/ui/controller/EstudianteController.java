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
        // Inicializar servicios (authorization service como campo para reusar en handlers)
        this.authorizationService = new org.example.academia.security.AuthorizationService();
        estudianteService = new EstudianteService(new EstudianteRepositoryImpl(), this.authorizationService, new AuditoriaRepositoryImpl());

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

        // Inicialmente ocultar el label de error de documento
        if (lblDocError != null) {
            lblDocError.setText("");
            lblDocError.setVisible(false);
        }

        // Por defecto, mostrar sólo estudiantes activos (UX): el checkbox controla esto
        if (chFiltrarActivos != null) {
            chFiltrarActivos.setSelected(true);
        }

        // Configurar columnas (usamos properties directas por nombres de getters)
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colNombre.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNombre()));
        // Columna combinada: nombre + estado
        if (colNombreEstado != null) {
            colNombreEstado.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getNombre() + (cell.getValue().isActivo() ? " (Activo)" : " (Inactivo)")
            ));
            // Aplicar estilo: nombre en rojo si inactivo
            colNombreEstado.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        EstudianteDTO dto = getTableView().getItems().get(getIndex());
                        if (dto != null && !dto.isActivo()) {
                            setStyle("-fx-text-fill: #b00020;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        }
        colApellido.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getApellido()));
        colDocumento.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeroDocumento()));
        colEmail.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
        colDireccion.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDireccion()));
        colTelefono.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTelefono()));

        tblEstudiantes.setItems(estudiantes);

        // Resaltar visualmente filas inactivas: opacidad reducida y fondo claro
        tblEstudiantes.setRowFactory(tv -> new TableRow<EstudianteDTO>() {
            @Override
            protected void updateItem(EstudianteDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (!item.isActivo()) {
                        // estilo inline para evitar depender de CSS externo
                        setStyle("-fx-opacity: 0.7; -fx-background-color: #f5f5f5;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // En lugar de una columna con acciones por fila, añadimos botones globales cerca de los botones
        // de formulario (Guardar/Nuevo). Esto facilita la edición/inactivación del estudiante seleccionado.
        btnEditarSelectedTop = new Button("Editar seleccionado");
        btnInactivarSelectedTop = new Button("Inactivar seleccionado");
        btnReactivarSelectedTop = new Button("Reactivar seleccionado");

        btnEditarSelectedTop.setOnAction(e -> {
            EstudianteDTO sel = tblEstudiantes.getSelectionModel().getSelectedItem();
            if (sel != null) cargarEstudianteEnFormulario(sel);
            else lblMensaje.setText("Seleccione un estudiante para editar.");
        });

        btnInactivarSelectedTop.setOnAction(e -> {
            EstudianteDTO sel = tblEstudiantes.getSelectionModel().getSelectedItem();
            if (sel == null) { lblMensaje.setText("Seleccione un estudiante para inactivar."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar inactivación");
            confirm.setHeaderText("Inactivar estudiante");
            confirm.setContentText("¿Desea inactivar al estudiante '" + sel.getNombre() + "' (Documento: " + sel.getNumeroDocumento() + ")?");
            Optional<ButtonType> opt = confirm.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) {
                try {
                    estudianteService.inactivate(sel.getId());
                    cargarTodos(); filtrar();
                    lblMensaje.setText("Estudiante inactivado.");
                } catch (Exception ex) {
                    lblMensaje.setText("Error inactivando estudiante: " + ex.getMessage());
                }
            }
        });

        btnReactivarSelectedTop.setOnAction(e -> {
            EstudianteDTO sel = tblEstudiantes.getSelectionModel().getSelectedItem();
            if (sel == null) { lblMensaje.setText("Seleccione un estudiante para reactivar."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar reactivación");
            confirm.setHeaderText("Reactivar estudiante");
            confirm.setContentText("¿Desea reactivar al estudiante '" + sel.getNombre() + "' (Documento: " + sel.getNumeroDocumento() + ")?");
            Optional<ButtonType> opt = confirm.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) {
                try {
                    estudianteService.reactivate(sel.getId());
                    cargarTodos(); filtrar();
                    lblMensaje.setText("Estudiante reactivado.");
                } catch (Exception ex) {
                    lblMensaje.setText("Error reactivando estudiante: " + ex.getMessage());
                }
            }
        });

        // Controlar permisos iniciales: si no tiene permiso, deshabilitar los botones
        try {
            boolean puedeEditarSel = this.authorizationService.hasPermission("ESTUDIANTE_EDITAR");
            btnEditarSelectedTop.setDisable(!puedeEditarSel);
        } catch (Exception ex) { btnEditarSelectedTop.setDisable(true); }
        try {
            boolean puedeInactivarSel = this.authorizationService.hasPermission("ESTUDIANTE_INACTIVAR");
            btnInactivarSelectedTop.setDisable(!puedeInactivarSel);
            btnReactivarSelectedTop.setDisable(!puedeInactivarSel);
        } catch (Exception ex) { btnInactivarSelectedTop.setDisable(true); btnReactivarSelectedTop.setDisable(true); }

        // Añadir los botones al HBox reservado en el FXML si existe, si no, intentar la inserción dinámica como fallback
        if (actionButtonsBox != null) {
            actionButtonsBox.getChildren().clear();
            actionButtonsBox.getChildren().addAll(btnEditarSelectedTop, btnInactivarSelectedTop, btnReactivarSelectedTop);
        } else {
            HBox topActions = new HBox(6, btnEditarSelectedTop, btnInactivarSelectedTop, btnReactivarSelectedTop);
            topActions.setStyle("-fx-padding: 6 0 6 0;");
            javafx.scene.Parent parent = btnGuardar.getParent();
            if (parent instanceof javafx.scene.layout.Pane) {
                javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                int idx = pane.getChildren().indexOf(btnGuardar);
                if (idx >= 0) pane.getChildren().add(idx, topActions);
                else pane.getChildren().add(topActions);
            } else {
                // Si no es posible, notificar en lblMensaje (no romper la inicialización)
                lblMensaje.setText("Acciones añadidas pero no insertadas en el layout; use la tabla para seleccionar.");
            }
        }

        // Escuchar selección de tabla para actualizar visibilidad/estado de botones
        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            updateTopButtonsState(newV);
        });

        btnGuardar.setOnAction(e -> onGuardar());
        btnGuardar.setDefaultButton(true); // permite pulsar Enter para activar el botón por defecto
        btnNuevo.setOnAction(e -> limpiarFormulario());

        // Permitir editar un estudiante haciendo doble click en la fila
        tblEstudiantes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                EstudianteDTO sel = tblEstudiantes.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    cargarEstudianteEnFormulario(sel);
                }
            }
        });

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

        // Verificar duplicado al perder foco en el campo de documento
        tfNumeroDocumento.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) { // perdió foco
                checkDuplicate();
            }
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

    private void checkDuplicate() {
        String doc = tfNumeroDocumento.getText() == null ? null : tfNumeroDocumento.getText().trim();
        if (doc == null || doc.isBlank()) return;
        try {
            EstudianteDTO found = estudianteService.findByNumeroDocumento(doc);
            if (found != null) {
                lblDocError.setText("Ya existe estudiante con documento " + doc + ": " + found.getNombre() + " " + found.getApellido());
                lblDocError.setVisible(true);
                // Estilo claro (rojo) aplicado via FXML; también asegurar accesibilidad
                lblDocError.setStyle("-fx-text-fill: #b00020; -fx-font-size: 11px;");
            } else {
                lblDocError.setText("");
                lblDocError.setVisible(false);
            }
        } catch (Exception ex) {
            // Si hay error de autorización u otro, mostrar en el label de documento para situarlo junto al campo
            lblDocError.setText(ex.getMessage());
            lblDocError.setVisible(true);
        }
    }

    private void cargarTodos() {
        estudiantes.setAll(estudianteService.findAll());
    }

    private void onGuardar() {
        lblMensaje.setText("");
        try {
            // Si estamos en modo edición, pedir confirmación antes de actualizar
            if (editingId != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmar actualización");
                confirm.setHeaderText("Actualizar estudiante");
                String doc = tfNumeroDocumento.getText() == null ? "" : tfNumeroDocumento.getText();
                String nombre = tfNombre.getText() == null ? "" : tfNombre.getText();
                confirm.setContentText("¿Desea actualizar al estudiante '" + nombre + "' (Documento: " + doc + ")?");
                Optional<ButtonType> opt = confirm.showAndWait();
                if (opt.isEmpty() || opt.get() != ButtonType.OK) {
                    lblMensaje.setText("Actualización cancelada.");
                    return;
                }
            }
            EstudianteDTO dto = new EstudianteDTO();
            // Si estamos editando, conservar el id para que el servicio haga update
            if (editingId != null) dto.setId(editingId);
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
            // Si es un error relacionado con el documento, mostrarlo junto al campo
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            if (msg.toLowerCase().contains("documento")) {
                if (lblDocError != null) {
                    lblDocError.setText(msg);
                    lblDocError.setVisible(true);
                    lblDocError.setStyle("-fx-text-fill: #b00020; -fx-font-size: 11px;");
                } else {
                    lblMensaje.setText(msg);
                }
            } else {
                lblMensaje.setText(msg);
            }
        } catch (Exception ex) {
            lblMensaje.setText("Error al guardar: " + ex.getMessage());
        }
    }

    private void cargarEstudianteEnFormulario(EstudianteDTO dto) {
        if (dto == null) return;
        this.editingId = dto.getId();
        tfNombre.setText(dto.getNombre());
        tfApellido.setText(dto.getApellido());
        cbTipoDocumento.setValue(dto.getTipoDocumento());
        tfNumeroDocumento.setText(dto.getNumeroDocumento());
        tfTelefono.setText(dto.getTelefono());
        tfDireccion.setText(dto.getDireccion());
        tfEmail.setText(dto.getEmail());
        chActivo.setSelected(dto.isActivo());
        // Indicar visualmente al usuario que está en modo edición
        if (btnGuardar != null) btnGuardar.setText("Actualizar");
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

    /**
     * Actualiza el estado (visible/disabled) de los botones globales según el estudiante seleccionado
     */
    private void updateTopButtonsState(EstudianteDTO dto) {
        if (btnEditarSelectedTop == null) return;
        if (dto == null) {
            btnEditarSelectedTop.setDisable(true);
            btnInactivarSelectedTop.setDisable(true);
            btnReactivarSelectedTop.setDisable(true);
            return;
        }
        // Permisos
        try {
            boolean puedeEditar = this.authorizationService.hasPermission("ESTUDIANTE_EDITAR");
            btnEditarSelectedTop.setDisable(!puedeEditar);
        } catch (Exception ex) {
            btnEditarSelectedTop.setDisable(true);
        }
        try {
            boolean puedeInactivar = this.authorizationService.hasPermission("ESTUDIANTE_INACTIVAR");
            // Mostrar según estado: si está activo mostrar Inactivar; si está inactivo, mostrar Reactivar
            if (dto.isActivo()) {
                btnInactivarSelectedTop.setVisible(true);
                btnInactivarSelectedTop.setManaged(true);
                btnReactivarSelectedTop.setVisible(false);
                btnReactivarSelectedTop.setManaged(false);
                btnInactivarSelectedTop.setDisable(!puedeInactivar);
            } else {
                btnInactivarSelectedTop.setVisible(false);
                btnInactivarSelectedTop.setManaged(false);
                btnReactivarSelectedTop.setVisible(true);
                btnReactivarSelectedTop.setManaged(true);
                btnReactivarSelectedTop.setDisable(!puedeInactivar);
            }
        } catch (Exception ex) {
            btnInactivarSelectedTop.setDisable(true);
            btnReactivarSelectedTop.setDisable(true);
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
        if (lblDocError != null) {
            lblDocError.setText("");
            lblDocError.setVisible(false);
        }
        // Reset modo edición
        this.editingId = null;
        if (btnGuardar != null) btnGuardar.setText("Guardar");
    }
}

