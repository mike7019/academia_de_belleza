package org.example.academia.service;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Maestro;
import org.example.academia.repository.CursoRepository;
import org.example.academia.repository.MaestroRepository;
import org.example.academia.repository.MaestroRepositoryImpl;
import org.example.academia.security.AuthorizationService;
import org.example.academia.util.BusinessException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones de Curso (KPI y consultas simples).
 */
public class CursoService {

    private final CursoRepository cursoRepository;
    private final MaestroRepository maestroRepository;
    private final AuthorizationService authorizationService;

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
        return cursoRepository.findCursosAbiertos();
    }

    public long getTotalCuposDisponibles() {
        return cursoRepository.getTotalCuposDisponibles();
    }

    /**
     * MAE-10: Asocia un maestro a un curso validando que exista y este activo.
     */
    public Curso asociarMaestro(Long cursoId, Long maestroId) {
        authorizationService.requirePermission("CURSO_EDITAR");

        if (cursoId == null) {
            throw new BusinessException("Debe indicar el curso a actualizar");
        }
        if (maestroId == null) {
            throw new BusinessException("Debe seleccionar un maestro valido");
        }

        Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
        if (cursoOpt.isEmpty()) {
            throw new BusinessException("No se encontro el curso con ID: " + cursoId);
        }

        Optional<Maestro> maestroOpt = maestroRepository.findById(maestroId);
        if (maestroOpt.isEmpty()) {
            throw new BusinessException("No se encontro el maestro con ID: " + maestroId);
        }

        Maestro maestro = maestroOpt.get();
        if (!maestro.isActivo()) {
            throw new BusinessException("No se puede asignar un maestro inactivo");
        }

        Curso curso = cursoOpt.get();
        curso.setMaestro(maestro);
        return cursoRepository.save(curso);
    }
}
