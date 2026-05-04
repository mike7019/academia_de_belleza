package org.example.academia.mapper;

import org.example.academia.domain.entity.Curso;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.domain.entity.Matricula;
import org.example.academia.dto.MatriculaDTO;

/**
 * Mapper para convertir entre entidad Matricula y DTO MatriculaDTO.
 */
public class MatriculaMapper {

    private MatriculaMapper() {
    }

    /**
     * Convierte una entidad Matricula a DTO MatriculaDTO.
     */
    public static MatriculaDTO toDTO(Matricula entity) {
        if (entity == null) {
            return null;
        }

        MatriculaDTO dto = new MatriculaDTO();
        dto.setId(entity.getId());
        dto.setFecha(entity.getFecha());
        dto.setEstado(entity.getEstado());
        dto.setValorBase(entity.getValorBase());
        dto.setDescuento(entity.getDescuento());
        dto.setValorFinal(entity.getValorFinal());
        dto.setObservaciones(entity.getObservaciones());

        // Enriquecer con información del estudiante
        Estudiante estudiante = entity.getEstudiante();
        if (estudiante != null) {
            dto.setEstudianteId(estudiante.getId());
            dto.setEstudianteNombre(estudiante.getNombre() + " " + estudiante.getApellido());
            dto.setEstudianteDocumento(estudiante.getNumeroDocumento());
        }

        // Enriquecer con información del curso
        Curso curso = entity.getCurso();
        if (curso != null) {
            dto.setCursoId(curso.getId());
            dto.setCursoNombre(curso.getNombre());
            dto.setPrecioCurso(curso.getPrecioBase());
        }

        return dto;
    }

    /**
     * Convierte un DTO MatriculaDTO a entidad Matricula.
     */
    public static Matricula toEntity(MatriculaDTO dto) {
        if (dto == null) {
            return null;
        }

        Matricula entity = new Matricula();
        entity.setId(dto.getId());
        entity.setFecha(dto.getFecha());
        entity.setEstado(dto.getEstado());
        entity.setValorBase(dto.getValorBase());
        entity.setDescuento(dto.getDescuento() != null ? dto.getDescuento() : java.math.BigDecimal.ZERO);
        entity.setValorFinal(dto.getValorFinal());
        entity.setObservaciones(dto.getObservaciones());

        // Nota: Las relaciones (estudiante, curso) se establecen en el servicio
        return entity;
    }
}

