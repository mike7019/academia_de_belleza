package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago_estudiante")
@Getter
@Setter
public class PagoEstudiante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha", nullable = false)
	private LocalDateTime fecha;

	@Column(name = "monto", nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	@Column(name = "metodo_pago", nullable = false, length = 20)
	private String metodoPago;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoPago estado;

	@Column(name = "referencia", length = 50)
	private String referencia;

	@Column(name = "observaciones")
	private String observaciones;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "matricula_id", nullable = false)
	private Matricula matricula;
}



