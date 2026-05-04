package org.example.academia.dto;

import org.example.academia.domain.enums.EstadoMatricula;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para Matrícula.
 * Contiene la información necesaria para la creación y visualización de matrículas.
 */
public class MatriculaDTO {

    private Long id;
    private LocalDate fecha;
    private EstadoMatricula estado;
    private BigDecimal valorBase;
    private BigDecimal descuento;
    private BigDecimal valorFinal;
    private String observaciones;

    // Referencias a entidades relacionadas
    private Long estudianteId;
    private String estudianteNombre;
    private String estudianteDocumento;

    private Long cursoId;
    private String cursoNombre;
    private BigDecimal precioCurso;

    // Constructores
    public MatriculaDTO() {
    }

    public MatriculaDTO(Long estudianteId, Long cursoId) {
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoMatricula getEstado() {
        return estado;
    }

    public void setEstado(EstadoMatricula estado) {
        this.estado = estado;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public String getEstudianteDocumento() {
        return estudianteDocumento;
    }

    public void setEstudianteDocumento(String estudianteDocumento) {
        this.estudianteDocumento = estudianteDocumento;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoNombre() {
        return cursoNombre;
    }

    public void setCursoNombre(String cursoNombre) {
        this.cursoNombre = cursoNombre;
    }

    public BigDecimal getPrecioCurso() {
        return precioCurso;
    }

    public void setPrecioCurso(BigDecimal precioCurso) {
        this.precioCurso = precioCurso;
    }

    @Override
    public String toString() {
        return "MatriculaDTO{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", estado=" + estado +
                ", valorBase=" + valorBase +
                ", descuento=" + descuento +
                ", valorFinal=" + valorFinal +
                ", estudianteId=" + estudianteId +
                ", estudianteNombre='" + estudianteNombre + '\'' +
                ", cursoId=" + cursoId +
                ", cursoNombre='" + cursoNombre + '\'' +
                '}';
    }
}

