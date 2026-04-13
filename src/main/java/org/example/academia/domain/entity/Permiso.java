package org.example.academia.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad Permiso.
 *
 * Representa una acción concreta habilitable (por ejemplo, ESTUDIANTE_CREAR).
 */
@Entity
@Table(name = "permiso")
@Getter
@Setter
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "modulo", length = 50)
    private String modulo;
}

