package org.example.academia.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para la entidad Estudiante.
 */
@Data
public class EstudianteDTO {
	private Long id;
	private String nombre;
	private String apellido;
	private String tipoDocumento;
	private String numeroDocumento;
	private String telefono;
	private String email;
	private boolean activo = true;
	private LocalDate fechaRegistro;
	private LocalDate fechaBaja;
}

