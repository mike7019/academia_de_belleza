package org.example.academia.dto;

import org.example.academia.domain.enums.EstadoCurso;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CursoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Integer cupoMaximo;
    private EstadoCurso estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long maestroId;
    private String maestroNombre;
    private int cuposOcupados;
    private int cuposDisponibles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public EstadoCurso getEstado() {
        return estado;
    }

    public void setEstado(EstadoCurso estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Long getMaestroId() {
        return maestroId;
    }

    public void setMaestroId(Long maestroId) {
        this.maestroId = maestroId;
    }

    public String getMaestroNombre() {
        return maestroNombre;
    }

    public void setMaestroNombre(String maestroNombre) {
        this.maestroNombre = maestroNombre;
    }

    public int getCuposOcupados() {
        return cuposOcupados;
    }

    public void setCuposOcupados(int cuposOcupados) {
        this.cuposOcupados = cuposOcupados;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }
}

