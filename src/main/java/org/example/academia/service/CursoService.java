package org.example.academia.service;

import org.example.academia.domain.entity.Curso;
import org.example.academia.repository.CursoRepository;

import java.util.List;

/**
 * Servicio para operaciones de Curso (KPI y consultas simples).
 */
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> getCursosAbiertos() {
        return cursoRepository.findCursosAbiertos();
    }

    public long getTotalCuposDisponibles() {
        return cursoRepository.getTotalCuposDisponibles();
    }
}
