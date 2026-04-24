package org.example.academia.ui.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.example.academia.domain.entity.Maestro;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.dto.CursoDTO;
import org.example.academia.security.AuthException;
import org.example.academia.service.CursoService;
import org.example.academia.service.MaestroService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador del modulo Cursos.
 */
public class CursoController {

    private static final String ESTADO_TODOS = "Todos";
    private static final int PAGE_SIZE = 20;

    @FXML
    private TableView<CursoDTO> cursosTable;

    @FXML
    private TableColumn<CursoDTO, String> nombreColumn;

    @FXML
    private TableColumn<CursoDTO, String> maestroColumn;

    @FXML
    private TableColumn<CursoDTO, EstadoCurso> estadoColumn;

    @FXML
    private TableColumn<CursoDTO, LocalDate> fechaInicioColumn;

    @FXML
    private TableColumn<CursoDTO, LocalDate> fechaFinColumn;

    @FXML
    private TableColumn<CursoDTO, Integer> cupoMaximoColumn;

    @FXML
    private TableColumn<CursoDTO, Integer> cuposOcupadosColumn;

    @FXML
    private TableColumn<CursoDTO, Integer> cuposDisponiblesColumn;

    @FXML
    private TableColumn<CursoDTO, BigDecimal> precioBaseColumn;

    @FXML
    private TextField filtroNombreField;

    @FXML
    private ComboBox<String> filtroEstadoCombo;

    @FXML
    private DatePicker filtroFechaInicioPicker;

    @FXML
    private DatePicker filtroFechaFinPicker;

    @FXML
    private Pagination paginacionCursos;

    @FXML
    private Label resumenPaginaLabel;

    private final CursoService cursoService;
    private final MaestroService maestroService;
    private final ObservableList<CursoDTO> cursosList;
    private final List<CursoDTO> cursosFiltrados;

    private Tooltip notificacionTooltip;
    private PauseTransition ocultarNotificacion;
    private boolean actualizandoPaginacion;

