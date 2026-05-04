package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Matricula;
import org.example.academia.domain.enums.EstadoMatricula;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA de {@link MatriculaRepository}.
 */
public class MatriculaRepositoryImpl implements MatriculaRepository {

    @Override
    public Matricula save(Matricula matricula) {
        EntityManager em = DatabaseConfig.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (matricula.getId() == null) {
                em.persist(matricula);
            } else {
                matricula = em.merge(matricula);
            }
            tx.commit();
            return matricula;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Matricula> findById(Long id) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Matricula> query = em.createQuery(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "WHERE m.id = :id",
                    Matricula.class
            );
            query.setParameter("id", id);
            List<Matricula> result = query.getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Matricula> findAll() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Matricula> query = em.createQuery(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "ORDER BY m.fecha DESC",
                    Matricula.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Matricula> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Matricula> query = em.createQuery(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "WHERE m.estudiante.id = :estudianteId AND m.curso.id = :cursoId",
                    Matricula.class
            );
            query.setParameter("estudianteId", estudianteId);
            query.setParameter("cursoId", cursoId);
            List<Matricula> result = query.getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Matricula> findByEstudianteId(Long estudianteId) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Matricula> query = em.createQuery(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "WHERE m.estudiante.id = :estudianteId " +
                    "ORDER BY m.fecha DESC",
                    Matricula.class
            );
            query.setParameter("estudianteId", estudianteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Matricula> findByCursoId(Long cursoId) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Matricula> query = em.createQuery(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "WHERE m.curso.id = :cursoId " +
                    "ORDER BY m.fecha DESC",
                    Matricula.class
            );
            query.setParameter("cursoId", cursoId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countMatriculasActivasByCursoId(Long cursoId) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(m) FROM Matricula m " +
                    "WHERE m.curso.id = :cursoId AND m.estado = :estado",
                    Long.class
            );
            query.setParameter("cursoId", cursoId);
            query.setParameter("estado", EstadoMatricula.ACTIVA);
            return Optional.ofNullable(query.getSingleResult()).orElse(0L);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Matricula> search(Long estudianteId, Long cursoId, EstadoMatricula estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT m FROM Matricula m " +
                    "LEFT JOIN FETCH m.estudiante " +
                    "LEFT JOIN FETCH m.curso " +
                    "WHERE 1=1"
            );

            boolean filtrarEstudiante = estudianteId != null;
            boolean filtrarCurso = cursoId != null;
            boolean filtrarEstado = estado != null;
            boolean filtrarFechaDesde = fechaDesde != null;
            boolean filtrarFechaHasta = fechaHasta != null;

            if (filtrarEstudiante) {
                jpql.append(" AND m.estudiante.id = :estudianteId");
            }
            if (filtrarCurso) {
                jpql.append(" AND m.curso.id = :cursoId");
            }
            if (filtrarEstado) {
                jpql.append(" AND m.estado = :estado");
            }
            if (filtrarFechaDesde) {
                jpql.append(" AND m.fecha >= :fechaDesde");
            }
            if (filtrarFechaHasta) {
                jpql.append(" AND m.fecha <= :fechaHasta");
            }

            jpql.append(" ORDER BY m.fecha DESC");

            TypedQuery<Matricula> query = em.createQuery(jpql.toString(), Matricula.class);

            if (filtrarEstudiante) {
                query.setParameter("estudianteId", estudianteId);
            }
            if (filtrarCurso) {
                query.setParameter("cursoId", cursoId);
            }
            if (filtrarEstado) {
                query.setParameter("estado", estado);
            }
            if (filtrarFechaDesde) {
                query.setParameter("fechaDesde", fechaDesde);
            }
            if (filtrarFechaHasta) {
                query.setParameter("fechaHasta", fechaHasta);
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

