package org.example.academia.security;

import org.example.academia.domain.entity.Rol;
import org.example.academia.domain.entity.Usuario;

/**
 * Servicio de autorización.
 *
 * Versión inicial: solo valida que exista un usuario autenticado.
 * En el futuro se integrará con entidades Rol/Permiso para verificar códigos
 * de permiso específicos (PAGO_REGISTRAR, ESTUDIANTE_CREAR, etc.).
 */
public class AuthorizationService {

	private final SessionManager sessionManager;

	public AuthorizationService() {
		this.sessionManager = SessionManager.getInstance();
	}

	/**
	 * Verifica si el usuario actual tiene un permiso.
	 *
	 * Implementación mínima:
	 * - Si no hay usuario autenticado → false.
	 * - Si hay usuario autenticado → true (no se discrimina por permisos aún).
	 *
	 * Esto permite ir añadiendo llamadas a requirePermission() en los servicios
	 * sin romper el flujo actual, y más adelante sustituir la lógica interna por
	 * una basada en roles/permisos desde BD.
	 */
	public boolean hasPermission(String codigoPermiso) {
		Usuario current = sessionManager.getCurrentUser();
		if (current == null) {
			return false;
		}

		if (current.getRoles() == null || current.getRoles().isEmpty()) {
			return false;
		}

		// Recorremos roles activos y sus permisos asociados para comprobar el código de permiso.
		return current.getRoles().stream()
				.filter(Rol::isActivo)
				.filter(rol -> rol.getPermisos() != null)
				.flatMap(rol -> rol.getPermisos().stream())
				.anyMatch(p -> p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(codigoPermiso));
	}

	/**
	 * Lanza excepción si el usuario actual no tiene el permiso requerido.
	 */
	public void requirePermission(String codigoPermiso) {
		if (!hasPermission(codigoPermiso)) {
			if (!sessionManager.isAuthenticated()) {
				throw new AuthException("Debe iniciar sesión para realizar esta acción");
			}
			throw new AuthException("Permiso denegado: " + codigoPermiso);
		}
	}
}