    public CursoController() {
        this.cursoService = new CursoService();
        this.maestroService = new MaestroService();
        this.cursosList = FXCollections.observableArrayList();
        this.cursosFiltrados = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        maestroColumn.setCellValueFactory(new PropertyValueFactory<>("maestroNombre"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        fechaInicioColumn.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        fechaFinColumn.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        cupoMaximoColumn.setCellValueFactory(new PropertyValueFactory<>("cupoMaximo"));
        cuposOcupadosColumn.setCellValueFactory(new PropertyValueFactory<>("cuposOcupados"));
        cuposDisponiblesColumn.setCellValueFactory(new PropertyValueFactory<>("cuposDisponibles"));
        precioBaseColumn.setCellValueFactory(new PropertyValueFactory<>("precioBase"));

        ObservableList<String> estados = FXCollections.observableArrayList(ESTADO_TODOS);
        for (EstadoCurso estado : EstadoCurso.values()) {
            estados.add(estado.name());
        }
        filtroEstadoCombo.setItems(estados);
        filtroEstadoCombo.setValue(ESTADO_TODOS);

        filtroNombreField.textProperty().addListener((obs, oldVal, newVal) -> cargarCursos(0));
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarCursos(0));
        filtroFechaInicioPicker.valueProperty().addListener((obs, oldVal, newVal) -> cargarCursos(0));
        filtroFechaFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> cargarCursos(0));

        paginacionCursos.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoPaginacion) {
                renderizarPagina(newVal.intValue());
            }
        });

        notificacionTooltip = new Tooltip();
        notificacionTooltip.setAutoHide(true);

        cursosTable.setItems(cursosList);
        cargarCursos(0);
    }

    @FXML
    private void onNuevoCurso() {
        CursoDTO nuevo = new CursoDTO();
        nuevo.setEstado(EstadoCurso.PLANIFICADO);
        nuevo.setFechaInicio(LocalDate.now());
        nuevo.setFechaFin(LocalDate.now().plusMonths(1));

        showCursoDialog(nuevo, false).ifPresent(dto -> {
            try {
                cursoService.crearCurso(dto);
                cargarCursos(0);
                showInfo("Curso creado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditarCurso() {
        CursoDTO seleccionado = cursosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un curso para editar.");
            return;
        }

        CursoDTO editable = clonarCurso(seleccionado);
        showCursoDialog(editable, true).ifPresent(dto -> {
            try {
                cursoService.actualizarCurso(dto);
                int paginaActual = paginacionCursos != null ? paginacionCursos.getCurrentPageIndex() : 0;
                cargarCursos(paginaActual);
                showInfo("Curso actualizado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onCambiarEstadoCurso() {
        CursoDTO seleccionado = cursosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un curso para cambiar estado.");
            return;
        }

        List<EstadoCurso> posibles = obtenerSiguientesEstados(seleccionado.getEstado());
        if (posibles.isEmpty()) {
            showError("El curso no tiene transiciones disponibles desde " + seleccionado.getEstado());
            return;
        }

        ChoiceDialog<EstadoCurso> dialog = new ChoiceDialog<>(posibles.get(0), posibles);
        dialog.setTitle("Cambiar estado de curso");
        dialog.setHeaderText("Curso: " + seleccionado.getNombre());
        dialog.setContentText("Nuevo estado:");

        Optional<EstadoCurso> elegido = dialog.showAndWait();
        if (elegido.isEmpty()) {
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Confirmar cambio de estado a " + elegido.get().name() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setHeaderText("Confirmacion");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            try {
                cursoService.cambiarEstado(seleccionado.getId(), elegido.get());
                int paginaActual = paginacionCursos != null ? paginacionCursos.getCurrentPageIndex() : 0;
                cargarCursos(paginaActual);
                showInfo("Estado actualizado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        }
    }

    @FXML
    private void onFiltrarCursos() {
        cargarCursos(0);
    }

    @FXML
    private void onLimpiarFiltros() {
        filtroNombreField.clear();
        filtroEstadoCombo.setValue(ESTADO_TODOS);
        filtroFechaInicioPicker.setValue(null);
        filtroFechaFinPicker.setValue(null);
        cargarCursos(0);
    }

    private Optional<CursoDTO> showCursoDialog(CursoDTO base, boolean edicion) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edicion ? "Editar curso" : "Nuevo curso");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField nombreField = new TextField(base.getNombre());
        TextArea descripcionArea = new TextArea(base.getDescripcion());
        descripcionArea.setPrefRowCount(3);

        TextField precioBaseField = new TextField(base.getPrecioBase() != null ? base.getPrecioBase().toPlainString() : "");
        TextField cupoMaximoField = new TextField(base.getCupoMaximo() != null ? String.valueOf(base.getCupoMaximo()) : "");

        DatePicker fechaInicioPicker = new DatePicker(base.getFechaInicio());
        DatePicker fechaFinPicker = new DatePicker(base.getFechaFin());

        ComboBox<MaestroOption> maestroCombo = new ComboBox<>(cargarMaestrosActivos());
        maestroCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(MaestroOption object) {
                return object == null ? "" : object.label;
            }

            @Override
            public MaestroOption fromString(String string) {
                return null;
            }
        });

        if (base.getMaestroId() != null) {
            maestroCombo.getItems().stream()
                    .filter(m -> base.getMaestroId().equals(m.id))
                    .findFirst()
                    .ifPresent(maestroCombo::setValue);
        }

        ComboBox<EstadoCurso> estadoCombo = new ComboBox<>(FXCollections.observableArrayList(EstadoCurso.values()));
        estadoCombo.setValue(base.getEstado() == null ? EstadoCurso.PLANIFICADO : base.getEstado());
        estadoCombo.setDisable(true);

        grid.add(new Label("Nombre*"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripcion"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Precio base*"), 0, 2);
        grid.add(precioBaseField, 1, 2);
        grid.add(new Label("Cupo maximo*"), 0, 3);
        grid.add(cupoMaximoField, 1, 3);
        grid.add(new Label("Fecha inicio*"), 0, 4);
        grid.add(fechaInicioPicker, 1, 4);
        grid.add(new Label("Fecha fin*"), 0, 5);
        grid.add(fechaFinPicker, 1, 5);
        grid.add(new Label("Maestro"), 0, 6);
        grid.add(maestroCombo, 1, 6);
        grid.add(new Label("Estado inicial"), 0, 7);
        grid.add(estadoCombo, 1, 7);

        Label ayudaEstado = new Label(edicion
                ? "El estado se cambia desde el boton 'Cambiar estado'."
                : "El curso se crea en estado PLANIFICADO por defecto."
        );
        ayudaEstado.setWrapText(true);
        grid.add(ayudaEstado, 0, 8, 2, 1);

        Label errorFormularioLabel = new Label();
        errorFormularioLabel.setWrapText(true);
        errorFormularioLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(errorFormularioLabel, 0, 9, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button guardarButton = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        final CursoDTO[] dtoResultado = new CursoDTO[1];

        Runnable refrescarEstadoGuardado = () -> {
            String error = validarFormularioCurso(
                    nombreField,
                    precioBaseField,
                    cupoMaximoField,
                    fechaInicioPicker,
                    fechaFinPicker,
                    estadoCombo,
                    maestroCombo
            );
            boolean invalido = error != null;
            guardarButton.setDisable(invalido);
            errorFormularioLabel.setText(invalido ? error : "");
        };

        nombreField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        precioBaseField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        cupoMaximoField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        fechaInicioPicker.valueProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        fechaFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        estadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        maestroCombo.valueProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());

        guardarButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                dtoResultado[0] = construirCursoDesdeFormulario(
                        base,
                        nombreField,
                        descripcionArea,
                        precioBaseField,
                        cupoMaximoField,
                        fechaInicioPicker,
                        fechaFinPicker,
                        estadoCombo,
                        maestroCombo
                );
            } catch (BusinessException ex) {
                errorFormularioLabel.setText(ex.getMessage());
                event.consume();
            }
        });

        refrescarEstadoGuardado.run();

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get() != guardarBtn) {
            return Optional.empty();
        }

        if (dtoResultado[0] == null) {
            return Optional.empty();
        }
        return Optional.of(dtoResultado[0]);
    }

    private CursoDTO construirCursoDesdeFormulario(CursoDTO base,
                                                   TextField nombreField,
                                                   TextArea descripcionArea,
                                                   TextField precioBaseField,
                                                   TextField cupoMaximoField,
                                                   DatePicker fechaInicioPicker,
                                                   DatePicker fechaFinPicker,
                                                   ComboBox<EstadoCurso> estadoCombo,
                                                   ComboBox<MaestroOption> maestroCombo) {
        CursoDTO dto = new CursoDTO();
        dto.setId(base.getId());
        dto.setNombre(requerido(nombreField.getText(), "El nombre del curso es obligatorio"));
        dto.setDescripcion(vacioANull(descripcionArea.getText()));
        dto.setPrecioBase(decimalPositivoRequerido(precioBaseField.getText(), "Precio base"));
        dto.setCupoMaximo(enteroPositivoRequerido(cupoMaximoField.getText(), "Cupo maximo"));

        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();
        if (fechaInicio == null || fechaFin == null) {
            throw new BusinessException("Debe seleccionar fecha inicio y fecha fin");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new BusinessException("La fecha fin no puede ser menor a la fecha inicio");
        }
        dto.setFechaInicio(fechaInicio);
        dto.setFechaFin(fechaFin);

        EstadoCurso estado = estadoCombo.getValue();
        if (estado == null) {
            estado = EstadoCurso.PLANIFICADO;
        }
        dto.setEstado(estado);

        MaestroOption maestro = maestroCombo.getValue();
        dto.setMaestroId(maestro == null ? null : maestro.id);
        dto.setMaestroNombre(maestro == null ? null : maestro.label);
        return dto;
    }

    private String validarFormularioCurso(TextField nombreField,
                                          TextField precioBaseField,
                                          TextField cupoMaximoField,
                                          DatePicker fechaInicioPicker,
                                          DatePicker fechaFinPicker,
                                          ComboBox<EstadoCurso> estadoCombo,
                                          ComboBox<MaestroOption> maestroCombo) {
        if (nombreField.getText() == null || nombreField.getText().isBlank()) {
            return "El nombre del curso es obligatorio";
        }

        String errorPrecio = validarDecimalPositivo(precioBaseField.getText(), "Precio base");
        if (errorPrecio != null) {
            return errorPrecio;
        }

        String errorCupo = validarEnteroPositivo(cupoMaximoField.getText(), "Cupo maximo");
        if (errorCupo != null) {
            return errorCupo;
        }

        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();
        if (fechaInicio == null || fechaFin == null) {
            return "Debe seleccionar fecha inicio y fecha fin";
        }
        if (fechaFin.isBefore(fechaInicio)) {
            return "La fecha fin no puede ser menor a la fecha inicio";
        }

        EstadoCurso estado = estadoCombo.getValue();
        if (estado == EstadoCurso.ABIERTO && maestroCombo.getValue() == null) {
            return "Para estado ABIERTO debe seleccionar maestro";
        }

        return null;
    }

    private ObservableList<MaestroOption> cargarMaestrosActivos() {
        ObservableList<MaestroOption> maestros = FXCollections.observableArrayList();
        try {
            List<Maestro> activos = maestroService.listarMaestrosActivos();
            for (Maestro maestro : activos) {
                maestros.add(new MaestroOption(
                        maestro.getId(),
                        maestro.getNombre() + " " + maestro.getApellido() + " (" + maestro.getNumeroDocumento() + ")"
                ));
            }
        } catch (BusinessException | AuthException ex) {
            showError("No fue posible cargar maestros activos: " + ex.getMessage());
        }
        return maestros;
    }

    private List<EstadoCurso> obtenerSiguientesEstados(EstadoCurso actual) {
        List<EstadoCurso> estados = new ArrayList<>();
        if (actual == EstadoCurso.PLANIFICADO) {
            estados.add(EstadoCurso.ABIERTO);
            estados.add(EstadoCurso.CANCELADO);
            return estados;
        }
        if (actual == EstadoCurso.ABIERTO) {
            estados.add(EstadoCurso.CERRADO);
            estados.add(EstadoCurso.CANCELADO);
        }
        return estados;
    }

    private CursoDTO clonarCurso(CursoDTO origen) {
        CursoDTO copia = new CursoDTO();
        copia.setId(origen.getId());
        copia.setNombre(origen.getNombre());
        copia.setDescripcion(origen.getDescripcion());
        copia.setPrecioBase(origen.getPrecioBase());
        copia.setCupoMaximo(origen.getCupoMaximo());
        copia.setEstado(origen.getEstado());
        copia.setFechaInicio(origen.getFechaInicio());
        copia.setFechaFin(origen.getFechaFin());
        copia.setMaestroId(origen.getMaestroId());
        copia.setMaestroNombre(origen.getMaestroNombre());
        return copia;
    }

    private void cargarCursos(int paginaPreferida) {
        cursosList.clear();
        cursosFiltrados.clear();
        try {
            String nombre = valorFiltro(filtroNombreField.getText());
            EstadoCurso estado = obtenerEstadoFiltro();
            LocalDate fechaInicio = filtroFechaInicioPicker.getValue();
            LocalDate fechaFin = filtroFechaFinPicker.getValue();

            cursosFiltrados.addAll(cursoService.listarCursos(nombre, estado, fechaInicio, fechaFin));
            actualizarPaginacion(paginaPreferida);
        } catch (BusinessException | AuthException ex) {
            showError(ex.getMessage());
            actualizarPaginacion(0);
        }
    }

    private void actualizarPaginacion(int paginaPreferida) {
        int total = cursosFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int paginaObjetivo = Math.max(0, Math.min(paginaPreferida, totalPaginas - 1));

        actualizandoPaginacion = true;
        paginacionCursos.setPageCount(totalPaginas);
        paginacionCursos.setCurrentPageIndex(paginaObjetivo);
        actualizandoPaginacion = false;

        renderizarPagina(paginaObjetivo);
    }

    private void renderizarPagina(int pageIndex) {
        cursosList.clear();

        int total = cursosFiltrados.size();
        if (total == 0) {
            resumenPaginaLabel.setText("Mostrando 0 de 0");
            return;
        }

        int fromIndex = pageIndex * PAGE_SIZE;
        if (fromIndex >= total) {
            fromIndex = Math.max(0, (total - 1) / PAGE_SIZE * PAGE_SIZE);
            pageIndex = fromIndex / PAGE_SIZE;
            actualizandoPaginacion = true;
            paginacionCursos.setCurrentPageIndex(pageIndex);
            actualizandoPaginacion = false;
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        cursosList.addAll(cursosFiltrados.subList(fromIndex, toIndex));
        resumenPaginaLabel.setText("Mostrando " + (fromIndex + 1) + "-" + toIndex + " de " + total);
    }

    private String valorFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private EstadoCurso obtenerEstadoFiltro() {
        String estado = filtroEstadoCombo.getValue();
        if (estado == null || ESTADO_TODOS.equals(estado)) {
            return null;
        }
        return EstadoCurso.valueOf(estado);
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

    private String validarDecimalPositivo(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            return nombreCampo + " es obligatorio";
        }
        try {
            BigDecimal decimal = new BigDecimal(valor.trim());
            if (decimal.compareTo(BigDecimal.ZERO) <= 0) {
                return nombreCampo + " debe ser mayor a 0";
            }
            return null;
        } catch (NumberFormatException ex) {
            return "Formato numerico invalido en " + nombreCampo;
        }
    }

    private BigDecimal decimalPositivoRequerido(String valor, String nombreCampo) {
        String error = validarDecimalPositivo(valor, nombreCampo);
        if (error != null) {
            throw new BusinessException(error);
        }
        return new BigDecimal(valor.trim());
    }

    private String validarEnteroPositivo(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            return nombreCampo + " es obligatorio";
        }
        try {
            int numero = Integer.parseInt(valor.trim());
            if (numero <= 0) {
                return nombreCampo + " debe ser mayor a 0";
            }
            return null;
        } catch (NumberFormatException ex) {
            return "Formato numerico invalido en " + nombreCampo;
        }
    }

    private int enteroPositivoRequerido(String valor, String nombreCampo) {
        String error = validarEnteroPositivo(valor, nombreCampo);
        if (error != null) {
            throw new BusinessException(error);
        }
        return Integer.parseInt(valor.trim());
    }

    private void showError(String message) {
        mostrarNotificacion(message, true);
    }

    private void showInfo(String message) {
        mostrarNotificacion(message, false);
    }

    private void mostrarNotificacion(String message, boolean esError) {
        if (cursosTable == null || cursosTable.getScene() == null) {
            return;
        }

        Window window = cursosTable.getScene().getWindow();
        if (window == null) {
            return;
        }

        String estiloBase = "-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 12 8 12;";
        String estiloTipo = esError
                ? "-fx-background-color: #d32f2f; -fx-text-fill: white;"
                : "-fx-background-color: #2e7d32; -fx-text-fill: white;";

        notificacionTooltip.hide();
        notificacionTooltip.setText(message);
        notificacionTooltip.setStyle(estiloBase + estiloTipo);

        double x = window.getX() + window.getWidth() - 360;
        double y = window.getY() + window.getHeight() - 90;
        notificacionTooltip.show(window, x, y);

        if (ocultarNotificacion != null) {
            ocultarNotificacion.stop();
        }

        ocultarNotificacion = new PauseTransition(Duration.seconds(3));
        ocultarNotificacion.setOnFinished(event -> notificacionTooltip.hide());
        ocultarNotificacion.play();
    }

    private static class MaestroOption {
        private final Long id;
        private final String label;

        private MaestroOption(Long id, String label) {
            this.id = id;
            this.label = label;
        }
    }
}
