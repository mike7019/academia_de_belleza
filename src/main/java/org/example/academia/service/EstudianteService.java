package org.example.academia.service;

import org.example.academia.repository.EstudianteRepository;

import java.time.LocalDate;

/**
 * Servicio de Estudiantes (mínimo funcional).
 *
 * Este archivo había quedado dañado; se restaura la estructura básica
 * conservando las llamadas previstas hacia el repositorio de estudiantes.
 */
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public long getTotalEstudiantesActivos() {
        return estudianteRepository.countByActivoTrue();
    }

    public long getNuevosEstudiantesRegistradosMesActual() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return estudianteRepository.countByFechaRegistroBetween(startOfMonth, endOfMonth);
    }
}
