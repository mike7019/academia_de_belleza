package org.example.academia.mapper;

import org.example.academia.domain.entity.Maestro;
import org.example.academia.dto.MaestroDTO;

public class MaestroMapper {
    public static MaestroDTO toDTO(Maestro entity) {
        if (entity == null) {
            return null;
        }
        return new MaestroDTO(
                entity.getId(),
                entity.getNombre(),
                entity.getApellido(),
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getDireccion(),
                entity.getTipoPagoProfesor(),
                entity.getTarifaHora(),
                entity.getSalarioMensual(),
                entity.getTarifaPorCurso(),
                entity.getPorcentajePorCurso(),
                entity.isActivo()
        );
    }

    public static Maestro toEntity(MaestroDTO dto) {
        if (dto == null) {
            return null;
        }
        Maestro entity = new Maestro();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setApellido(dto.getApellido());
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());
        entity.setDireccion(dto.getDireccion());
        entity.setTipoPagoProfesor(dto.getTipoPagoProfesor());
        entity.setTarifaHora(dto.getTarifaHora());
        entity.setSalarioMensual(dto.getSalarioMensual());
        entity.setTarifaPorCurso(dto.getTarifaPorCurso());
        entity.setPorcentajePorCurso(dto.getPorcentajePorCurso());
        entity.setActivo(dto.isActivo());
        return entity;
    }
}
