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

	/**
	 * Búsqueda paginada y filtrada de estudiantes.
	 * Parámetros con valor null o vacío serán ignorados en los criterios.
	 * @param nombre filtro por nombre (fragmento, case-insensitive)
	 * @param apellido filtro por apellido (fragmento, case-insensitive)
	 * @param numeroDocumento filtro por número de documento (fragmento, case-insensitive)
	 * @param activo si no es null, filtra por el flag activo
	 * @param offset primer resultado (0-based)
	 * @param limit máximo de resultados a devolver
	 * @param sortBy campo por el que ordenar (debe ser validado por la implementación)
	 * @param asc true = ascendente, false = descendente
	 * @return lista paginada de estudiantes que cumplen los criterios
	 */
	List<Estudiante> search(String nombre, String apellido, String numeroDocumento, Boolean activo,
							int offset, int limit, String sortBy, boolean asc);

	/**
	 * Cuenta el total de estudiantes que cumplen los criterios (sin paginación).
	 */
	long countByCriteria(String nombre, String apellido, String numeroDocumento, Boolean activo);
}

