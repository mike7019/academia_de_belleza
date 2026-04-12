package org.example.academia.repository;

import org.example.academia.domain.entity.Estudiante;

import java.time.LocalDate;

public interface EstudianteRepository {

    long countByActivoTrue();

    long countByFechaRegistroBetween(LocalDate start, LocalDate end);

}
