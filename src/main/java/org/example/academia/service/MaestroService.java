package org.example.academia.service;

import org.example.academia.domain.entity.Maestro;

import org.example.academia.repository.MaestroRepository;
import org.example.academia.repository.MaestroRepositoryImpl;
import org.example.academia.security.AuthorizationService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


/**
 * Servicio de Maestros.
 */
public class MaestroService {

    private static final String PERMISO_MAESTRO_VER = "MAESTRO_VER";

    private final MaestroRepository maestroRepository;
    private final AuthorizationService authorizationService;

    public MaestroService() {
        this.maestroRepository = new MaestroRepositoryImpl();
        this.authorizationService = new AuthorizationService();
    }

    /**
     * Busca un maestro activo por su número de documento.
     *
     * @param numeroDocumento el número de documento único del maestro.
     * @return un Optional con el maestro si existe y está activo, vacío en caso contrario.
     */
    public Optional<Maestro> buscarPorDocumento(String numeroDocumento) {
        authorizationService.requirePermission(PERMISO_MAESTRO_VER);
        return maestroRepository.findByNumeroDocumento(numeroDocumento);
    }

    /**
     * Lista todos los maestros activos.
     *
     * @return una lista de maestros con activo = true.
     */
    public List<Maestro> listarMaestrosActivos() {
        authorizationService.requirePermission(PERMISO_MAESTRO_VER);
        return maestroRepository.findByActivoTrue();
    }

    /**
     * MAE-11: expone la configuración de pago de un maestro para cálculos de nómina.
     */
    public Maestro obtenerMaestroParaNomina(Long maestroId) {
        authorizationService.requirePermission(PERMISO_MAESTRO_VER);
        if (maestroId == null) {
            throw new BusinessException("Debe indicar el maestro para cálculo de nómina");
        }

        Maestro maestro = maestroRepository.findById(maestroId)
                .orElseThrow(() -> new BusinessException("No se encontró el maestro con ID: " + maestroId));

        if (!maestro.isActivo()) {
            throw new BusinessException("El maestro está inactivo y no puede usarse en nómina");
        }

        validarModalidadPago(maestro);
        return maestro;
    }

    /**
     * Guarda un nuevo maestro en la base de datos.
     *
     * @param maestro el maestro a guardar.
     * @return el maestro guardado con ID asignado.
     * @throws IllegalArgumentException si el maestro ya existe (documento único) o datos inválidos.
     */
    public Maestro guardarMaestro(Maestro maestro) {
        authorizationService.requirePermission("MAESTRO_CREAR");

        // Validaciones básicas
        if (maestro == null || maestro.getNumeroDocumento() == null || maestro.getNumeroDocumento().isBlank()) {
            throw new BusinessException("El número de documento es obligatorio");
        }

        if (maestro.getNombre() == null || maestro.getNombre().isBlank()) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (maestro.getApellido() == null || maestro.getApellido().isBlank()) {
            throw new BusinessException("El apellido es obligatorio");
        }

        // Verificar que no exista un maestro con el mismo documento
        Optional<Maestro> existente = maestroRepository.findByNumeroDocumento(maestro.getNumeroDocumento());
        if (existente.isPresent()) {
            throw new BusinessException("Ya existe un maestro con el número de documento: " + maestro.getNumeroDocumento());
        }

        // Validar modalidad de pago
        validarModalidadPago(maestro);

        maestro.setActivo(true);
        return maestroRepository.save(maestro);
    }

    /**
     * Actualiza un maestro existente en la base de datos.
     *
     * @param maestro el maestro a actualizar (debe tener ID).
     * @return el maestro actualizado.
     * @throws IllegalArgumentException si el maestro no tiene ID o datos inválidos.
     */
    public Maestro actualizarMaestro(Maestro maestro) {
        authorizationService.requirePermission("MAESTRO_EDITAR");

        // Validaciones básicas
        if (maestro == null || maestro.getId() == null) {
            throw new BusinessException("El maestro debe tener un ID para actualizar");
        }

        if (maestro.getNombre() == null || maestro.getNombre().isBlank()) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (maestro.getApellido() == null || maestro.getApellido().isBlank()) {
            throw new BusinessException("El apellido es obligatorio");
        }

        // Validar documento único si cambia
        Optional<Maestro> existente = maestroRepository.findByNumeroDocumento(maestro.getNumeroDocumento());
        if (existente.isPresent() && !existente.get().getId().equals(maestro.getId())) {
            throw new BusinessException("Ya existe un maestro con el número de documento: " + maestro.getNumeroDocumento());
        }

        // Validar modalidad de pago
        validarModalidadPago(maestro);

        return maestroRepository.save(maestro);
    }

