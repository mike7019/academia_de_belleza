package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad Usuario.
 *
 * Representa cuentas de usuario del sistema, con relación 1:N hacia Auditoria.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombreCompleto;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "intentos_fallidos", nullable = false)
	private int intentosFallidos = 0;

	@Column(name = "ultimo_acceso")
	private LocalDateTime ultimoAcceso;

	@OneToMany(mappedBy = "usuario")
	private List<Auditoria> auditorias = new ArrayList<>();

	@ManyToMany
	@JoinTable(
			name = "usuario_rol",
			joinColumns = @JoinColumn(name = "usuario_id"),
			inverseJoinColumns = @JoinColumn(name = "rol_id")
	)
	private Set<Rol> roles = new HashSet<>();
}



