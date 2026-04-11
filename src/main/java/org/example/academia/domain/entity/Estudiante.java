package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Estudiante.
 */
@Entity
@Table(name = "estudiante")
@Getter
@Setter
public class Estudiante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;

	@Column(name = "apellido", nullable = false, length = 50)
	private String apellido;

	@Column(name = "tipo_documento", nullable = false, length = 20)
	private String tipoDocumento;

	@Column(name = "numero_documento", nullable = false, length = 30, unique = true)
	private String numeroDocumento;

	@Column(name = "telefono", length = 20)
	private String telefono;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "direccion", length = 150)
	private String direccion;

	@Column(name = "fecha_registro", nullable = false)
	private LocalDate fechaRegistro;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "fecha_baja")
	private LocalDate fechaBaja;

	@OneToMany(mappedBy = "estudiante")
	private List<Matricula> matriculas = new ArrayList<>();
}



