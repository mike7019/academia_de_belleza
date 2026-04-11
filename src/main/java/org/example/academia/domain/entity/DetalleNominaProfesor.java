package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_nomina_profesor")
@Getter
@Setter
public class DetalleNominaProfesor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nomina_profesor_id", nullable = false)
	private NominaProfesor nominaProfesor;

	@Column(name = "tipo_concepto", nullable = false, length = 20)
	private String tipoConcepto;

	@Column(name = "descripcion", length = 200)
	private String descripcion;

	@Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
	private BigDecimal cantidad;

	@Column(name = "tarifa", nullable = false, precision = 12, scale = 2)
	private BigDecimal tarifa;

	@Column(name = "monto", nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	@Column(name = "referencia_externa")
	private Long referenciaExterna;
}



