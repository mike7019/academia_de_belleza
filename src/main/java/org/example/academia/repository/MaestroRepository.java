package org.example.academia.repository;

import org.example.academia.domain.entity.Maestro;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Maestro.
 *
 * Define operaciones de acceso a datos para la entidad Maestro, incluyendo búsqueda por documento,
 * listado de maestros activos y persistencia.
 */
public interface MaestroRepository {

    /**
     * Busca un maestro activo por su número de documento.
     *
     * @param numeroDocumento el número de documento único del maestro.
     * @return un Optional con el maestro si existe y está activo, vacío en caso contrario.
     */
    Optional<Maestro> findByNumeroDocumento(String numeroDocumento);

    /**
     * Lista todos los maestros activos (eliminación suave).
     *
     * @return una lista de maestros con activo = true.
     */
    List<Maestro> findByActivoTrue();

    /**
     * Guarda o actualiza un maestro en la base de datos.
     *
     * @param maestro el maestro a guardar o actualizar.
     * @return el maestro guardado con ID asignado si es nuevo.
     */
    Maestro save(Maestro maestro);

    /**
     * Busca un maestro por su ID (activo o inactivo).
     *
     * @param id ID del maestro
     * @return Optional con el maestro si existe
     */
    Optional<Maestro> findById(Long id);
}
