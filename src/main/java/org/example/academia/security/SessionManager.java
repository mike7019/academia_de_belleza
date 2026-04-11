package org.example.academia.security;

import org.example.academia.domain.entity.Usuario;

/**
 * Gestiona la sesión del usuario autenticado actual (singleton simple en memoria).
 */
public class SessionManager {

	private static final SessionManager INSTANCE = new SessionManager();

	private Usuario currentUser;

	private SessionManager() {
	}

	public static SessionManager getInstance() {
		return INSTANCE;
	}

	public void login(Usuario usuario) {
		this.currentUser = usuario;
	}

	public void logout() {
		this.currentUser = null;
	}

	public Usuario getCurrentUser() {
		return currentUser;
	}

	public boolean isAuthenticated() {
		return currentUser != null;
	}
}


