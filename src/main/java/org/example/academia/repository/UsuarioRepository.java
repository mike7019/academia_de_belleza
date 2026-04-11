package org.example.academia.repository;

import org.example.academia.domain.entity.Usuario;

import java.util.Optional;

/**
 * Repositorio de Usuario.
 *
 * Define operaciones básicas de acceso a datos para la entidad Usuario.
 */
public interface UsuarioRepository {

	Optional<Usuario> findByUsername(String username);

	Usuario save(Usuario usuario);
}

