package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.EstadoNomina;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nomina_profesor")
@Getter
@Setter
public class NominaProfesor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "periodo_nomina_id")
	private PeriodoNomina periodoNomina;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "maestro_id")
	private Maestro maestro;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoNomina estado;

	@Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
	private BigDecimal montoTotal;

	@Column(name = "fecha_calculo")
	private LocalDateTime fechaCalculo;

	@Column(name = "fecha_aprobacion")
	private LocalDateTime fechaAprobacion;

	@Column(name = "fecha_pago")
	private LocalDateTime fechaPago;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movimiento_financiero_id")
	private MovimientoFinanciero movimientoFinanciero;

	@OneToMany(mappedBy = "nominaProfesor")
	private List<DetalleNominaProfesor> detalles = new ArrayList<>();
}



