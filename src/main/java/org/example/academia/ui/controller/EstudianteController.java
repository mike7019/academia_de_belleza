package org.example.academia.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.example.academia.dto.EstudianteDTO;
import org.example.academia.security.AuthException;
import org.example.academia.service.BusinessException;
import org.example.academia.service.EstudianteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el módulo de Estudiantes.
 *
 * Mantiene el mismo diseño operativo que Maestros: listado + diálogo CRUD.
 */
public class EstudianteController {

    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";
    private static final String ESTADO_TODOS = "Todos";
    private static final int PAGE_SIZE = 20;

    @FXML
    private TableView<EstudianteDTO> estudiantesTable;

    @FXML
    private TableColumn<EstudianteDTO, String> nombreColumn;

    @FXML
    private TableColumn<EstudianteDTO, String> apellidoColumn;

    @FXML
    private TableColumn<EstudianteDTO, String> documentoColumn;

    @FXML
    private TableColumn<EstudianteDTO, String> telefonoColumn;

    @FXML
    private TableColumn<EstudianteDTO, String> emailColumn;

    @FXML
    private TableColumn<EstudianteDTO, Boolean> activoColumn;

    @FXML
    private TextField filtroNombreField;

    @FXML
    private TextField filtroDocumentoField;

    @FXML
    private ComboBox<String> filtroEstadoCombo;

    @FXML
    private Pagination paginacionEstudiantes;

    @FXML
    private Label resumenPaginaLabel;

    private final EstudianteService estudianteService;
    private final ObservableList<EstudianteDTO> estudiantesList;
    private final List<EstudianteDTO> estudiantesFiltrados;
    private boolean actualizandoPaginacion;

    public EstudianteController() {
        this.estudianteService = new EstudianteService();
        this.estudiantesList = FXCollections.observableArrayList();
        this.estudiantesFiltrados = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        filtroEstadoCombo.setItems(FXCollections.observableArrayList(ESTADO_ACTIVOS, ESTADO_INACTIVOS, ESTADO_TODOS));
        filtroEstadoCombo.setValue(ESTADO_ACTIVOS);

        // Filtrado reactivo: refresca listado mientras se escribe.
        filtroNombreField.textProperty().addListener((obs, oldVal, newVal) -> cargarEstudiantes(0));
        filtroDocumentoField.textProperty().addListener((obs, oldVal, newVal) -> cargarEstudiantes(0));
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarEstudiantes(0));

