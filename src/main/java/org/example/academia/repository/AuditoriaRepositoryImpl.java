package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Auditoria;

/**
 * Implementación JPA de AuditoriaRepository.
 */
public class AuditoriaRepositoryImpl implements AuditoriaRepository {

    @Override
    public Auditoria save(Auditoria auditoria) {
        EntityManager em = DatabaseConfig.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (auditoria.getId() == null) {
                em.persist(auditoria);
            } else {
                auditoria = em.merge(auditoria);
            }
            tx.commit();
            return auditoria;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}

