package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
public class Auditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha", nullable = false)
	private LocalDateTime fecha;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "accion", nullable = false, length = 50)
	private String accion;

	@Column(name = "entidad", nullable = false, length = 50)
	private String entidad;

	@Column(name = "id_entidad")
	private Long idEntidad;

	@Column(name = "detalle_antes")
	private String detalleAntes;

	@Column(name = "detalle_despues")
	private String detalleDespues;
}



