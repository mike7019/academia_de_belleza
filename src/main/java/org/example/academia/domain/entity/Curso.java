package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.EstadoCurso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "curso")
@Getter
@Setter
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioBase;

	@Column(name = "cupo_maximo", nullable = false)
	private int cupoMaximo;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoCurso estado;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin", nullable = false)
	private LocalDate fechaFin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "maestro_id")
	private Maestro maestro;

	@OneToMany(mappedBy = "curso")
	private List<Matricula> matriculas = new ArrayList<>();
}



