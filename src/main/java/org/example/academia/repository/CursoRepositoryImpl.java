package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.domain.enums.EstadoMatricula;

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
            TypedQuery<Curso> query = em.createQuery("SELECT c FROM Curso c WHERE c.estado = :estado", Curso.class);
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
    public Optional<Curso> findById(Long id) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            Curso curso = em.find(Curso.class, id);
            return curso != null ? Optional.of(curso) : Optional.empty();
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

