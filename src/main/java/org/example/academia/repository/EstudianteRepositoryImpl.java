package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Estudiante;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA de {@link EstudianteRepository} usando EntityManager.
 */
public class EstudianteRepositoryImpl implements EstudianteRepository {

    @Override
    public Optional<Estudiante> findById(Long id) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            Estudiante e = em.find(Estudiante.class, id);
            return Optional.ofNullable(e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Estudiante> findByNumeroDocumento(String numeroDocumento) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Estudiante> query = em.createQuery(
                    "SELECT e FROM Estudiante e WHERE e.numeroDocumento = :numero", Estudiante.class);
            query.setParameter("numero", numeroDocumento);
            Estudiante e = query.getSingleResult();
            return Optional.of(e);
        } catch (NoResultException ex) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public Estudiante save(Estudiante estudiante) {
        EntityManager em = DatabaseConfig.createEntityManager();
        EntityTransaction tx = em.getTransaction();
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
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Estudiante> findAll() {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Estudiante> query = em.createQuery("SELECT e FROM Estudiante e", Estudiante.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

