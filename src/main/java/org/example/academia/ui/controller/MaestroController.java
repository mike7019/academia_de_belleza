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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.example.academia.domain.enums.TipoPagoProfesor;
import org.example.academia.dto.MaestroDTO;
import org.example.academia.mapper.MaestroMapper;
import org.example.academia.security.AuthException;
import org.example.academia.service.MaestroService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controlador para el módulo de Maestros/Profesores.
 *
 * Gestiona listado, creación, edición e inactivación de maestros.
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

    private final MaestroService maestroService;
    private final ObservableList<MaestroDTO> maestrosList;

    public MaestroController() {
        this.maestroService = new MaestroService();
        this.maestrosList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        tipoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPagoProfesor"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        maestrosTable.setItems(maestrosList);
        cargarMaestros();
    }

    @FXML
    private void onNuevoMaestro() {
        MaestroDTO nuevo = new MaestroDTO();
        nuevo.setTipoDocumento("CC");
        nuevo.setActivo(true);

        showMaestroDialog(nuevo, false).ifPresent(dto -> {
            try {
                maestroService.guardarMaestro(MaestroMapper.toEntity(dto));
                cargarMaestros();
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
                cargarMaestros();
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
                "¿Está seguro de inactivar al maestro " + seleccionado.getNombre() + " " + seleccionado.getApellido() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setHeaderText("Confirmar inactivación");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            try {
                maestroService.inactivarMaestro(seleccionado.getId());
                cargarMaestros();
                showInfo("Maestro inactivado correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        }
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
        grid.add(new Label("Teléfono"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Email"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("Dirección"), 0, 6);
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

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get() != guardarBtn) {
            return Optional.empty();
        }

        try {
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

            switch (tipoPago) {
                case POR_HORA:
                    dto.setTarifaHora(decimalRequerido(tarifaHoraField.getText(), "Tarifa por hora"));
                    break;
                case FIJO_MENSUAL:
                    dto.setSalarioMensual(decimalRequerido(salarioMensualField.getText(), "Salario mensual"));
                    break;
                case POR_CURSO:
                    dto.setTarifaPorCurso(decimalRequerido(tarifaPorCursoField.getText(), "Tarifa por curso"));
                    break;
                case PORCENTAJE:
                    dto.setPorcentajePorCurso(decimalRequerido(porcentajeField.getText(), "Porcentaje"));
                    break;
                default:
                    throw new BusinessException("Modalidad de pago no soportada");
            }

            return Optional.of(dto);
        } catch (BusinessException ex) {
            showError(ex.getMessage());
            return Optional.empty();
        }
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
            throw new BusinessException("Formato numérico inválido en " + nombreCampo);
        }
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
        maestrosList.clear();
        try {
            maestroService.listarMaestrosActivos().forEach(maestro -> maestrosList.add(MaestroMapper.toDTO(maestro)));
        } catch (BusinessException | AuthException ex) {
            showError(ex.getMessage());
        }
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
