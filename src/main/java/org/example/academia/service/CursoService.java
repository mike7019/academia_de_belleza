package org.example.academia.service;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Maestro;
import org.example.academia.domain.enums.EstadoCurso;
import org.example.academia.dto.CursoDTO;
import org.example.academia.mapper.CursoMapper;
import org.example.academia.repository.CursoRepository;
import org.example.academia.repository.CursoRepositoryImpl;
import org.example.academia.repository.MaestroRepository;
import org.example.academia.repository.MaestroRepositoryImpl;
import org.example.academia.security.AuthorizationService;
import org.example.academia.util.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de Curso.
 */
public class CursoService {

    private final CursoRepository cursoRepository;
    private final MaestroRepository maestroRepository;
    private final AuthorizationService authorizationService;

    public CursoService() {
        this(new CursoRepositoryImpl(), new MaestroRepositoryImpl(), new AuthorizationService());
    }

    public CursoService(CursoRepository cursoRepository) {
        this(cursoRepository, new MaestroRepositoryImpl(), new AuthorizationService());
    }

    public CursoService(CursoRepository cursoRepository,
                        MaestroRepository maestroRepository,
                        AuthorizationService authorizationService) {
        this.cursoRepository = cursoRepository;
        this.maestroRepository = maestroRepository;
        this.authorizationService = authorizationService;
    }

    public List<Curso> getCursosAbiertos() {
        authorizationService.requirePermission("CURSO_VER");
        return cursoRepository.findCursosAbiertos();
    }

    public long getTotalCuposDisponibles() {
        authorizationService.requirePermission("CURSO_VER");
        return cursoRepository.getTotalCuposDisponibles();
    }

    public List<CursoDTO> listarCursos(String nombre, EstadoCurso estado, LocalDate fechaInicio, LocalDate fechaFin) {
        authorizationService.requirePermission("CURSO_VER");
        List<Curso> cursos = cursoRepository.search(nombre, estado, fechaInicio, fechaFin);
        List<CursoDTO> resultado = new ArrayList<>();

        for (Curso curso : cursos) {
            CursoDTO dto = CursoMapper.toDTO(curso);
            long ocupados = cursoRepository.countMatriculasActivasByCursoId(curso.getId());
            int cupoMaximo = curso.getCupoMaximo();
            dto.setCuposOcupados((int) ocupados);
            dto.setCuposDisponibles(Math.max(0, cupoMaximo - (int) ocupados));
            resultado.add(dto);
        }

        return resultado;
    }

    public CursoDTO crearCurso(CursoDTO dto) {
        authorizationService.requirePermission("CURSO_CREAR");
        validarCursoDTO(dto, false);

        Curso curso = CursoMapper.toEntity(dto);
        Maestro maestro = obtenerMaestroActivo(dto.getMaestroId());
        curso.setMaestro(maestro);
        curso.setEstado(EstadoCurso.PLANIFICADO);

        Curso guardado = cursoRepository.save(curso);
        return enriquecerDTO(CursoMapper.toDTO(guardado));
    }

    public CursoDTO actualizarCurso(CursoDTO dto) {
        authorizationService.requirePermission("CURSO_EDITAR");
        validarCursoDTO(dto, true);

        Curso actual = cursoRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("No se encontró el curso con ID: " + dto.getId()));

        long ocupados = cursoRepository.countMatriculasActivasByCursoId(actual.getId());
        if (dto.getCupoMaximo() != null && dto.getCupoMaximo() < ocupados) {
            throw new BusinessException("El cupo máximo no puede ser menor a los cupos ocupados (" + ocupados + ")");
        }

        actual.setNombre(dto.getNombre().trim());
        actual.setDescripcion(normalizarTextoOpcional(dto.getDescripcion()));
        actual.setPrecioBase(dto.getPrecioBase());
        actual.setCupoMaximo(dto.getCupoMaximo());
        actual.setFechaInicio(dto.getFechaInicio());
        actual.setFechaFin(dto.getFechaFin());

        Maestro maestro = obtenerMaestroActivo(dto.getMaestroId());
        actual.setMaestro(maestro);

        if (actual.getEstado() == EstadoCurso.ABIERTO && maestro == null) {
            throw new BusinessException("Un curso ABIERTO debe tener maestro asignado");
        }

