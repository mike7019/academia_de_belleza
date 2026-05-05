package org.example.academia.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import org.example.academia.dto.StudentReportDTO;
import org.example.academia.service.ReportesService;
import org.example.academia.service.ReportesServiceImpl;
import org.example.academia.repository.EstudianteRepository;
import org.example.academia.repository.CursoRepository;
import org.example.academia.domain.entity.Curso;
import java.time.LocalDate;
import java.util.List;

public class ReportesController {
    @FXML private TextField nombreField;
    @FXML private ComboBox<Curso> cursoCombo;
    @FXML private ComboBox<String> estadoCombo;
    @FXML private DatePicker fechaDesdePicker;
    @FXML private DatePicker fechaHastaPicker;
    @FXML private Button btnFiltrar;
    @FXML private TableView<StudentReportDTO> tablaEstudiantes;
    @FXML private TableColumn<StudentReportDTO, Long> colId;
    @FXML private TableColumn<StudentReportDTO, String> colNombreCompleto;
    @FXML private TableColumn<StudentReportDTO, String> colDocumento;
    @FXML private TableColumn<StudentReportDTO, String> colTelefono;
    @FXML private TableColumn<StudentReportDTO, String> colEmail;
    @FXML private TableColumn<StudentReportDTO, String> colCurso;
    @FXML private TableColumn<StudentReportDTO, String> colEstado;
    @FXML private TableColumn<StudentReportDTO, LocalDate> colFechaRegistro;
    @FXML private Label lblSinResultados;
    @FXML private Label lblTotal;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnExportarExcel;

    private final ReportesService reportesService;
    private final ObservableList<StudentReportDTO> estudiantesList = FXCollections.observableArrayList();
    private final CursoRepository cursoRepository;

    public ReportesController() {
        // Instanciación manual siguiendo patrón del proyecto
        this.cursoRepository = new org.example.academia.repository.CursoRepositoryImpl();
        EstudianteRepository estudianteRepository = new org.example.academia.repository.EstudianteRepositoryImpl();
        this.reportesService = new ReportesServiceImpl(estudianteRepository, cursoRepository);
    }

    @FXML
    private void initialize() {
        try {
            // Validar repositorios
            if (cursoRepository == null || reportesService == null) {
                lblSinResultados.setText("Error: Servicios no inicializados");
                lblSinResultados.setVisible(true);
                return;
            }

            // Configurar columnas de la tabla
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colNombreCompleto.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
            colDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
            colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colCurso.setCellValueFactory(new PropertyValueFactory<>("cursoMatriculado"));
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            colFechaRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
            tablaEstudiantes.setItems(estudiantesList);

            // Cargar combos
            cargarCursos();
            estadoCombo.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
            estadoCombo.getSelectionModel().selectFirst();

            // Acción de filtrar
            btnFiltrar.setOnAction(e -> filtrarEstudiantes());
            btnExportarPDF.setOnAction(e -> exportarPDF());
            btnExportarExcel.setOnAction(e -> exportarExcel());
            
            // Carga inicial de estudiantes
            filtrarEstudiantes();
            
        } catch (Exception e) {
            lblSinResultados.setText("Error al inicializar la vista de reportes");
            lblSinResultados.setVisible(true);
            System.err.println("Error en initialize de ReportesController: " + e.getMessage());
        }
    }

    private void cargarCursos() {
        try {
            List<Curso> cursos = cursoRepository.findCursosAbiertos();
            cursoCombo.getItems().clear();
            cursoCombo.getItems().add(null); // Opción "Todos"
            
            if (cursos == null || cursos.isEmpty()) {
                cursoCombo.setPromptText("No hay cursos registrados");
            } else {
                cursoCombo.getItems().addAll(cursos);
                cursoCombo.setPromptText("Todos los cursos");
            }
        } catch (Exception e) {
            cursoCombo.getItems().clear();
            cursoCombo.getItems().add(null);
            cursoCombo.setPromptText("Error al cargar cursos");
            System.err.println("Error en cargarCursos: " + e.getMessage());
        }
    }

    private void filtrarEstudiantes() {
        try {
            if (reportesService == null) return;

            String nombre = nombreField.getText();
            Curso curso = cursoCombo.getValue();
            Long cursoId = (curso != null) ? curso.getId() : null;
            String estadoSel = estadoCombo.getValue();
            Boolean estado = null;
            if ("Activo".equals(estadoSel)) estado = true;
            else if ("Inactivo".equals(estadoSel)) estado = false;
            LocalDate desde = fechaDesdePicker.getValue();
            LocalDate hasta = fechaHastaPicker.getValue();
            
            List<StudentReportDTO> lista = reportesService.obtenerEstudiantesParaReporte(nombre, cursoId, estado, desde, hasta);
            estudiantesList.setAll(lista);
            
            if (lista.isEmpty()) {
                lblSinResultados.setText("No hay estudiantes registrados");
                lblSinResultados.setVisible(true);
            } else {
                lblSinResultados.setVisible(false);
            }
            
            lblTotal.setText("Total estudiantes: " + lista.size());
        } catch (Exception ex) {
            lblSinResultados.setText("Error al consultar estudiantes");
            lblSinResultados.setVisible(true);
            estudiantesList.clear();
            lblTotal.setText("Total estudiantes: 0");
            System.err.println("Error en filtrarEstudiantes: " + ex.getMessage());
        }
    }

    private void exportarPDF() {
        if (estudiantesList.isEmpty()) return;
        mostrarAlertaInfo("Funcionalidad de exportación disponible próximamente");
    }

    private void exportarExcel() {
        if (estudiantesList.isEmpty()) return;
        mostrarAlertaInfo("Funcionalidad de exportación disponible próximamente");
    }

    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
