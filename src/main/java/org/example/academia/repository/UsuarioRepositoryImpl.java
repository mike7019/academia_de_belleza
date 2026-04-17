package org.example.academia.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.academia.config.DatabaseConfig;
import org.example.academia.domain.entity.Usuario;

import java.util.Optional;

/**
 * Implementación JPA de {@link UsuarioRepository} usando EntityManager directo.
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @Override
    public Optional<Usuario> findByUsername(String username) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT DISTINCT u FROM Usuario u " +
                            "LEFT JOIN FETCH u.roles r " +
                            "LEFT JOIN FETCH r.permisos " +
                            "WHERE u.username = :username",
                    Usuario.class);
            query.setParameter("username", username);
            Usuario usuario = query.getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException ex) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        EntityManager em = DatabaseConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)",
                    Usuario.class);
            query.setParameter("email", email);
            Usuario usuario = query.getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException ex) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

          @Override
          public Usuario save(Usuario usuario) {
            EntityManager em = DatabaseConfig.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
              tx.begin();
              if (usuario.getId() == null) {
                em.persist(usuario);
              } else {
                usuario = em.merge(usuario);
              }
              tx.commit();
              return usuario;
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

