package org.example.academia.repository;

import org.example.academia.domain.entity.Estudiante;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Estudiante (definición de operaciones básicas).
 */
public interface EstudianteRepository {

	Optional<Estudiante> findById(Long id);

	Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);

	Estudiante save(Estudiante estudiante);

	List<Estudiante> findAll();
}

