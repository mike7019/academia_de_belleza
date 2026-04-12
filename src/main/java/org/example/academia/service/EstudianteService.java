package org.example.academia.service;

import org.example.academia.repository.EstudianteRepository;
import org.example.academia.repository.EstudianteRepositoryImpl;

import java.time.LocalDate;

/**
 * Servicio de Estudiantes.
 */
public class EstudianteService {

    private final EstudianteRepository estudianteRepository = new EstudianteRepositoryImpl();

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
