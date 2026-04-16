package org.example.academia.dto;

import org.example.academia.domain.enums.TipoPagoProfesor;

public class MaestroDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String numeroDocumento;
    private String telefono;
    private String email;
    private TipoPagoProfesor tipoPagoProfesor;
    private boolean activo;

    // Constructor vacío
    public MaestroDTO() {}

    // Constructor con parámetros
    public MaestroDTO(Long id, String nombre, String apellido, String numeroDocumento, String telefono, String email, TipoPagoProfesor tipoPagoProfesor, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.email = email;
        this.tipoPagoProfesor = tipoPagoProfesor;
        this.activo = activo;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public TipoPagoProfesor getTipoPagoProfesor() { return tipoPagoProfesor; }
    public void setTipoPagoProfesor(TipoPagoProfesor tipoPagoProfesor) { this.tipoPagoProfesor = tipoPagoProfesor; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
