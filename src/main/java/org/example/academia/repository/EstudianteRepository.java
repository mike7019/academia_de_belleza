package org.example.academia.repository;

import org.example.academia.domain.entity.Estudiante;

import java.time.LocalDate;

public interface EstudianteRepository {

    long countByActivoTrue();

    long countByFechaRegistroBetween(LocalDate desde, LocalDate hasta);

    Estudiante save(Estudiante estudiante);
}
