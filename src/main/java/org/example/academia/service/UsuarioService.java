package org.example.academia.service;

import org.example.academia.domain.entity.Usuario;
import org.example.academia.repository.UsuarioRepository;
import org.example.academia.repository.UsuarioRepositoryImpl;
import org.example.academia.security.PasswordEncoder;

import java.util.Optional;

/**
 * Servicio de usuarios: registro y operaciones básicas sobre Usuario.
 */
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.passwordEncoder = new PasswordEncoder();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @throws IllegalArgumentException si el usuario ya existe o los datos son inválidos.
     */
    public Usuario registrarUsuario(String nombreCompleto,
                                    String email,
                                    String username,
                                    String rawPassword) {
        if (username == null || username.isBlank() ||
                rawPassword == null || rawPassword.isBlank() ||
                nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("Nombre completo, usuario y contraseña son obligatorios");
        }

        String usernameTrimmed = username.trim();

        Optional<Usuario> existente = usuarioRepository.findByUsername(usernameTrimmed);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usernameTrimmed);
        usuario.setNombreCompleto(nombreCompleto.trim());
        usuario.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }
}

