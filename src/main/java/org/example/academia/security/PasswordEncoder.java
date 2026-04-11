package org.example.academia.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Encapsula operaciones de hash y verificación de contraseñas usando BCrypt.
 */
public class PasswordEncoder {

	/**
	 * Calcula el hash BCrypt de una contraseña en texto plano.
	 */
	public String encode(String rawPassword) {
		if (rawPassword == null) {
			throw new IllegalArgumentException("La contraseña no puede ser nula");
		}
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
	}

	/**
	 * Verifica si una contraseña en texto plano coincide con un hash almacenado.
	 */
	public boolean matches(String rawPassword, String encodedPassword) {
		if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
			return false;
		}
		return BCrypt.checkpw(rawPassword, encodedPassword);
	}
}


