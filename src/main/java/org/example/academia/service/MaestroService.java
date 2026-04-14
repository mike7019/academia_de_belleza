package org.example.academia.service;

import org.example.academia.domain.entity.Maestro;
import org.example.academia.repository.MaestroRepository;
import org.example.academia.repository.MaestroRepositoryImpl;
import org.example.academia.security.AuthorizationService;

import java.util.List;
import java.util.Optional;


/**
 * Servicio de Maestros.
 */
public class MaestroService {

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
        authorizationService.requirePermission("MAE_BUSCAR"); // Si aplica
        return maestroRepository.findByNumeroDocumento(numeroDocumento);
    }

    /**
     * Lista todos los maestros activos.
     *
     * @return una lista de maestros con activo = true.
     */
    public List<Maestro> listarMaestrosActivos() {
        authorizationService.requirePermission("MAE_LISTAR"); // Si aplica
        return maestroRepository.findByActivoTrue();
    }

    /**
     * Guarda un nuevo maestro en la base de datos.
     *
     * @param maestro el maestro a guardar.
     * @return el maestro guardado con ID asignado.
     * @throws IllegalArgumentException si el maestro ya existe (documento único) o datos inválidos.
     */
    public Maestro guardarMaestro(Maestro maestro) {
        authorizationService.requirePermission("MAE_CREAR");

        // Validaciones básicas
        if (maestro == null || maestro.getNumeroDocumento() == null || maestro.getNumeroDocumento().isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }

        if (maestro.getNombre() == null || maestro.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (maestro.getApellido() == null || maestro.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        // Verificar que no exista un maestro con el mismo documento
        Optional<Maestro> existente = maestroRepository.findByNumeroDocumento(maestro.getNumeroDocumento());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un maestro con el número de documento: " + maestro.getNumeroDocumento());
        }

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
        authorizationService.requirePermission("MAE_ACTUALIZAR");

        // Validaciones básicas
        if (maestro == null || maestro.getId() == null) {
            throw new IllegalArgumentException("El maestro debe tener un ID para actualizar");
        }

        if (maestro.getNombre() == null || maestro.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (maestro.getApellido() == null || maestro.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        return maestroRepository.save(maestro);
    }
}
