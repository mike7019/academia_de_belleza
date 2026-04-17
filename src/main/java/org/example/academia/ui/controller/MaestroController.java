package org.example.academia.ui.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.academia.domain.enums.TipoPagoProfesor;
import org.example.academia.dto.MaestroDTO;
import org.example.academia.mapper.MaestroMapper;
import org.example.academia.security.AuthException;
import org.example.academia.service.MaestroService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el modulo de Maestros/Profesores.
 *
 * Gestiona listado, creacion, edicion e inactivacion de maestros.
 */
public class MaestroController {

    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";
    private static final String ESTADO_TODOS = "Todos";
    private static final int PAGE_SIZE = 20;

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

    @FXML
    private TextField filtroNombreField;

    @FXML
    private TextField filtroDocumentoField;

    @FXML
    private ComboBox<String> filtroEstadoCombo;

    @FXML
    private Pagination paginacionMaestros;

    @FXML
    private Label resumenPaginaLabel;

    private final MaestroService maestroService;
    private final ObservableList<MaestroDTO> maestrosList;
    private final List<MaestroDTO> maestrosFiltrados;

    private Tooltip notificacionTooltip;
    private PauseTransition ocultarNotificacion;
    private boolean actualizandoPaginacion;

    public MaestroController() {
        this.maestroService = new MaestroService();
        this.maestrosList = FXCollections.observableArrayList();
        this.maestrosFiltrados = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        tipoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPagoProfesor"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        filtroEstadoCombo.setItems(FXCollections.observableArrayList(ESTADO_ACTIVOS, ESTADO_INACTIVOS, ESTADO_TODOS));
        filtroEstadoCombo.setValue(ESTADO_ACTIVOS);

        filtroNombreField.textProperty().addListener((obs, oldVal, newVal) -> cargarMaestros(0));
        filtroDocumentoField.textProperty().addListener((obs, oldVal, newVal) -> cargarMaestros(0));
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarMaestros(0));

