package org.example.academia.service;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.domain.entity.Matricula;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.domain.enums.EstadoMatricula;
import org.example.academia.dto.MatriculaDTO;
import org.example.academia.mapper.MatriculaMapper;
import org.example.academia.repository.*;
import org.example.academia.security.AuthorizationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de Matrícula.
 *
 * Gestiona:
 * - Creación de matrículas (USER STORY 115)
 * - Validaciones de cupo disponible
 * - Validación de que curso esté ABIERTO
 * - Cálculo de valor final (descuentos)
 * - Cambio de estado de matrículas
 */
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;
    private final AuthorizationService authorizationService;

    public MatriculaService() {
        this(new MatriculaRepositoryImpl(),
             new EstudianteRepositoryImpl(),
             new CursoRepositoryImpl(),
             new AuthorizationService());
    }

    public MatriculaService(MatriculaRepository matriculaRepository,
                           EstudianteRepository estudianteRepository,
                           CursoRepository cursoRepository,
                           AuthorizationService authorizationService) {
        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * USER STORY 115: Crear matrícula
     *
     * Validaciones:
     * 1. Curso debe existir y estar en estado ABIERTO
     * 2. Estudiante debe existir y estar activo
     * 3. No debe existir matrícula previa (estudiante ya inscrito)
     * 4. Cupo disponible en el curso
     * 5. Cálculo de valor final (precio base - descuento)
     */
    public MatriculaDTO crearMatricula(Long estudianteId, Long cursoId, BigDecimal descuento, String observaciones) {
        authorizationService.requirePermission("MATRICULA_CREAR");

        // Validar parámetros
        if (estudianteId == null) {
            throw new BusinessException("Debe seleccionar un estudiante");
        }
        if (cursoId == null) {
            throw new BusinessException("Debe seleccionar un curso");
        }

        // Obtener y validar estudiante
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado"));

        if (!estudiante.isActivo()) {
            throw new BusinessException("No se puede matricular un estudiante inactivo");
        }

        // Obtener y validar curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("Curso no encontrado"));

        // Validar estado del curso
        if (curso.getEstado() != EstadoCurso.ABIERTO) {
            throw new BusinessException("El curso debe estar en estado ABIERTO para permitir matrículas. " +
                    "Estado actual: " + curso.getEstado());
        }

        // Validar que no exista matrícula previa
        Optional<Matricula> existente = matriculaRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId);
        if (existente.isPresent()) {
            throw new BusinessException("El estudiante ya está matriculado en este curso");
        }

        // Validar cupo disponible
        long cuposOcupados = matriculaRepository.countMatriculasActivasByCursoId(cursoId);
        if (cuposOcupados >= curso.getCupoMaximo()) {
            throw new BusinessException("No hay cupos disponibles en este curso. " +
                    "Cupo máximo: " + curso.getCupoMaximo() + ", ocupados: " + cuposOcupados);
        }

        // Crear matrícula
        Matricula matricula = new Matricula();
        matricula.setFecha(LocalDate.now());
        matricula.setEstado(EstadoMatricula.PENDIENTE);
        matricula.setEstudiante(estudiante);
        matricula.setCurso(curso);

        // Cálculo de valor final
        BigDecimal valorBase = curso.getPrecioBase();
        BigDecimal descuentoFinal = descuento != null ? descuento : BigDecimal.ZERO;

        // Validar que el descuento no sea negativo ni mayor al valor base
        if (descuentoFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El descuento no puede ser negativo");
        }
        if (descuentoFinal.compareTo(valorBase) > 0) {
            throw new BusinessException("El descuento no puede ser mayor al valor base del curso");
        }

        BigDecimal valorFinal = valorBase.subtract(descuentoFinal);

        matricula.setValorBase(valorBase);
        matricula.setDescuento(descuentoFinal);
        matricula.setValorFinal(valorFinal);
        matricula.setObservaciones(observaciones);

        // Guardar matrícula
        Matricula guardada = matriculaRepository.save(matricula);

        return MatriculaMapper.toDTO(guardada);
    }

    /**
     * Listar todas las matrículas con filtros opcionales.
     */
    public List<MatriculaDTO> listarMatriculas(Long estudianteId, Long cursoId, EstadoMatricula estado,
                                               LocalDate fechaDesde, LocalDate fechaHasta) {
        authorizationService.requirePermission("MATRICULA_VER");

        List<Matricula> matriculas = matriculaRepository.search(estudianteId, cursoId, estado, fechaDesde, fechaHasta);
        List<MatriculaDTO> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            resultado.add(MatriculaMapper.toDTO(m));
        }

        return resultado;
    }

    /**
     * Obtener matrícula por ID.
     */
    public MatriculaDTO obtenerMatricula(Long matriculaId) {
        authorizationService.requirePermission("MATRICULA_VER");

        if (matriculaId == null) {
            throw new BusinessException("Debe proporcionar ID de matrícula");
        }

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new BusinessException("Matrícula no encontrada"));

        return MatriculaMapper.toDTO(matricula);
    }

    /**
     * Cambiar estado de matrícula.
     *
     * Transiciones válidas:
     * - PENDIENTE → ACTIVA
     * - ACTIVA → CANCELADA o FINALIZADA
     */
    public MatriculaDTO cambiarEstado(Long matriculaId, EstadoMatricula nuevoEstado) {
        authorizationService.requirePermission("MATRICULA_VER");

        if (matriculaId == null) {
            throw new BusinessException("Debe proporcionar ID de matrícula");
        }
        if (nuevoEstado == null) {
            throw new BusinessException("Debe seleccionar el nuevo estado");
        }

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new BusinessException("Matrícula no encontrada"));

        EstadoMatricula estadoActual = matricula.getEstado();

        if (estadoActual == nuevoEstado) {
            throw new BusinessException("La matrícula ya se encuentra en estado " + nuevoEstado);
        }

        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new BusinessException("Transición no válida: " + estadoActual + " → " + nuevoEstado);
        }

        matricula.setEstado(nuevoEstado);
        Matricula actualizada = matriculaRepository.save(matricula);

        return MatriculaMapper.toDTO(actualizada);
    }

    /**
     * Listar matrículas de un estudiante específico.
     */
    public List<MatriculaDTO> listarMatriculasEstudiante(Long estudianteId) {
        authorizationService.requirePermission("MATRICULA_VER");

        if (estudianteId == null) {
            throw new BusinessException("Debe proporcionar ID de estudiante");
        }

        List<Matricula> matriculas = matriculaRepository.findByEstudianteId(estudianteId);
        List<MatriculaDTO> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            resultado.add(MatriculaMapper.toDTO(m));
        }

        return resultado;
    }

    /**
     * Listar matrículas de un curso específico.
     */
    public List<MatriculaDTO> listarMatriculasCurso(Long cursoId) {
        authorizationService.requirePermission("MATRICULA_VER");

        if (cursoId == null) {
            throw new BusinessException("Debe proporcionar ID de curso");
        }

        List<Matricula> matriculas = matriculaRepository.findByCursoId(cursoId);
        List<MatriculaDTO> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            resultado.add(MatriculaMapper.toDTO(m));
        }

        return resultado;
    }

    /**
     * USER STORY 116: Cancelar matrícula
     * 
     * Cambios:
     * - Estado: ACTIVA/PENDIENTE → CANCELADA
     * - Libera cupo (automático al cambiar estado)
     * - No elimina datos (soft cancel)
     */
    public MatriculaDTO cancelarMatricula(Long matriculaId) {
        authorizationService.requirePermission("MATRICULA_CANCELAR");
        
        if (matriculaId == null) {
            throw new BusinessException("Debe proporcionar ID de matrícula");
        }
        
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new BusinessException("Matrícula no encontrada"));
        
        EstadoMatricula estadoActual = matricula.getEstado();
        
        // Validar que pueda cancelarse
        if (estadoActual == EstadoMatricula.CANCELADA) {
            throw new BusinessException("La matrícula ya se encuentra cancelada");
        }
        
        if (estadoActual == EstadoMatricula.FINALIZADA) {
            throw new BusinessException("No se puede cancelar una matrícula finalizada");
        }
        
        // Cambiar a CANCELADA (libera cupo automáticamente)
        matricula.setEstado(EstadoMatricula.CANCELADA);
        Matricula cancelada = matriculaRepository.save(matricula);
        
        return MatriculaMapper.toDTO(cancelada);
    }

    /**
     * Validar transiciones de estado permitidas.
     */
    private boolean esTransicionValida(EstadoMatricula actual, EstadoMatricula destino) {
        if (actual == EstadoMatricula.PENDIENTE) {
            return destino == EstadoMatricula.ACTIVA || destino == EstadoMatricula.CANCELADA;
        }
        if (actual == EstadoMatricula.ACTIVA) {
            return destino == EstadoMatricula.FINALIZADA || destino == EstadoMatricula.CANCELADA;
        }
        return false;
    }
}