        Curso actualizado = cursoRepository.save(actual);
        return enriquecerDTO(CursoMapper.toDTO(actualizado));
    }

    public CursoDTO cambiarEstado(Long cursoId, EstadoCurso nuevoEstado) {
        authorizationService.requirePermission("CURSO_CAMBIAR_ESTADO");

        if (cursoId == null) {
            throw new BusinessException("Debe indicar el curso a actualizar");
        }
        if (nuevoEstado == null) {
            throw new BusinessException("Debe seleccionar el nuevo estado del curso");
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("No se encontró el curso con ID: " + cursoId));

        EstadoCurso actual = curso.getEstado();
        if (actual == nuevoEstado) {
            throw new BusinessException("El curso ya se encuentra en estado " + nuevoEstado);
        }

        if (!esTransicionValida(actual, nuevoEstado)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevoEstado);
        }

        if (nuevoEstado == EstadoCurso.ABIERTO && curso.getMaestro() == null) {
            throw new BusinessException("No se puede abrir un curso sin maestro asignado");
        }

        curso.setEstado(nuevoEstado);
        Curso guardado = cursoRepository.save(curso);
        return enriquecerDTO(CursoMapper.toDTO(guardado));
    }

    /**
     * MAE-10: Asocia un maestro a un curso validando que exista y esté activo.
     */
    public Curso asociarMaestro(Long cursoId, Long maestroId) {
        authorizationService.requirePermission("CURSO_EDITAR");

        if (cursoId == null) {
            throw new BusinessException("Debe indicar el curso a actualizar");
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("No se encontró el curso con ID: " + cursoId));

        Maestro maestro = obtenerMaestroActivo(maestroId);
        curso.setMaestro(maestro);

        return cursoRepository.save(curso);
    }

    private CursoDTO enriquecerDTO(CursoDTO dto) {
        if (dto == null || dto.getId() == null) {
            return dto;
        }
        long ocupados = cursoRepository.countMatriculasActivasByCursoId(dto.getId());
        int cupo = dto.getCupoMaximo() == null ? 0 : dto.getCupoMaximo();
        dto.setCuposOcupados((int) ocupados);
        dto.setCuposDisponibles(Math.max(0, cupo - (int) ocupados));
        return dto;
    }

    private void validarCursoDTO(CursoDTO dto, boolean requiereId) {
        if (dto == null) {
            throw new BusinessException("La información del curso es obligatoria");
        }
        if (requiereId && dto.getId() == null) {
            throw new BusinessException("El curso debe tener ID para actualizar");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("El nombre del curso es obligatorio");
        }
        if (dto.getPrecioBase() == null || dto.getPrecioBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio base debe ser mayor a 0");
        }
        if (dto.getCupoMaximo() == null || dto.getCupoMaximo() <= 0) {
            throw new BusinessException("El cupo máximo debe ser mayor a 0");
        }
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new BusinessException("Debe indicar fecha de inicio y fecha fin");
        }
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new BusinessException("La fecha fin no puede ser menor a la fecha inicio");
        }
        if (dto.getEstado() == null) {
            dto.setEstado(EstadoCurso.PLANIFICADO);
        }
    }

    private Maestro obtenerMaestroActivo(Long maestroId) {
        if (maestroId == null) {
            return null;
        }

        Optional<Maestro> maestroOpt = maestroRepository.findById(maestroId);
        if (maestroOpt.isEmpty()) {
            throw new BusinessException("No se encontró el maestro con ID: " + maestroId);
        }

        Maestro maestro = maestroOpt.get();
        if (!maestro.isActivo()) {
            throw new BusinessException("No se puede asignar un maestro inactivo");
        }
        return maestro;
    }

    private boolean esTransicionValida(EstadoCurso actual, EstadoCurso destino) {
        if (actual == EstadoCurso.PLANIFICADO) {
            return destino == EstadoCurso.ABIERTO || destino == EstadoCurso.CANCELADO;
        }
        if (actual == EstadoCurso.ABIERTO) {
            return destino == EstadoCurso.CERRADO || destino == EstadoCurso.CANCELADO;
        }
        return false;
    }

    private String normalizarTextoOpcional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
