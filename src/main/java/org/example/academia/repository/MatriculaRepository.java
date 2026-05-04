package org.example.academia.repository;

import org.example.academia.domain.entity.Matricula;
import org.example.academia.domain.enums.EstadoMatricula;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones de acceso a datos de Matrícula.
 */
public interface MatriculaRepository {

    /**
     * Guarda o actualiza una matrícula.
     */
    Matricula save(Matricula matricula);

    /**
     * Busca una matrícula por ID.
     */
    Optional<Matricula> findById(Long id);

    /**
     * Lista todas las matrículas.
     */
    List<Matricula> findAll();

    /**
     * Busca matrículas por estudiante y curso (debe ser única).
     */
    Optional<Matricula> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);

    /**
     * Lista matrículas de un estudiante específico.
     */
    List<Matricula> findByEstudianteId(Long estudianteId);

    /**
     * Lista matrículas de un curso específico.
     */
    List<Matricula> findByCursoId(Long cursoId);

    /**
     * Cuenta matrículas activas de un curso.
     */
    long countMatriculasActivasByCursoId(Long cursoId);

    /**
     * Busca matrículas con filtros opcionales.
     */
    List<Matricula> search(Long estudianteId, Long cursoId, EstadoMatricula estado, LocalDate fechaDesde, LocalDate fechaHasta);
}

