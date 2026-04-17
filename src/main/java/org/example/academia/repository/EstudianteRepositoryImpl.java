package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Estudiante;

import java.time.LocalDate;

public class EstudianteRepositoryImpl implements EstudianteRepository {

    @Override
    public long countByActivoTrue() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(e) FROM Estudiante e WHERE e.activo = true", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByFechaRegistroBetween(LocalDate desde, LocalDate hasta) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(e) FROM Estudiante e WHERE e.fechaRegistro BETWEEN :desde AND :hasta", Long.class);
            query.setParameter("desde", desde);
            query.setParameter("hasta", hasta);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public Estudiante save(Estudiante estudiante) {
        EntityManager em = DatabaseConfig.createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            if (estudiante.getId() == null) {
                em.persist(estudiante);
            } else {
                estudiante = em.merge(estudiante);
            }
            tx.commit();
            return estudiante;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}

