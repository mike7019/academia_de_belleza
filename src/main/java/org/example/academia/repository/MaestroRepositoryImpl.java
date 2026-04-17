package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Maestro;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA de {@link MaestroRepository} usando EntityManager directo.
 */
public class MaestroRepositoryImpl implements MaestroRepository {

    @Override
    public Optional<Maestro> findByNumeroDocumento(String numeroDocumento) {
        try (EntityManager em = DatabaseConfig.createEntityManager()) {
            TypedQuery<Maestro> query = em.createQuery(
                    "SELECT m FROM Maestro m WHERE m.activo = true AND m.numeroDocumento = :numeroDocumento",
                    Maestro.class);
            query.setParameter("numeroDocumento", numeroDocumento);
            Maestro maestro = query.getSingleResult();
            return Optional.of(maestro);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Maestro> findByActivoTrue() {
        try (EntityManager em = DatabaseConfig.createEntityManager()) {
            TypedQuery<Maestro> query = em.createQuery(
                    "SELECT m FROM Maestro m WHERE m.activo = true",
                    Maestro.class);
            return query.getResultList();
        }
    }

    @Override
    public Maestro save(Maestro maestro) {
        try (EntityManager em = DatabaseConfig.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (maestro.getId() == null) {
                    em.persist(maestro);
                } else {
                    maestro = em.merge(maestro);
                }
                tx.commit();
                return maestro;
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw ex;
            }
        }
    }

    @Override
    public Optional<Maestro> findById(Long id) {
        try (EntityManager em = DatabaseConfig.createEntityManager()) {
            Maestro maestro = em.find(Maestro.class, id);
            return maestro != null ? Optional.of(maestro) : Optional.empty();
        }
    }
}
