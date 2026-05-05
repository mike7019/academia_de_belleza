package org.example.academia.dto;

import java.time.LocalDate;

/**
 * DTO para el reporte de estudiantes.
 */
public class StudentReportDTO {
    private Long id;
    private String nombreCompleto;
    private String numeroDocumento;
    private String telefono;
    private String email;
    private String cursoMatriculado;
    private String estado;
    private LocalDate fechaRegistro;

    public StudentReportDTO(Long id, String nombreCompleto, String numeroDocumento, String telefono, String email, String cursoMatriculado, String estado, LocalDate fechaRegistro) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.email = email;
        this.cursoMatriculado = cursoMatriculado;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCursoMatriculado() { return cursoMatriculado; }
    public void setCursoMatriculado(String cursoMatriculado) { this.cursoMatriculado = cursoMatriculado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

