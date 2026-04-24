package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.domain.enums.EstadoMatricula;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA de {@link CursoRepository}.
 */
public class CursoRepositoryImpl implements CursoRepository {

    @Override
    public List<Curso> findCursosAbiertos() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Curso> query = em.createQuery(
                    "SELECT c FROM Curso c LEFT JOIN FETCH c.maestro WHERE c.estado = :estado",
                    Curso.class
            );
            query.setParameter("estado", EstadoCurso.ABIERTO);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long getTotalCuposDisponibles() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> cuposQuery = em.createQuery(
                    "SELECT COALESCE(SUM(c.cupoMaximo), 0) FROM Curso c WHERE c.estado = :cursoEstado",
                    Long.class
            );
            cuposQuery.setParameter("cursoEstado", EstadoCurso.ABIERTO);

            TypedQuery<Long> ocupadosQuery = em.createQuery(
                    "SELECT COUNT(m) FROM Matricula m WHERE m.estado = :matEstado AND m.curso.estado = :cursoEstado",
                    Long.class
            );
            ocupadosQuery.setParameter("matEstado", EstadoMatricula.ACTIVA);
            ocupadosQuery.setParameter("cursoEstado", EstadoCurso.ABIERTO);

            long cupos = Optional.ofNullable(cuposQuery.getSingleResult()).orElse(0L);
            long ocupados = Optional.ofNullable(ocupadosQuery.getSingleResult()).orElse(0L);
            return Math.max(0L, cupos - ocupados);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Curso> search(String nombre, EstadoCurso estado, LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM Curso c LEFT JOIN FETCH c.maestro WHERE 1=1");
            boolean filtrarNombre = nombre != null && !nombre.isBlank();
            boolean filtrarEstado = estado != null;
            boolean filtrarFechaInicio = fechaInicio != null;
            boolean filtrarFechaFin = fechaFin != null;

            if (filtrarNombre) {
                jpql.append(" AND LOWER(c.nombre) LIKE :nombre");
            }
            if (filtrarEstado) {
                jpql.append(" AND c.estado = :estado");
            }
            if (filtrarFechaInicio) {
                jpql.append(" AND c.fechaInicio >= :fechaInicio");
            }
            if (filtrarFechaFin) {
                jpql.append(" AND c.fechaFin <= :fechaFin");
            }
            jpql.append(" ORDER BY c.fechaInicio DESC, c.nombre ASC");

            TypedQuery<Curso> query = em.createQuery(jpql.toString(), Curso.class);
            if (filtrarNombre) {
                query.setParameter("nombre", "%" + nombre.trim().toLowerCase() + "%");
            }
            if (filtrarEstado) {
                query.setParameter("estado", estado);
            }
            if (filtrarFechaInicio) {
                query.setParameter("fechaInicio", fechaInicio);
            }
            if (filtrarFechaFin) {
                query.setParameter("fechaFin", fechaFin);
            }

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
                    "SELECT COUNT(m) FROM Matricula m WHERE m.curso.id = :cursoId AND m.estado = :estado",
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
    public Optional<Curso> findById(Long id) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Curso> query = em.createQuery(
                    "SELECT c FROM Curso c LEFT JOIN FETCH c.maestro WHERE c.id = :id",
                    Curso.class
            );
            query.setParameter("id", id);
            List<Curso> result = query.getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public Curso save(Curso curso) {
        EntityManager em = DatabaseConfig.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (curso.getId() == null) {
                em.persist(curso);
            } else {
                curso = em.merge(curso);
            }
            tx.commit();
            return curso;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}

