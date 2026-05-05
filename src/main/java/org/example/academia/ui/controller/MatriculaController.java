package org.example.academia.ui.controller;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.example.academia.domain.enums.EstadoMatricula;
import org.example.academia.dto.CursoDTO;
import org.example.academia.dto.EstudianteDTO;
import org.example.academia.dto.MatriculaDTO;
import org.example.academia.security.AuthException;
import org.example.academia.service.CursoService;
import org.example.academia.service.EstudianteService;
import org.example.academia.service.MatriculaService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el módulo de Matrículas.
 *
 * Gestiona creación, visualización y administración de matrículas.
 * USER STORY 115: Crear matrícula
 */
public class MatriculaController {

    private static final int PAGE_SIZE = 15;

    @FXML
    private TableView<MatriculaDTO> matriculasTable;

    @FXML
    private TableColumn<MatriculaDTO, String> estudianteColumn;

    @FXML
    private TableColumn<MatriculaDTO, String> documentoColumn;

    @FXML
    private TableColumn<MatriculaDTO, String> cursoColumn;

    @FXML
    private TableColumn<MatriculaDTO, LocalDate> fechaColumn;

    @FXML
    private TableColumn<MatriculaDTO, EstadoMatricula> estadoColumn;

    @FXML
    private TableColumn<MatriculaDTO, BigDecimal> valorBaseColumn;

    @FXML
    private TableColumn<MatriculaDTO, BigDecimal> descuentoColumn;

    @FXML
    private TableColumn<MatriculaDTO, BigDecimal> valorFinalColumn;

    @FXML
    private ComboBox<String> filtroEstadoCombo;

    @FXML
    private ComboBox<EstudianteDTO> filtroEstudianteCombo;

    @FXML
    private ComboBox<CursoDTO> filtroCursoCombo;

    @FXML
    private Pagination paginacionMatriculas;

    @FXML
    private Label resumenPaginaLabel;

    private final MatriculaService matriculaService;
    private final EstudianteService estudianteService;
    private final CursoService cursoService;

    private final ObservableList<MatriculaDTO> matriculasList;
    private final List<MatriculaDTO> matriculasFiltradas;

    private Tooltip notificacionTooltip;
    private PauseTransition ocultarNotificacion;
    private boolean actualizandoPaginacion;

