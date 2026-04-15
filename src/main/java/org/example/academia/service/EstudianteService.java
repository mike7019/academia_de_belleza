package org.example.academia.service;

import org.example.academia.dto.EstudianteDTO;
import org.example.academia.mapper.EstudianteMapper;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.repository.EstudianteRepository;
import org.example.academia.repository.AuditoriaRepository;
import org.example.academia.domain.entity.Auditoria;
import org.example.academia.security.SessionManager;
import org.example.academia.security.AuthorizationService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Estudiantes con validaciones de negocio.
 */
public class EstudianteService {

	private final EstudianteRepository repository;
	private final AuthorizationService authorizationService;
	private final AuditoriaRepository auditoriaRepository;

	public EstudianteService(EstudianteRepository repository, AuthorizationService authorizationService, AuditoriaRepository auditoriaRepository) {
		this.repository = repository;
		this.authorizationService = authorizationService;
		this.auditoriaRepository = auditoriaRepository;
	}

	/**
	 * Crea o actualiza un estudiante. Realiza validaciones de negocio:
	 * - nombre y apellido obligatorios
	 * - numeroDocumento obligatorio y único
	 */
	public EstudianteDTO save(EstudianteDTO dto) {

		if (dto == null) {
			throw new BusinessException("Datos de estudiante no proporcionados");
		}

		// Verificación de permisos por operación: crear o editar
		if (dto.getId() == null) {
			authorizationService.requirePermission("ESTUDIANTE_CREAR");
		} else {
			authorizationService.requirePermission("ESTUDIANTE_EDITAR");
		}
		if (dto.getNombre() == null || dto.getNombre().isBlank()) {
			throw new BusinessException("El nombre es obligatorio");
		}
		if (dto.getApellido() == null || dto.getApellido().isBlank()) {
			throw new BusinessException("El apellido es obligatorio");
		}
		if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isBlank()) {
			throw new BusinessException("El número de documento es obligatorio");
		}

		// Verificar unicidad de numeroDocumento
		repository.findByNumeroDocumento(dto.getNumeroDocumento()).ifPresent(existing -> {
			if (dto.getId() == null || !existing.getId().equals(dto.getId())) {
				throw new BusinessException("Ya existe un estudiante con ese número de documento");
			}
		});

		// Validación de formato de email y telefono (si provistos)
		if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
			if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				throw new BusinessException("El email tiene un formato inválido");
			}
		}
		if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
			// En Java las barras invertidas deben escaparse en literales de cadena
			if (!dto.getTelefono().matches("^[0-9+()\\-\\s]{6,20}$")) {
				throw new BusinessException("El teléfono tiene un formato inválido");
			}
		}

		// Auditoría: obtener estado antes (si existe)
		String detalleAntes = "";
		if (dto.getId() != null) {
			detalleAntes = repository.findById(dto.getId()).map(EstudianteMapper::toDTO).map(Object::toString).orElse("");
		}

		Estudiante entidad = EstudianteMapper.toEntity(dto);
		Estudiante saved = repository.save(entidad);

		// Auditoría: registrar después
		String detalleDespues = EstudianteMapper.toDTO(saved).toString();
		if (auditoriaRepository != null) {
			Auditoria a = new Auditoria();
			a.setFecha(java.time.LocalDateTime.now());
			a.setUsuario(SessionManager.getInstance().getCurrentUser());
			a.setAccion(dto.getId() == null ? "CREAR_ESTUDIANTE" : "MODIFICAR_ESTUDIANTE");
			a.setEntidad("Estudiante");
			a.setIdEntidad(saved.getId());
			a.setDetalleAntes(detalleAntes);
			a.setDetalleDespues(detalleDespues);
			auditoriaRepository.save(a);
		}

		return EstudianteMapper.toDTO(saved);
	}

	public EstudianteDTO findById(Long id) {
		return repository.findById(id).map(EstudianteMapper::toDTO).orElse(null);
	}

	public List<EstudianteDTO> findAll() {
		return repository.findAll().stream().map(EstudianteMapper::toDTO).collect(Collectors.toList());
	}
}

