package org.example.academia.service;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.dto.StudentReportDTO;
import org.example.academia.repository.CursoRepository;
import org.example.academia.repository.EstudianteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportesServiceImpl implements ReportesService {
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;

    public ReportesServiceImpl(EstudianteRepository estudianteRepository, CursoRepository cursoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    public List<StudentReportDTO> obtenerEstudiantesParaReporte(String nombre, Long cursoId, Boolean estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        List<StudentReportDTO> resultado = new ArrayList<>();
        for (Estudiante est : estudiantes) {
            // Filtro por nombre
            if (nombre != null && !nombre.isBlank()) {
                String nombreCompleto = (est.getNombre() + " " + est.getApellido()).toLowerCase();
                if (!nombreCompleto.contains(nombre.toLowerCase())) continue;
            }
            // Filtro por estado
            if (estado != null && est.isActivo() != estado) continue;
            // Filtro por fecha de registro
            if (fechaDesde != null && est.getFechaRegistro().isBefore(fechaDesde)) continue;
            if (fechaHasta != null && est.getFechaRegistro().isAfter(fechaHasta)) continue;
            // Filtro por curso
            String cursoMatriculado = "";
            if (cursoId != null) {
                boolean tieneCurso = false;
                if (est.getMatriculas() != null) {
                    for (var mat : est.getMatriculas()) {
                        if (mat.getCurso() != null && mat.getCurso().getId().equals(cursoId)) {
                            tieneCurso = true;
                            cursoMatriculado = mat.getCurso().getNombre();
                            break;
                        }
                    }
                }
                if (!tieneCurso) continue;
            } else {
                // Si no se filtra por curso, mostrar el primero (si existe)
                if (est.getMatriculas() != null && !est.getMatriculas().isEmpty() && est.getMatriculas().get(0).getCurso() != null) {
                    cursoMatriculado = est.getMatriculas().get(0).getCurso().getNombre();
                }
            }
            String estadoStr = est.isActivo() ? "Activo" : "Inactivo";
            resultado.add(new StudentReportDTO(
                    est.getId(),
                    est.getNombre() + " " + est.getApellido(),
                    est.getNumeroDocumento(),
                    est.getTelefono(),
                    est.getEmail(),
                    cursoMatriculado,
                    estadoStr,
                    est.getFechaRegistro()
            ));
        }
        return resultado;
    }

    @Override
    public byte[] exportarEstudiantesPDF(List<StudentReportDTO> estudiantes) throws Exception {
        // Exportación no implementada, retorna null para evitar excepción
        return null;
    }

    @Override
    public byte[] exportarEstudiantesExcel(List<StudentReportDTO> estudiantes) throws Exception {
        // Exportación no implementada, retorna null para evitar excepción
        return null;
    }
}


