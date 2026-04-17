package org.example.academia.security;

import org.example.academia.domain.entity.Usuario;
import org.example.academia.repository.UsuarioRepository;
import org.example.academia.repository.UsuarioRepositoryImpl;
import org.example.academia.service.EmailService;

import java.security.SecureRandom;
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

	/**
	 * Cambia la contraseña de un usuario existente.
	 *
	 * @param username nombre de usuario
	 * @param nuevaContrasena nueva contraseña en texto plano
	 * @throws AuthException si el usuario no existe o está inactivo
	 */
	public void cambiarContrasena(String username, String nuevaContrasena) {
		if (username == null || username.isBlank()) {
			throw new AuthException("El nombre de usuario es obligatorio");
		}
		if (nuevaContrasena == null || nuevaContrasena.isBlank()) {
			throw new AuthException("La nueva contraseña es obligatoria");
		}
		if (nuevaContrasena.length() < 4) {
			throw new AuthException("La contraseña debe tener al menos 4 caracteres");
		}

		Optional<Usuario> optionalUsuario = usuarioRepository.findByUsername(username.trim());
		if (optionalUsuario.isEmpty()) {
			throw new AuthException("No se encontró un usuario con ese nombre");
		}

		Usuario usuario = optionalUsuario.get();
		if (!usuario.isActivo()) {
			throw new AuthException("El usuario se encuentra inactivo");
		}

		usuario.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
		usuario.setRequiereCambioContrasena(false);
		usuarioRepository.save(usuario);
	}

	/**
	 * Recupera la contraseña: genera una temporal y la envía al email registrado del usuario.
	 *
	 * @param email correo electrónico del usuario
	 * @throws AuthException si no se encuentra usuario con ese email
	 */
	public void recuperarContrasena(String email) {
		if (email == null || email.isBlank()) {
			throw new AuthException("El correo electrónico es obligatorio");
		}

		Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email.trim());
		if (optionalUsuario.isEmpty()) {
			throw new AuthException("No se encontró un usuario con ese correo electrónico");
		}

		Usuario usuario = optionalUsuario.get();
		if (!usuario.isActivo()) {
			throw new AuthException("El usuario se encuentra inactivo");
		}

		// Generar contraseña temporal de 8 caracteres
		String contrasenaTemporal = generarContrasenaTemporal(8);
		usuario.setPasswordHash(passwordEncoder.encode(contrasenaTemporal));
		usuario.setRequiereCambioContrasena(true);
		usuarioRepository.save(usuario);

		// Enviar por correo
		try {
			EmailService emailService = new EmailService();
			String asunto = "Academia de Belleza - Recuperación de contraseña";
			String cuerpo = "Hola " + usuario.getNombreCompleto() + ",\n\n"
					+ "Se ha solicitado la recuperación de tu contraseña.\n\n"
					+ "Tu nueva contraseña temporal es: " + contrasenaTemporal + "\n\n"
					+ "Por favor, inicia sesión y cambia tu contraseña lo antes posible.\n\n"
					+ "Si no solicitaste este cambio, contacta al administrador.\n\n"
					+ "— Academia de Belleza";
			emailService.enviarCorreo(email.trim(), asunto, cuerpo);
		} catch (Exception e) {
			throw new AuthException("Se actualizó la contraseña pero no se pudo enviar el correo: " + e.getMessage());
		}
	}

	private String generarContrasenaTemporal(int longitud) {
		String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(longitud);
		for (int i = 0; i < longitud; i++) {
			sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
		}
		return sb.toString();
	}
}
