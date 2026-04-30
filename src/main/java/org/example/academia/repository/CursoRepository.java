package org.example.academia.repository;

import org.example.academia.domain.entity.Curso;

import java.util.Optional;
import java.util.List;

public interface CursoRepository {
	/**
	 * Devuelve la lista de cursos cuyo estado es ABIERTO.
	 */
	List<Curso> findCursosAbiertos();

	/**
	 * Calcula el número total de cupos disponibles en cursos ABIERTO.
	 */
	long getTotalCuposDisponibles();

	/**
	 * Busca un curso por ID.
	 */
	Optional<Curso> findById(Long id);

	/**
	 * Guarda o actualiza un curso.
	 */
	Curso save(Curso curso);

}