        paginacionEstudiantes.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoPaginacion) {
                renderizarPagina(newVal.intValue());
            }
        });

        estudiantesTable.setItems(estudiantesList);
        cargarEstudiantes(0);
    }

    @FXML
    private void onNuevoEstudiante() {
        EstudianteDTO nuevo = new EstudianteDTO();
        nuevo.setTipoDocumento("CC");
        nuevo.setActivo(true);

        showEstudianteDialog(nuevo, false).ifPresent(dto -> {
            try {
                estudianteService.guardarEstudiante(dto);
                cargarEstudiantes(0);
                showInfo("Estudiante creado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditarEstudiante() {
        EstudianteDTO seleccionado = estudiantesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un estudiante para editar.");
            return;
        }

        EstudianteDTO editable = clonarEstudiante(seleccionado);
        showEstudianteDialog(editable, true).ifPresent(dto -> {
            try {
                estudianteService.actualizarEstudiante(dto);
                cargarEstudiantes(0);
                showInfo("Estudiante actualizado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onInactivarEstudiante() {
        EstudianteDTO seleccionado = estudiantesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un estudiante para inactivar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de inactivar al estudiante " + seleccionado.getNombre() + " " + seleccionado.getApellido() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setHeaderText("Confirmar inactivación");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            try {
                estudianteService.inactivarEstudiante(seleccionado.getId());
                int paginaActual = paginacionEstudiantes != null ? paginacionEstudiantes.getCurrentPageIndex() : 0;
                cargarEstudiantes(paginaActual);
                showInfo("Estudiante inactivado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        }
    }

    @FXML
    private void onFiltrarEstudiantes() {
        cargarEstudiantes(0);
    }

    @FXML
    private void onLimpiarFiltros() {
        filtroNombreField.clear();
        filtroDocumentoField.clear();
        filtroEstadoCombo.setValue(ESTADO_ACTIVOS);
        cargarEstudiantes(0);
    }

    private Optional<EstudianteDTO> showEstudianteDialog(EstudianteDTO base, boolean edicion) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edicion ? "Editar estudiante" : "Nuevo estudiante");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField nombreField = new TextField(base.getNombre());
        TextField apellidoField = new TextField(base.getApellido());
        ComboBox<String> tipoDocumentoCombo = new ComboBox<>(FXCollections.observableArrayList("CC", "CE", "TI", "PASAPORTE"));
        tipoDocumentoCombo.setEditable(true);
        tipoDocumentoCombo.setValue(base.getTipoDocumento() != null ? base.getTipoDocumento() : "CC");
        TextField documentoField = new TextField(base.getNumeroDocumento());
        TextField telefonoField = new TextField(base.getTelefono());
        TextField emailField = new TextField(base.getEmail());
        TextField direccionField = new TextField(base.getDireccion());

        grid.add(new Label("Nombre*"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellido*"), 0, 1);
        grid.add(apellidoField, 1, 1);
        grid.add(new Label("Tipo documento*"), 0, 2);
        grid.add(tipoDocumentoCombo, 1, 2);
        grid.add(new Label("Documento*"), 0, 3);
        grid.add(documentoField, 1, 3);
        grid.add(new Label("Teléfono"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Email"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("Dirección"), 0, 6);
        grid.add(direccionField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get() != guardarBtn) {
            return Optional.empty();
        }

        try {
            EstudianteDTO dto = new EstudianteDTO();
            dto.setId(base.getId());
            dto.setActivo(base.isActivo());
            dto.setFechaRegistro(base.getFechaRegistro());
            dto.setFechaBaja(base.getFechaBaja());
            dto.setNombre(requerido(nombreField.getText(), "El nombre es obligatorio"));
            dto.setApellido(requerido(apellidoField.getText(), "El apellido es obligatorio"));
            dto.setTipoDocumento(requerido(tipoDocumentoCombo.getEditor().getText(), "El tipo de documento es obligatorio"));
            dto.setNumeroDocumento(requerido(documentoField.getText(), "El documento es obligatorio"));
            dto.setTelefono(vacioANull(telefonoField.getText()));
            dto.setEmail(vacioANull(emailField.getText()));
            dto.setDireccion(vacioANull(direccionField.getText()));
            return Optional.of(dto);
        } catch (BusinessException ex) {
            showError(ex.getMessage());
            return Optional.empty();
        }
    }

    private String requerido(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new BusinessException(mensaje);
        }
        return valor.trim();
    }

    private String vacioANull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private EstudianteDTO clonarEstudiante(EstudianteDTO origen) {
        EstudianteDTO copia = new EstudianteDTO();
        copia.setId(origen.getId());
        copia.setNombre(origen.getNombre());
        copia.setApellido(origen.getApellido());
        copia.setTipoDocumento(origen.getTipoDocumento());
        copia.setNumeroDocumento(origen.getNumeroDocumento());
        copia.setTelefono(origen.getTelefono());
        copia.setEmail(origen.getEmail());
        copia.setDireccion(origen.getDireccion());
        copia.setActivo(origen.isActivo());
        copia.setFechaRegistro(origen.getFechaRegistro());
        copia.setFechaBaja(origen.getFechaBaja());
        return copia;
    }

    private void cargarEstudiantes() {
        cargarEstudiantes(0);
    }

    private void cargarEstudiantes(int paginaPreferida) {
        estudiantesList.clear();
        estudiantesFiltrados.clear();
        try {
            String nombre = valorFiltro(filtroNombreField.getText());
            String documento = valorFiltro(filtroDocumentoField.getText());
            Boolean activo = obtenerEstadoFiltro();

            estudiantesFiltrados.addAll(estudianteService.listarEstudiantes(nombre, documento, activo));
            actualizarPaginacion(paginaPreferida);
        } catch (BusinessException | AuthException ex) {
            showError(ex.getMessage());
            actualizarPaginacion(0);
        }
    }

    private void actualizarPaginacion(int paginaPreferida) {
        int total = estudiantesFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int paginaObjetivo = Math.max(0, Math.min(paginaPreferida, totalPaginas - 1));

        actualizandoPaginacion = true;
        paginacionEstudiantes.setPageCount(totalPaginas);
        paginacionEstudiantes.setCurrentPageIndex(paginaObjetivo);
        actualizandoPaginacion = false;

        renderizarPagina(paginaObjetivo);
    }

    private void renderizarPagina(int pageIndex) {
        estudiantesList.clear();

        int total = estudiantesFiltrados.size();
        if (total == 0) {
            resumenPaginaLabel.setText("Mostrando 0 de 0");
            return;
        }

        int fromIndex = pageIndex * PAGE_SIZE;
        if (fromIndex >= total) {
            fromIndex = Math.max(0, (total - 1) / PAGE_SIZE * PAGE_SIZE);
            pageIndex = fromIndex / PAGE_SIZE;
            actualizandoPaginacion = true;
            paginacionEstudiantes.setCurrentPageIndex(pageIndex);
            actualizandoPaginacion = false;
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        estudiantesList.addAll(estudiantesFiltrados.subList(fromIndex, toIndex));
        resumenPaginaLabel.setText("Mostrando " + (fromIndex + 1) + "-" + toIndex + " de " + total);
    }

    private String valorFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private Boolean obtenerEstadoFiltro() {
        String estado = filtroEstadoCombo.getValue();
        if (ESTADO_ACTIVOS.equals(estado)) {
            return true;
        }
        if (ESTADO_INACTIVOS.equals(estado)) {
            return false;
        }
        return null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Información");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

