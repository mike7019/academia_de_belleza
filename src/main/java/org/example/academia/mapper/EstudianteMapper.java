package org.example.academia.mapper;

import org.example.academia.domain.entity.Estudiante;
import org.example.academia.dto.EstudianteDTO;

import java.time.LocalDate;

/**
 * Mapper simple entre Estudiante y EstudianteDTO.
 */
public class EstudianteMapper {

	public static EstudianteDTO toDTO(Estudiante e) {
		if (e == null) return null;
		EstudianteDTO dto = new EstudianteDTO();
		dto.setId(e.getId());
		dto.setNombre(e.getNombre());
		dto.setApellido(e.getApellido());
		dto.setTipoDocumento(e.getTipoDocumento());
		dto.setNumeroDocumento(e.getNumeroDocumento());
		dto.setTelefono(e.getTelefono());
		dto.setEmail(e.getEmail());
		 dto.setDireccion(e.getDireccion());
		dto.setActivo(e.isActivo());
		dto.setFechaRegistro(e.getFechaRegistro());
		dto.setFechaBaja(e.getFechaBaja());
		return dto;
	}

	public static Estudiante toEntity(EstudianteDTO dto) {
		if (dto == null) return null;
		Estudiante e = new Estudiante();
		e.setId(dto.getId());
		e.setNombre(dto.getNombre());
		e.setApellido(dto.getApellido());
		e.setTipoDocumento(dto.getTipoDocumento());
		e.setNumeroDocumento(dto.getNumeroDocumento());
		e.setTelefono(dto.getTelefono());
		e.setEmail(dto.getEmail());
		 e.setDireccion(dto.getDireccion());
		e.setActivo(dto.isActivo());
		// fechas: mantener fechaRegistro si ya existe o setear hoy si es null
		e.setFechaRegistro(dto.getFechaRegistro() == null ? LocalDate.now() : dto.getFechaRegistro());
		e.setFechaBaja(dto.getFechaBaja());
		return e;
	}
}

