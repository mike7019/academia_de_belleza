package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.academia.domain.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movimiento_financiero")
@Getter
@Setter
public class MovimientoFinanciero {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha", nullable = false)
	private LocalDateTime fecha;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 20)
	private TipoMovimiento tipo;

	@Column(name = "monto", nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	@Column(name = "concepto", nullable = false, length = 200)
	private String concepto;

	@Column(name = "origen", nullable = false, length = 20)
	private String origen;

	@Column(name = "id_origen")
	private Long idOrigen;

	@Column(name = "estado", nullable = false, length = 20)
	private String estado;

	@OneToMany(mappedBy = "movimientoFinanciero")
	private List<NominaProfesor> nominasAsociadas = new ArrayList<>();
}



