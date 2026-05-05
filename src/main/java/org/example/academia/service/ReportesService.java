package org.example.academia.service;

import org.example.academia.dto.StudentReportDTO;
import java.time.LocalDate;
import java.util.List;

public interface ReportesService {
    /**
     * Obtiene el listado de estudiantes según filtros.
     * @param nombre nombre o fragmento (puede ser null)
     * @param cursoId id del curso (puede ser null)
     * @param estado true=activo, false=inactivo, null=ambos
     * @param fechaDesde fecha de registro desde (puede ser null)
     * @param fechaHasta fecha de registro hasta (puede ser null)
     * @return lista de estudiantes para el reporte
     */
    List<StudentReportDTO> obtenerEstudiantesParaReporte(String nombre, Long cursoId, Boolean estado, LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Exporta el reporte de estudiantes filtrados a PDF.
     * @param estudiantes lista de estudiantes filtrados
     * @return bytes del archivo PDF
     */
    byte[] exportarEstudiantesPDF(List<StudentReportDTO> estudiantes) throws Exception;

    /**
     * Exporta el reporte de estudiantes filtrados a Excel.
     * @param estudiantes lista de estudiantes filtrados
     * @return bytes del archivo Excel
     */
    byte[] exportarEstudiantesExcel(List<StudentReportDTO> estudiantes) throws Exception;
}