    public MatriculaController() {
        this.matriculaService = new MatriculaService();
        this.estudianteService = new EstudianteService();
        this.cursoService = new CursoService();
        this.matriculasList = FXCollections.observableArrayList();
        this.matriculasFiltradas = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        // Configurar columnas
        estudianteColumn.setCellValueFactory(new PropertyValueFactory<>("estudianteNombre"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("estudianteDocumento"));
        cursoColumn.setCellValueFactory(new PropertyValueFactory<>("cursoNombre"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        valorBaseColumn.setCellValueFactory(new PropertyValueFactory<>("valorBase"));
        descuentoColumn.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        valorFinalColumn.setCellValueFactory(new PropertyValueFactory<>("valorFinal"));

        // Configurar filtro de estado
        filtroEstadoCombo.setItems(FXCollections.observableArrayList(
                "Todos",
                EstadoMatricula.PENDIENTE.toString(),
                EstadoMatricula.ACTIVA.toString(),
                EstadoMatricula.CANCELADA.toString(),
                EstadoMatricula.FINALIZADA.toString()
        ));
        filtroEstadoCombo.setValue("Todos");
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarMatriculas(0));

        // Configurar filtro de estudiante
        filtroEstudianteCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarMatriculas(0));

        // Configurar filtro de curso
        filtroCursoCombo.valueProperty().addListener((obs, oldVal, newVal) -> cargarMatriculas(0));

        // Configurar paginación
        paginacionMatriculas.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoPaginacion) {
                renderizarPagina(newVal.intValue());
            }
        });

        // Configurar notificaciones
        notificacionTooltip = new Tooltip();
        notificacionTooltip.setAutoHide(true);

        matriculasTable.setItems(matriculasList);
        cargarMatriculas(0);

        // Cargar combos de filtros
        cargarCombosDeEstudiantes();
        cargarCombosDeCursos();
    }

    /**
     * USER STORY 115: Abrir diálogo para crear nueva matrícula.
     */
    @FXML
    private void onNuevaMatricula() {
        showMatriculaDialog(null).ifPresent(dto -> {
            try {
                MatriculaDTO creada = matriculaService.crearMatricula(
                        dto.getEstudianteId(),
                        dto.getCursoId(),
                        dto.getDescuento(),
                        dto.getObservaciones()
                );
                cargarMatriculas(0);
                showInfo("Matrícula creada correctamente.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onVerDetalles() {
        MatriculaDTO seleccionada = matriculasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            showError("Seleccione una matrícula para ver detalles.");
            return;
        }

        showDetallesDialog(seleccionada);
    }

    @FXML
    private void onRefrescar() {
        cargarMatriculas(0);
    }

    /**
     * USER STORY 116: Cancelar matrícula seleccionada.
     */
    @FXML
    private void onCancelarMatricula() {
        MatriculaDTO seleccionada = matriculasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            showError("Seleccione una matrícula para cancelar.");
            return;
        }

        // Verificar estado actual
        if (seleccionada.getEstado() == EstadoMatricula.CANCELADA) {
            showError("La matrícula ya se encuentra cancelada.");
            return;
        }

        if (seleccionada.getEstado() == EstadoMatricula.FINALIZADA) {
            showError("No se puede cancelar una matrícula finalizada.");
            return;
        }

        // Mostrar confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("Cancelar Matrícula");
        confirmacion.setContentText(
                "¿Está seguro de que desea cancelar la matrícula?\n\n" +
                "Estudiante: " + seleccionada.getEstudianteNombre() + "\n" +
                "Curso: " + seleccionada.getCursoNombre() + "\n" +
                "Estado actual: " + seleccionada.getEstado() + "\n\n" +
                "Se liberará el cupo y no se eliminarán los datos."
        );

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                MatriculaDTO cancelada = matriculaService.cancelarMatricula(seleccionada.getId());
                cargarMatriculas(0);
                showInfo("Matrícula cancelada correctamente. Cupo liberado.");
            } catch (BusinessException | AuthException ex) {
                showError(ex.getMessage());
            }
        }
    }

    /**
     * Diálogo para crear nueva matrícula.
     */
    private Optional<MatriculaDTO> showMatriculaDialog(MatriculaDTO editando) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva Matrícula");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Estudiante
        Label estudianteLabel = new Label("Estudiante*");
        ComboBox<EstudianteDTO> estudianteCombo = new ComboBox<>();
        configurarComboEstudiantes(estudianteCombo);

        // Curso
        Label cursoLabel = new Label("Curso*");
        ComboBox<CursoDTO> cursoCombo = new ComboBox<>();
        configurarComboCursos(cursoCombo);

        // Descuento
        Label descuentoLabel = new Label("Descuento");
        TextField descuentoField = new TextField("0");

        // Observaciones
        Label observacionesLabel = new Label("Observaciones");
        TextArea observacionesArea = new TextArea();
        observacionesArea.setPrefRowCount(3);
        observacionesArea.setWrapText(true);

        // Campo de error
        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");

        grid.add(estudianteLabel, 0, 0);
        grid.add(estudianteCombo, 1, 0);
        grid.add(cursoLabel, 0, 1);
        grid.add(cursoCombo, 1, 1);
        grid.add(descuentoLabel, 0, 2);
        grid.add(descuentoField, 1, 2);
        grid.add(observacionesLabel, 0, 3);
        grid.add(observacionesArea, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Button guardarButton = (Button) dialog.getDialogPane().lookupButton(guardarBtn);

        Runnable validar = () -> {
            String error = null;
            if (estudianteCombo.getValue() == null) {
                error = "Debe seleccionar un estudiante";
            } else if (cursoCombo.getValue() == null) {
                error = "Debe seleccionar un curso";
            }
            guardarButton.setDisable(error != null);
            errorLabel.setText(error != null ? error : "");
        };

        estudianteCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
        cursoCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get() != guardarBtn) {
            return Optional.empty();
        }

        MatriculaDTO dto = new MatriculaDTO();
        dto.setEstudianteId(estudianteCombo.getValue().getId());
        dto.setCursoId(cursoCombo.getValue().getId());

        try {
            BigDecimal descuento = new BigDecimal(descuentoField.getText().trim());
            dto.setDescuento(descuento);
        } catch (NumberFormatException ex) {
            showError("Formato inválido en descuento");
            return Optional.empty();
        }

        dto.setObservaciones(observacionesArea.getText());

        return Optional.of(dto);
    }

    /**
     * Diálogo para ver detalles de matrícula.
     */
    private void showDetallesDialog(MatriculaDTO matricula) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Detalles de Matrícula");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Estudiante:"), 0, 0);
        grid.add(new Label(matricula.getEstudianteNombre()), 1, 0);

        grid.add(new Label("Documento:"), 0, 1);
        grid.add(new Label(matricula.getEstudianteDocumento()), 1, 1);

        grid.add(new Label("Curso:"), 0, 2);
        grid.add(new Label(matricula.getCursoNombre()), 1, 2);

        grid.add(new Label("Fecha:"), 0, 3);
        grid.add(new Label(matricula.getFecha().toString()), 1, 3);

        grid.add(new Label("Estado:"), 0, 4);
        grid.add(new Label(matricula.getEstado().toString()), 1, 4);

        grid.add(new Label("Valor Base:"), 0, 5);
        grid.add(new Label(matricula.getValorBase().toPlainString()), 1, 5);

        grid.add(new Label("Descuento:"), 0, 6);
        grid.add(new Label(matricula.getDescuento().toPlainString()), 1, 6);

        grid.add(new Label("Valor Final:"), 0, 7);
        grid.add(new Label(matricula.getValorFinal().toPlainString()), 1, 7);

        if (matricula.getObservaciones() != null && !matricula.getObservaciones().isEmpty()) {
            grid.add(new Label("Observaciones:"), 0, 8);
            TextArea obsArea = new TextArea(matricula.getObservaciones());
            obsArea.setEditable(false);
            obsArea.setPrefRowCount(3);
            grid.add(obsArea, 1, 8);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    /**
     * Configura el ComboBox de estudiantes.
     */
    private void configurarComboEstudiantes(ComboBox<EstudianteDTO> combo) {
        try {
            List<EstudianteDTO> estudiantes = estudianteService.listarEstudiantesActivos();
            combo.setItems(FXCollections.observableArrayList(estudiantes));
            combo.setConverter(new StringConverter<EstudianteDTO>() {
                @Override
                public String toString(EstudianteDTO estudiante) {
                    if (estudiante == null) return "";
                    return estudiante.getNombre() + " " + estudiante.getApellido() +
                           " (" + estudiante.getNumeroDocumento() + ")";
                }

                @Override
                public EstudianteDTO fromString(String string) {
                    return null;
                }
            });
        } catch (Exception ex) {
            showError("Error al cargar estudiantes: " + ex.getMessage());
        }
    }

    /**
     * Configura el ComboBox de cursos.
     */
    private void configurarComboCursos(ComboBox<CursoDTO> combo) {
        try {
            List<CursoDTO> cursos = cursoService.listarCursos(null, null, null, null);
            combo.setItems(FXCollections.observableArrayList(cursos));
            combo.setConverter(new StringConverter<CursoDTO>() {
                @Override
                public String toString(CursoDTO curso) {
                    if (curso == null) return "";
                    return curso.getNombre() + " (" + curso.getEstado() +
                           ") - Cupos: " + curso.getCuposDisponibles() + "/" + curso.getCupoMaximo();
                }

                @Override
                public CursoDTO fromString(String string) {
                    return null;
                }
            });
        } catch (Exception ex) {
            showError("Error al cargar cursos: " + ex.getMessage());
        }
    }

    /**
     * USER STORY 117: Cargar combo de estudiantes para filtrar.
     */
    private void cargarCombosDeEstudiantes() {
        try {
            List<EstudianteDTO> estudiantes = estudianteService.listarEstudiantesActivos();
            List<EstudianteDTO> conOpcionVacia = new ArrayList<>();
            conOpcionVacia.add(null); // Opción para "todos"
            conOpcionVacia.addAll(estudiantes);

            filtroEstudianteCombo.setItems(FXCollections.observableArrayList(conOpcionVacia));
            filtroEstudianteCombo.setConverter(new StringConverter<EstudianteDTO>() {
                @Override
                public String toString(EstudianteDTO estudiante) {
                    if (estudiante == null) return "Todos";
                    return estudiante.getNombre() + " " + estudiante.getApellido();
                }

                @Override
                public EstudianteDTO fromString(String string) {
                    return null;
                }
            });
            filtroEstudianteCombo.setValue(null);
        } catch (Exception ex) {
            showError("Error al cargar estudiantes para filtro: " + ex.getMessage());
        }
    }

    /**
     * USER STORY 117: Cargar combo de cursos para filtrar.
     */
    private void cargarCombosDeCursos() {
        try {
            List<CursoDTO> cursos = cursoService.listarCursos(null, null, null, null);
            List<CursoDTO> conOpcionVacia = new ArrayList<>();
            conOpcionVacia.add(null); // Opción para "todos"
            conOpcionVacia.addAll(cursos);

            filtroCursoCombo.setItems(FXCollections.observableArrayList(conOpcionVacia));
            filtroCursoCombo.setConverter(new StringConverter<CursoDTO>() {
                @Override
                public String toString(CursoDTO curso) {
                    if (curso == null) return "Todos";
                    return curso.getNombre() + " (" + curso.getEstado() + ")";
                }

                @Override
                public CursoDTO fromString(String string) {
                    return null;
                }
            });
            filtroCursoCombo.setValue(null);
        } catch (Exception ex) {
            showError("Error al cargar cursos para filtro: " + ex.getMessage());
        }
    }

    /**
     * USER STORY 117: Limpiar todos los filtros.
     */
    @FXML
    private void onLimpiarFiltros() {
        filtroEstadoCombo.setValue("Todos");
        filtroEstudianteCombo.setValue(null);
        filtroCursoCombo.setValue(null);
        cargarMatriculas(0);
        showInfo("Filtros limpiados.");
    }

    /**
     * Cargar matrículas desde el servicio.
     */
    private void cargarMatriculas(int paginaPreferida) {
        matriculasList.clear();
        matriculasFiltradas.clear();
        try {
            EstadoMatricula estado = null;
            String filtroEstado = filtroEstadoCombo.getValue();
            if (filtroEstado != null && !"Todos".equals(filtroEstado)) {
                estado = EstadoMatricula.valueOf(filtroEstado);
            }

            EstudianteDTO estudianteFiltro = filtroEstudianteCombo.getValue();
            CursoDTO cursoFiltro = filtroCursoCombo.getValue();

            List<MatriculaDTO> matriculas = matriculaService.listarMatriculas(
                    estudianteFiltro != null ? estudianteFiltro.getId() : null,
                    cursoFiltro != null ? cursoFiltro.getId() : null,
                    estado,
                    null,
                    null
            );
            matriculasFiltradas.addAll(matriculas);

            actualizarPaginacion(paginaPreferida);
        } catch (BusinessException | AuthException ex) {
            showError(ex.getMessage());
            actualizarPaginacion(0);
        }
    }

    /**
     * Actualizar paginación.
     */
    private void actualizarPaginacion(int paginaPreferida) {
        int total = matriculasFiltradas.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int paginaObjetivo = Math.max(0, Math.min(paginaPreferida, totalPaginas - 1));

        actualizandoPaginacion = true;
        paginacionMatriculas.setPageCount(totalPaginas);
        paginacionMatriculas.setCurrentPageIndex(paginaObjetivo);
        actualizandoPaginacion = false;

        renderizarPagina(paginaObjetivo);
    }

    /**
     * Renderizar página.
     */
    private void renderizarPagina(int pageIndex) {
        matriculasList.clear();

        int total = matriculasFiltradas.size();
        if (total == 0) {
            resumenPaginaLabel.setText("Mostrando 0 de 0");
            return;
        }

        int fromIndex = pageIndex * PAGE_SIZE;
        if (fromIndex >= total) {
            fromIndex = Math.max(0, (total - 1) / PAGE_SIZE * PAGE_SIZE);
            pageIndex = fromIndex / PAGE_SIZE;
            actualizandoPaginacion = true;
            paginacionMatriculas.setCurrentPageIndex(pageIndex);
            actualizandoPaginacion = false;
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        matriculasList.addAll(matriculasFiltradas.subList(fromIndex, toIndex));
        resumenPaginaLabel.setText("Mostrando " + (fromIndex + 1) + "-" + toIndex + " de " + total);
    }

    /**
     * Mostrar notificación de error.
     */
    private void showError(String message) {
        mostrarNotificacion(message, true);
    }

    /**
     * Mostrar notificación de éxito.
     */
    private void showInfo(String message) {
        mostrarNotificacion(message, false);
    }

    /**
     * Mostrar notificación general.
     */
    private void mostrarNotificacion(String message, boolean esError) {
        if (matriculasTable == null || matriculasTable.getScene() == null) {
            return;
        }

        Window window = matriculasTable.getScene().getWindow();
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
