package org.example.academia.repository;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.enums.EstadoCurso;

import java.time.LocalDate;
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
	 * Lista cursos aplicando filtros opcionales.
	 */
	List<Curso> search(String nombre, EstadoCurso estado, LocalDate fechaInicio, LocalDate fechaFin);

	/**
	 * Cuenta matrículas activas asociadas al curso.
	 */
	long countMatriculasActivasByCursoId(Long cursoId);

	/**
	 * Busca un curso por ID.
	 */
	Optional<Curso> findById(Long id);

	/**
	 * Guarda o actualiza un curso.
	 */
	Curso save(Curso curso);

}
