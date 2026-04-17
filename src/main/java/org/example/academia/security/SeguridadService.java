package org.example.academia.security;

import org.example.academia.domain.entity.Usuario;
import org.example.academia.repository.UsuarioRepository;
import org.example.academia.repository.UsuarioRepositoryImpl;

import java.util.Optional;

/**
 * Servicio de seguridad: autenticación de usuarios.
 */
public class SeguridadService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final SessionManager sessionManager;

	public SeguridadService() {
		this.usuarioRepository = new UsuarioRepositoryImpl();
		this.passwordEncoder = new PasswordEncoder();
		this.sessionManager = SessionManager.getInstance();
	}

	/**
	 * Intenta autenticar al usuario con las credenciales proporcionadas.
	 *
	 * @throws AuthException si las credenciales son inválidas o el usuario está inactivo.
	 */
	public void login(String username, String password) {
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			throw new AuthException("Usuario y contraseña son obligatorios");
		}

		Optional<Usuario> optionalUsuario = usuarioRepository.findByUsername(username.trim());

		if (optionalUsuario.isEmpty()) {
			throw new AuthException("Usuario o contraseña incorrectos");
		}

		Usuario usuario = optionalUsuario.get();

		if (!usuario.isActivo()) {
			throw new AuthException("El usuario se encuentra inactivo");
		}

		if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
			throw new AuthException("Usuario o contraseña incorrectos");
		}

		// Autenticación exitosa: registrar en SessionManager
		sessionManager.login(usuario);

		// DEBUG: imprimir roles y permisos cargados para ayudar a depuración en desarrollo
		try {
			if (usuario.getRoles() != null) {
				System.out.println("[DEBUG] Usuario autenticado: " + usuario.getUsername());
				usuario.getRoles().forEach(r -> {
					System.out.println("[DEBUG] Rol: " + r.getNombre() + " (activo=" + r.isActivo() + ")");
					if (r.getPermisos() != null) {
						r.getPermisos().forEach(p -> System.out.println("[DEBUG]   Permiso: " + p.getCodigo()));
					}
				});
			}
		} catch (Exception e) {
			// No interrumpir la autenticación por fallos en logging
			System.err.println("[DEBUG] Error imprimiendo roles/permisos: " + e.getMessage());
		}
	}
}


