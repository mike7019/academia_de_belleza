package org.example.academia.mapper;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Maestro;
import org.example.academia.dto.CursoDTO;

public class CursoMapper {

    private CursoMapper() {
    }

    public static CursoDTO toDTO(Curso entity) {
        if (entity == null) {
            return null;
        }

        CursoDTO dto = new CursoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecioBase(entity.getPrecioBase());
        dto.setCupoMaximo(entity.getCupoMaximo());
        dto.setEstado(entity.getEstado());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaFin(entity.getFechaFin());

        Maestro maestro = entity.getMaestro();
        if (maestro != null) {
            dto.setMaestroId(maestro.getId());
            dto.setMaestroNombre(maestro.getNombre() + " " + maestro.getApellido());
        }

        return dto;
    }

    public static Curso toEntity(CursoDTO dto) {
        if (dto == null) {
            return null;
        }

        Curso entity = new Curso();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecioBase(dto.getPrecioBase());
        entity.setCupoMaximo(dto.getCupoMaximo() == null ? 0 : dto.getCupoMaximo());
        entity.setEstado(dto.getEstado());
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        return entity;
    }
}

