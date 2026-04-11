package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.EstadoMatricula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matricula")
@Getter
@Setter
public class Matricula {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoMatricula estado;

	@Column(name = "valor_base", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorBase;

	@Column(name = "descuento", nullable = false, precision = 12, scale = 2)
	private BigDecimal descuento;

	@Column(name = "valor_final", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorFinal;

	@Column(name = "observaciones")
	private String observaciones;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "estudiante_id", nullable = false)
	private Estudiante estudiante;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	@OneToMany(mappedBy = "matricula")
	private List<PagoEstudiante> pagos = new ArrayList<>();
}



