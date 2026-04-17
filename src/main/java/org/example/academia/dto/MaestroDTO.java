package org.example.academia.dto;

import org.example.academia.domain.enums.TipoPagoProfesor;

import java.math.BigDecimal;

public class MaestroDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String email;
    private String direccion;
    private TipoPagoProfesor tipoPagoProfesor;
    private BigDecimal tarifaHora;
    private BigDecimal salarioMensual;
    private BigDecimal tarifaPorCurso;
    private BigDecimal porcentajePorCurso;
    private boolean activo;

    public MaestroDTO() {
    }

    public MaestroDTO(Long id, String nombre, String apellido, String tipoDocumento, String numeroDocumento,
                      String telefono, String email, String direccion, TipoPagoProfesor tipoPagoProfesor,
                      BigDecimal tarifaHora, BigDecimal salarioMensual, BigDecimal tarifaPorCurso,
                      BigDecimal porcentajePorCurso, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.tipoPagoProfesor = tipoPagoProfesor;
        this.tarifaHora = tarifaHora;
        this.salarioMensual = salarioMensual;
        this.tarifaPorCurso = tarifaPorCurso;
        this.porcentajePorCurso = porcentajePorCurso;
        this.activo = activo;
    }

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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public TipoPagoProfesor getTipoPagoProfesor() {
        return tipoPagoProfesor;
    }

    public void setTipoPagoProfesor(TipoPagoProfesor tipoPagoProfesor) {
        this.tipoPagoProfesor = tipoPagoProfesor;
    }

    public BigDecimal getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(BigDecimal tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public BigDecimal getSalarioMensual() {
        return salarioMensual;
    }

    public void setSalarioMensual(BigDecimal salarioMensual) {
        this.salarioMensual = salarioMensual;
    }

    public BigDecimal getTarifaPorCurso() {
        return tarifaPorCurso;
    }

    public void setTarifaPorCurso(BigDecimal tarifaPorCurso) {
        this.tarifaPorCurso = tarifaPorCurso;
    }

    public BigDecimal getPorcentajePorCurso() {
        return porcentajePorCurso;
    }

    public void setPorcentajePorCurso(BigDecimal porcentajePorCurso) {
        this.porcentajePorCurso = porcentajePorCurso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
