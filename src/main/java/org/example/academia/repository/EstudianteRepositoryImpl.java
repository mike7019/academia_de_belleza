package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;

import java.time.LocalDate;

/**
 * Implementación JPA de {@link EstudianteRepository} usando EntityManager directo.
 */
public class EstudianteRepositoryImpl implements EstudianteRepository {

    @Override
    public long countByActivoTrue() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.activo = true",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByFechaRegistroBetween(LocalDate start, LocalDate end) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(e) FROM Estudiante e WHERE e.fechaRegistro BETWEEN :start AND :end",
                    Long.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
