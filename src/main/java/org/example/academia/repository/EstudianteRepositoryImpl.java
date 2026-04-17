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

    @Override
    public List<Estudiante> search(String nombre, String apellido, String numeroDocumento, Boolean activo, int offset, int limit, String sortBy, boolean asc) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            StringBuilder sb = new StringBuilder("SELECT e FROM Estudiante e WHERE 1=1");
            if (nombre != null && !nombre.isBlank()) {
                sb.append(" AND LOWER(e.nombre) LIKE :nombre");
            }
            if (apellido != null && !apellido.isBlank()) {
                sb.append(" AND LOWER(e.apellido) LIKE :apellido");
            }
            if (numeroDocumento != null && !numeroDocumento.isBlank()) {
                sb.append(" AND LOWER(e.numeroDocumento) LIKE :numeroDocumento");
            }
            if (activo != null) {
                sb.append(" AND e.activo = :activo");
            }

            // Lista blanca de campos permitidos para orden
            String[] allowed = new String[]{"id", "nombre", "apellido", "numeroDocumento", "email"};
            boolean ok = false;
            if (sortBy != null) {
                for (String a : allowed) {
                    if (a.equals(sortBy)) {
                        ok = true; break;
                    }
                }
            }
            if (ok) {
                sb.append(" ORDER BY e.").append(sortBy).append(asc ? " ASC" : " DESC");
            }

            TypedQuery<Estudiante> query = em.createQuery(sb.toString(), Estudiante.class);
            if (nombre != null && !nombre.isBlank()) {
                query.setParameter("nombre", "%" + nombre.trim().toLowerCase() + "%");
            }
            if (apellido != null && !apellido.isBlank()) {
                query.setParameter("apellido", "%" + apellido.trim().toLowerCase() + "%");
            }
            if (numeroDocumento != null && !numeroDocumento.isBlank()) {
                query.setParameter("numeroDocumento", "%" + numeroDocumento.trim().toLowerCase() + "%");
            }
            if (activo != null) {
                query.setParameter("activo", activo);
            }

            if (offset >= 0) query.setFirstResult(offset);
            if (limit > 0) query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByCriteria(String nombre, String apellido, String numeroDocumento, Boolean activo) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            StringBuilder sb = new StringBuilder("SELECT COUNT(e) FROM Estudiante e WHERE 1=1");
            if (nombre != null && !nombre.isBlank()) {
                sb.append(" AND LOWER(e.nombre) LIKE :nombre");
            }
            if (apellido != null && !apellido.isBlank()) {
                sb.append(" AND LOWER(e.apellido) LIKE :apellido");
            }
            if (numeroDocumento != null && !numeroDocumento.isBlank()) {
                sb.append(" AND LOWER(e.numeroDocumento) LIKE :numeroDocumento");
            }
            if (activo != null) {
                sb.append(" AND e.activo = :activo");
            }

            TypedQuery<Long> query = em.createQuery(sb.toString(), Long.class);
            if (nombre != null && !nombre.isBlank()) {
                query.setParameter("nombre", "%" + nombre.trim().toLowerCase() + "%");
            }
            if (apellido != null && !apellido.isBlank()) {
                query.setParameter("apellido", "%" + apellido.trim().toLowerCase() + "%");
            }
            if (numeroDocumento != null && !numeroDocumento.isBlank()) {
                query.setParameter("numeroDocumento", "%" + numeroDocumento.trim().toLowerCase() + "%");
            }
            if (activo != null) {
                query.setParameter("activo", activo);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}

