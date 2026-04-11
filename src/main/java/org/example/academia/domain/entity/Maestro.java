package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.TipoPagoProfesor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maestro")
@Getter
@Setter
public class Maestro {

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

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_pago_profesor", nullable = false, length = 20)
	private TipoPagoProfesor tipoPagoProfesor;

	@Column(name = "tarifa_hora", precision = 12, scale = 2)
	private BigDecimal tarifaHora;

	@Column(name = "salario_mensual", precision = 12, scale = 2)
	private BigDecimal salarioMensual;

	@Column(name = "tarifa_por_curso", precision = 12, scale = 2)
	private BigDecimal tarifaPorCurso;

	@Column(name = "porcentaje_por_curso", precision = 5, scale = 2)
	private BigDecimal porcentajePorCurso;

	@OneToMany(mappedBy = "maestro")
	private List<Curso> cursos = new ArrayList<>();

	@OneToMany(mappedBy = "maestro")
	private List<NominaProfesor> nominas = new ArrayList<>();
}