    /**
     * Valida que los datos de pago del maestro sean coherentes según su modalidad.
     */
    private void validarModalidadPago(Maestro maestro) {
        if (maestro.getTipoPagoProfesor() == null) {
            throw new BusinessException("Debe seleccionar una modalidad de pago para el maestro");
        }
        switch (maestro.getTipoPagoProfesor().name()) {
            case "POR_HORA":
                if (maestro.getTarifaHora() == null) {
                    throw new BusinessException("La tarifa por hora es obligatoria para modalidad POR_HORA");
                }
                validarMayorACero(maestro.getTarifaHora(), "La tarifa por hora");
                if (maestro.getSalarioMensual() != null || maestro.getTarifaPorCurso() != null || maestro.getPorcentajePorCurso() != null) {
                    throw new BusinessException("Solo la tarifa por hora debe estar informada para modalidad POR_HORA");
                }
                break;
            case "FIJO_MENSUAL":
                if (maestro.getSalarioMensual() == null) {
                    throw new BusinessException("El salario mensual es obligatorio para modalidad FIJO_MENSUAL");
                }
                validarMayorACero(maestro.getSalarioMensual(), "El salario mensual");
                if (maestro.getTarifaHora() != null || maestro.getTarifaPorCurso() != null || maestro.getPorcentajePorCurso() != null) {
                    throw new BusinessException("Solo el salario mensual debe estar informado para modalidad FIJO_MENSUAL");
                }
                break;
            case "POR_CURSO":
                if (maestro.getTarifaPorCurso() == null) {
                    throw new BusinessException("La tarifa por curso es obligatoria para modalidad POR_CURSO");
                }
                validarMayorACero(maestro.getTarifaPorCurso(), "La tarifa por curso");
                if (maestro.getTarifaHora() != null || maestro.getSalarioMensual() != null || maestro.getPorcentajePorCurso() != null) {
                    throw new BusinessException("Solo la tarifa por curso debe estar informada para modalidad POR_CURSO");
                }
                break;
            case "PORCENTAJE":
                if (maestro.getPorcentajePorCurso() == null) {
                    throw new BusinessException("El porcentaje por curso es obligatorio para modalidad PORCENTAJE");
                }
                validarMayorACero(maestro.getPorcentajePorCurso(), "El porcentaje por curso");
                if (maestro.getPorcentajePorCurso().compareTo(new BigDecimal("100")) > 0) {
                    throw new BusinessException("El porcentaje por curso no puede superar 100");
                }
                if (maestro.getTarifaHora() != null || maestro.getSalarioMensual() != null || maestro.getTarifaPorCurso() != null) {
                    throw new BusinessException("Solo el porcentaje debe estar informado para modalidad PORCENTAJE");
                }
                break;
            default:
                throw new BusinessException("Modalidad de pago no soportada: " + maestro.getTipoPagoProfesor());
        }
    }

    private void validarMayorACero(BigDecimal valor, String campo) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(campo + " debe ser mayor a 0");
        }
    }

    /**
     * Inactiva un maestro (soft delete).
     * @param id ID del maestro a inactivar
     */
    public void inactivarMaestro(Long id) {
        authorizationService.requirePermission("MAESTRO_INACTIVAR");
        Optional<Maestro> maestroOpt = maestroRepository.findById(id);
        if (maestroOpt.isEmpty()) {
            throw new BusinessException("No se encontró el maestro con ID: " + id);
        }
        Maestro maestro = maestroOpt.get();
        if (!maestro.isActivo()) {
            throw new BusinessException("El maestro ya está inactivo.");
        }
        maestro.setActivo(false);
        maestroRepository.save(maestro);
    }

    /**
     * Lista maestros aplicando filtros opcionales de nombre, documento y estado.
     *
     * @param nombre filtro por nombre (parcial), opcional.
     * @param numeroDocumento filtro por documento (parcial), opcional.
     * @param activo filtro por estado; null para incluir todos.
     * @return lista de maestros que cumplen los filtros.
     */
    public List<Maestro> listarMaestros(String nombre, String numeroDocumento, Boolean activo) {
        authorizationService.requirePermission(PERMISO_MAESTRO_VER);
        return maestroRepository.search(nombre, numeroDocumento, activo);
    }
}
