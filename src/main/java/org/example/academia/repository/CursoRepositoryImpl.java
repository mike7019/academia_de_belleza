package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.domain.enums.EstadoMatricula;

import java.util.List;

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
            TypedQuery<Long> query = em.createQuery(
                    "SELECT SUM(c.cupoMaximo - (SELECT COUNT(m) FROM Matricula m WHERE m.curso = c AND m.estado = :matEstado)) " +
                            "FROM Curso c WHERE c.estado = :cursoEstado", Long.class);
            query.setParameter("matEstado", EstadoMatricula.ACTIVA);
            query.setParameter("cursoEstado", EstadoCurso.ABIERTO);
            Long result = query.getSingleResult();
            return result == null ? 0L : result;
        } finally {
            em.close();
        }
    }
}