        paginacionMaestros.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoPaginacion) {
                renderizarPagina(newVal.intValue());
            }
        });

        notificacionTooltip = new Tooltip();
        notificacionTooltip.setAutoHide(true);

        maestrosTable.setItems(maestrosList);
        cargarMaestros(0);
    }

    @FXML
    private void onNuevoMaestro() {
        MaestroDTO nuevo = new MaestroDTO();
        nuevo.setTipoDocumento("CC");
        nuevo.setActivo(true);

        showMaestroDialog(nuevo, false).ifPresent(dto -> {
            try {
                maestroService.guardarMaestro(MaestroMapper.toEntity(dto));
                cargarMaestros(0);
                showInfo("Maestro creado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onEditarMaestro() {
        MaestroDTO seleccionado = maestrosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un maestro para editar.");
            return;
        }

        MaestroDTO editable = clonarMaestro(seleccionado);
        showMaestroDialog(editable, true).ifPresent(dto -> {
            try {
                maestroService.actualizarMaestro(MaestroMapper.toEntity(dto));
                cargarMaestros(0);
                showInfo("Maestro actualizado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onInactivarMaestro() {
        MaestroDTO seleccionado = maestrosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un maestro para inactivar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Esta seguro de inactivar al maestro " + seleccionado.getNombre() + " " + seleccionado.getApellido() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setHeaderText("Confirmar inactivacion");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            try {
                maestroService.inactivarMaestro(seleccionado.getId());
                int paginaActual = paginacionMaestros != null ? paginacionMaestros.getCurrentPageIndex() : 0;
                cargarMaestros(paginaActual);
                showInfo("Maestro inactivado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        }
    }

    @FXML
    private void onFiltrarMaestros() {
        cargarMaestros(0);
    }

    @FXML
    private void onLimpiarFiltros() {
        filtroNombreField.clear();
        filtroDocumentoField.clear();
        cargarMaestros(0);
    }

    private Optional<MaestroDTO> showMaestroDialog(MaestroDTO base, boolean edicion) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(edicion ? "Editar maestro" : "Nuevo maestro");

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

        ComboBox<TipoPagoProfesor> tipoPagoCombo = new ComboBox<>(FXCollections.observableArrayList(TipoPagoProfesor.values()));
        tipoPagoCombo.setValue(base.getTipoPagoProfesor());

        TextField tarifaHoraField = new TextField(base.getTarifaHora() != null ? base.getTarifaHora().toPlainString() : "");
        TextField salarioMensualField = new TextField(base.getSalarioMensual() != null ? base.getSalarioMensual().toPlainString() : "");
        TextField tarifaPorCursoField = new TextField(base.getTarifaPorCurso() != null ? base.getTarifaPorCurso().toPlainString() : "");
        TextField porcentajeField = new TextField(base.getPorcentajePorCurso() != null ? base.getPorcentajePorCurso().toPlainString() : "");

        grid.add(new Label("Nombre*"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellido*"), 0, 1);
        grid.add(apellidoField, 1, 1);
        grid.add(new Label("Tipo documento*"), 0, 2);
        grid.add(tipoDocumentoCombo, 1, 2);
        grid.add(new Label("Documento*"), 0, 3);
        grid.add(documentoField, 1, 3);
        grid.add(new Label("Telefono"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Email"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("Direccion"), 0, 6);
        grid.add(direccionField, 1, 6);
        grid.add(new Label("Tipo pago*"), 0, 7);
        grid.add(tipoPagoCombo, 1, 7);

        Label tarifaHoraLabel = new Label("Tarifa por hora");
        Label salarioMensualLabel = new Label("Salario mensual");
        Label tarifaPorCursoLabel = new Label("Tarifa por curso");
        Label porcentajeLabel = new Label("Porcentaje");

        grid.add(tarifaHoraLabel, 0, 8);
        grid.add(tarifaHoraField, 1, 8);
        grid.add(salarioMensualLabel, 0, 9);
        grid.add(salarioMensualField, 1, 9);
        grid.add(tarifaPorCursoLabel, 0, 10);
        grid.add(tarifaPorCursoField, 1, 10);
        grid.add(porcentajeLabel, 0, 11);
        grid.add(porcentajeField, 1, 11);

        Label ayudaPagoLabel = new Label("Seleccione el tipo de pago para mostrar el campo correspondiente.");
        ayudaPagoLabel.setWrapText(true);
        grid.add(ayudaPagoLabel, 0, 12, 2, 1);

        Label errorFormularioLabel = new Label();
        errorFormularioLabel.setWrapText(true);
        errorFormularioLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(errorFormularioLabel, 0, 13, 2, 1);

        tipoPagoCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                actualizarCamposPago(
                        newVal,
                        tarifaHoraLabel,
                        tarifaHoraField,
                        salarioMensualLabel,
                        salarioMensualField,
                        tarifaPorCursoLabel,
                        tarifaPorCursoField,
                        porcentajeLabel,
                        porcentajeField,
                        ayudaPagoLabel
                ));
        actualizarCamposPago(
                tipoPagoCombo.getValue(),
                tarifaHoraLabel,
                tarifaHoraField,
                salarioMensualLabel,
                salarioMensualField,
                tarifaPorCursoLabel,
                tarifaPorCursoField,
                porcentajeLabel,
                porcentajeField,
                ayudaPagoLabel
        );

        dialog.getDialogPane().setContent(grid);

        Button guardarButton = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        final MaestroDTO[] dtoResultado = new MaestroDTO[1];

        Runnable refrescarEstadoGuardado = () -> {
            String error = validarFormularioMaestro(
                    nombreField,
                    apellidoField,
                    tipoDocumentoCombo,
                    documentoField,
                    tipoPagoCombo,
                    tarifaHoraField,
                    salarioMensualField,
                    tarifaPorCursoField,
                    porcentajeField
            );
            boolean invalido = error != null;
            guardarButton.setDisable(invalido);
            errorFormularioLabel.setText(invalido ? error : "");
        };

        nombreField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        apellidoField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        tipoDocumentoCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        documentoField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        tipoPagoCombo.valueProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        tarifaHoraField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        salarioMensualField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        tarifaPorCursoField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());
        porcentajeField.textProperty().addListener((obs, oldVal, newVal) -> refrescarEstadoGuardado.run());

        guardarButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                dtoResultado[0] = construirMaestroDesdeFormulario(
                        base,
                        nombreField,
                        apellidoField,
                        tipoDocumentoCombo,
                        documentoField,
                        telefonoField,
                        emailField,
                        direccionField,
                        tipoPagoCombo,
                        tarifaHoraField,
                        salarioMensualField,
                        tarifaPorCursoField,
                        porcentajeField
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

    private MaestroDTO construirMaestroDesdeFormulario(MaestroDTO base,
                                                       TextField nombreField,
                                                       TextField apellidoField,
                                                       ComboBox<String> tipoDocumentoCombo,
                                                       TextField documentoField,
                                                       TextField telefonoField,
                                                       TextField emailField,
                                                       TextField direccionField,
                                                       ComboBox<TipoPagoProfesor> tipoPagoCombo,
                                                       TextField tarifaHoraField,
                                                       TextField salarioMensualField,
                                                       TextField tarifaPorCursoField,
                                                       TextField porcentajeField) {
        MaestroDTO dto = new MaestroDTO();
        dto.setId(base.getId());
        dto.setActivo(base.isActivo());
        dto.setNombre(requerido(nombreField.getText(), "El nombre es obligatorio"));
        dto.setApellido(requerido(apellidoField.getText(), "El apellido es obligatorio"));
        dto.setTipoDocumento(requerido(tipoDocumentoCombo.getEditor().getText(), "El tipo de documento es obligatorio"));
        dto.setNumeroDocumento(requerido(documentoField.getText(), "El documento es obligatorio"));
        dto.setTelefono(vacioANull(telefonoField.getText()));
        dto.setEmail(vacioANull(emailField.getText()));
        dto.setDireccion(vacioANull(direccionField.getText()));

        TipoPagoProfesor tipoPago = tipoPagoCombo.getValue();
        if (tipoPago == null) {
            throw new BusinessException("Debe seleccionar una modalidad de pago");
        }
        dto.setTipoPagoProfesor(tipoPago);

        dto.setTarifaHora(null);
        dto.setSalarioMensual(null);
        dto.setTarifaPorCurso(null);
        dto.setPorcentajePorCurso(null);

        switch (tipoPago) {
            case POR_HORA:
                dto.setTarifaHora(decimalPositivoRequerido(tarifaHoraField.getText(), "Tarifa por hora"));
                break;
            case FIJO_MENSUAL:
                dto.setSalarioMensual(decimalPositivoRequerido(salarioMensualField.getText(), "Salario mensual"));
                break;
            case POR_CURSO:
                dto.setTarifaPorCurso(decimalPositivoRequerido(tarifaPorCursoField.getText(), "Tarifa por curso"));
                break;
            case PORCENTAJE:
                dto.setPorcentajePorCurso(porcentajeRequerido(porcentajeField.getText(), "Porcentaje"));
                break;
            default:
                throw new BusinessException("Modalidad de pago no soportada");
        }
        return dto;
    }

    private String validarFormularioMaestro(TextField nombreField,
                                            TextField apellidoField,
                                            ComboBox<String> tipoDocumentoCombo,
                                            TextField documentoField,
                                            ComboBox<TipoPagoProfesor> tipoPagoCombo,
                                            TextField tarifaHoraField,
                                            TextField salarioMensualField,
                                            TextField tarifaPorCursoField,
                                            TextField porcentajeField) {
        if (nombreField.getText() == null || nombreField.getText().isBlank()) {
            return "El nombre es obligatorio";
        }
        if (apellidoField.getText() == null || apellidoField.getText().isBlank()) {
            return "El apellido es obligatorio";
        }
        if (tipoDocumentoCombo.getEditor().getText() == null || tipoDocumentoCombo.getEditor().getText().isBlank()) {
            return "El tipo de documento es obligatorio";
        }
        if (documentoField.getText() == null || documentoField.getText().isBlank()) {
            return "El documento es obligatorio";
        }

        TipoPagoProfesor tipoPago = tipoPagoCombo.getValue();
        if (tipoPago == null) {
            return "Debe seleccionar una modalidad de pago";
        }

        switch (tipoPago) {
            case POR_HORA:
                return validarDecimalPositivo(tarifaHoraField.getText(), "Tarifa por hora");
            case FIJO_MENSUAL:
                return validarDecimalPositivo(salarioMensualField.getText(), "Salario mensual");
            case POR_CURSO:
                return validarDecimalPositivo(tarifaPorCursoField.getText(), "Tarifa por curso");
            case PORCENTAJE:
                return validarPorcentaje(porcentajeField.getText(), "Porcentaje");
            default:
                return "Modalidad de pago no soportada";
        }
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

    private String validarPorcentaje(String valor, String nombreCampo) {
        String error = validarDecimalPositivo(valor, nombreCampo);
        if (error != null) {
            return error;
        }
        BigDecimal porcentaje = new BigDecimal(valor.trim());
        if (porcentaje.compareTo(new BigDecimal("100")) > 0) {
            return nombreCampo + " no puede superar 100";
        }
        return null;
    }

    private void actualizarCamposPago(TipoPagoProfesor tipoPago,
                                      Label tarifaHoraLabel,
                                      TextField tarifaHoraField,
                                      Label salarioMensualLabel,
                                      TextField salarioMensualField,
                                      Label tarifaPorCursoLabel,
                                      TextField tarifaPorCursoField,
                                      Label porcentajeLabel,
                                      TextField porcentajeField,
                                      Label ayudaPagoLabel) {
        boolean esPorHora = tipoPago == TipoPagoProfesor.POR_HORA;
        boolean esFijo = tipoPago == TipoPagoProfesor.FIJO_MENSUAL;
        boolean esPorCurso = tipoPago == TipoPagoProfesor.POR_CURSO;
        boolean esPorcentaje = tipoPago == TipoPagoProfesor.PORCENTAJE;

        togglePagoField(tarifaHoraLabel, tarifaHoraField, esPorHora);
        togglePagoField(salarioMensualLabel, salarioMensualField, esFijo);
        togglePagoField(tarifaPorCursoLabel, tarifaPorCursoField, esPorCurso);
        togglePagoField(porcentajeLabel, porcentajeField, esPorcentaje);

        if (!esPorHora) {
            tarifaHoraField.clear();
        }
        if (!esFijo) {
            salarioMensualField.clear();
        }
        if (!esPorCurso) {
            tarifaPorCursoField.clear();
        }
        if (!esPorcentaje) {
            porcentajeField.clear();
        }

        ayudaPagoLabel.setText(tipoPago == null
                ? "Seleccione el tipo de pago para mostrar el campo correspondiente."
                : "Ingrese solo el valor de " + tipoPago.name() + ".");
    }

    private void togglePagoField(Label label, TextField field, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        field.setVisible(visible);
        field.setManaged(visible);
        field.setDisable(!visible);
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

    private BigDecimal decimalRequerido(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new BusinessException(nombreCampo + " es obligatorio");
        }
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException("Formato numerico invalido en " + nombreCampo);
        }
    }

    private BigDecimal decimalPositivoRequerido(String valor, String nombreCampo) {
        BigDecimal decimal = decimalRequerido(valor, nombreCampo);
        if (decimal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(nombreCampo + " debe ser mayor a 0");
        }
        return decimal;
    }

    private BigDecimal porcentajeRequerido(String valor, String nombreCampo) {
        BigDecimal porcentaje = decimalPositivoRequerido(valor, nombreCampo);
        if (porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(nombreCampo + " no puede superar 100");
        }
        return porcentaje;
    }

    private MaestroDTO clonarMaestro(MaestroDTO origen) {
        MaestroDTO copia = new MaestroDTO();
        copia.setId(origen.getId());
        copia.setNombre(origen.getNombre());
        copia.setApellido(origen.getApellido());
        copia.setTipoDocumento(origen.getTipoDocumento());
        copia.setNumeroDocumento(origen.getNumeroDocumento());
        copia.setTelefono(origen.getTelefono());
        copia.setEmail(origen.getEmail());
        copia.setDireccion(origen.getDireccion());
        copia.setTipoPagoProfesor(origen.getTipoPagoProfesor());
        copia.setTarifaHora(origen.getTarifaHora());
        copia.setSalarioMensual(origen.getSalarioMensual());
        copia.setTarifaPorCurso(origen.getTarifaPorCurso());
        copia.setPorcentajePorCurso(origen.getPorcentajePorCurso());
        copia.setActivo(origen.isActivo());
        return copia;
    }

    private void cargarMaestros() {
        cargarMaestros(0);
    }

    private void cargarMaestros(int paginaPreferida) {
        maestrosList.clear();
        maestrosFiltrados.clear();
        try {
            String nombre = valorFiltro(filtroNombreField.getText());
            String documento = valorFiltro(filtroDocumentoField.getText());
            Boolean activo = obtenerEstadoFiltro();

            maestroService.listarMaestros(nombre, documento, activo)
                    .forEach(maestro -> maestrosFiltrados.add(MaestroMapper.toDTO(maestro)));

            actualizarPaginacion(paginaPreferida);
        } catch (BusinessException | AuthException ex) {
            showError(ex.getMessage());
            actualizarPaginacion(0);
        }
    }

    private void actualizarPaginacion(int paginaPreferida) {
        int total = maestrosFiltrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int paginaObjetivo = Math.max(0, Math.min(paginaPreferida, totalPaginas - 1));

        actualizandoPaginacion = true;
        paginacionMaestros.setPageCount(totalPaginas);
        paginacionMaestros.setCurrentPageIndex(paginaObjetivo);
        actualizandoPaginacion = false;

        renderizarPagina(paginaObjetivo);
    }

    private void renderizarPagina(int pageIndex) {
        maestrosList.clear();

        int total = maestrosFiltrados.size();
        if (total == 0) {
            resumenPaginaLabel.setText("Mostrando 0 de 0");
            return;
        }

        int fromIndex = pageIndex * PAGE_SIZE;
        if (fromIndex >= total) {
            fromIndex = Math.max(0, (total - 1) / PAGE_SIZE * PAGE_SIZE);
            pageIndex = fromIndex / PAGE_SIZE;
            actualizandoPaginacion = true;
            paginacionMaestros.setCurrentPageIndex(pageIndex);
            actualizandoPaginacion = false;
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        maestrosList.addAll(maestrosFiltrados.subList(fromIndex, toIndex));
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
        mostrarNotificacion(message, true);
    }

    private void showInfo(String message) {
        mostrarNotificacion(message, false);
    }

    private void mostrarNotificacion(String message, boolean esError) {
        if (maestrosTable == null || maestrosTable.getScene() == null) {
            return;
        }

        Window window = maestrosTable.getScene().getWindow();
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
}
