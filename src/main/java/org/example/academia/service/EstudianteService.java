package org.example.academia.service;

import org.example.academia.dto.EstudianteDTO;
import org.example.academia.mapper.EstudianteMapper;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.repository.EstudianteRepository;
import org.example.academia.repository.EstudianteRepositoryImpl;
import org.example.academia.repository.AuditoriaRepository;
import org.example.academia.repository.AuditoriaRepositoryImpl;
import org.example.academia.domain.entity.Auditoria;
import org.example.academia.security.SessionManager;
import org.example.academia.security.AuthorizationService;
import org.example.academia.security.AuthException;

import java.util.List;
import java.util.stream.Collectors;
import org.example.academia.dto.PaginatedResult;

/**
 * Servicio de Estudiantes con validaciones de negocio.
 */
public class EstudianteService {

	private final EstudianteRepository repository;
	private final AuthorizationService authorizationService;
	private final AuditoriaRepository auditoriaRepository;
	private static final String PERMISO_ESTUDIANTE_VER = "ESTUDIANTE_VER";
	private static final String PERMISO_ESTUDIANTE_LISTAR = "ESTUDIANTE_LISTAR";

	public EstudianteService() {
		this.repository = new EstudianteRepositoryImpl();
		this.authorizationService = new AuthorizationService();
		this.auditoriaRepository = new AuditoriaRepositoryImpl();
	}

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

	/**
	 * Buscar estudiante por número de documento (exacto). Devuelve null si no existe.
	 * Requiere permiso de lectura: ESTUDIANTE_VER o ESTUDIANTE_LISTAR.
	 */
	public EstudianteDTO findByNumeroDocumento(String numeroDocumento) {
		if (numeroDocumento == null || numeroDocumento.isBlank()) return null;
		if (!tienePermisoLectura()) {
			throw new AuthException("Permiso denegado: ESTUDIANTE_VER o ESTUDIANTE_LISTAR");
		}
		return repository.findByNumeroDocumento(numeroDocumento).map(EstudianteMapper::toDTO).orElse(null);
	}

	public List<EstudianteDTO> findAll() {
		if (!tienePermisoLectura()) {
			throw new AuthException("Permiso denegado: ESTUDIANTE_VER o ESTUDIANTE_LISTAR");
		}
		return repository.findAll().stream().map(EstudianteMapper::toDTO).collect(Collectors.toList());
	}

	/**
	 * Búsqueda paginada de estudiantes según criterios opcionales.
	 * @param nombre fragmento de nombre (opcional)
	 * @param apellido fragmento de apellido (opcional)
	 * @param numeroDocumento fragmento de documento (opcional)
	 * @param activo si no es null, filtra por activo
	 * @param page 0-based
	 * @param size tamaño de página (>0)
	 * @param sortBy campo válido para ordenar (id,nombre,apellido,numeroDocumento,email)
	 * @param asc true orden ascendente
	 */
	public PaginatedResult<EstudianteDTO> search(String nombre, String apellido, String numeroDocumento, Boolean activo,
												  int page, int size, String sortBy, boolean asc) {
		// Compatibilidad: aceptar codigo ESTUDIANTE_VER (seed V5) o ESTUDIANTE_LISTAR (seed V6).
		// Si el usuario no tiene ninguno de los dos permisos, lanzar AuthException.
		if (!tienePermisoLectura()) {
			throw new AuthException("Permiso denegado: ESTUDIANTE_VER o ESTUDIANTE_LISTAR");
		}

		if (page < 0) page = 0;
		if (size <= 0) size = 100; // valor por defecto razonable

		int offset = page * size;
		List<Estudiante> entities = repository.search(nombre, apellido, numeroDocumento, activo, offset, size, sortBy, asc);
		long total = repository.countByCriteria(nombre, apellido, numeroDocumento, activo);
		List<EstudianteDTO> items = entities.stream().map(EstudianteMapper::toDTO).collect(Collectors.toList());
		return new PaginatedResult<>(items, total, page, size);
	}

	/**
	 * Inactivar (soft-delete) un estudiante: marca activo=false y registra fecha de baja.
	 * Requiere permiso: ESTUDIANTE_INACTIVAR
	 */
	public void inactivate(Long id) {
		if (id == null) throw new BusinessException("Id de estudiante no proporcionado");
		authorizationService.requirePermission("ESTUDIANTE_INACTIVAR");
		Estudiante e = repository.findById(id).orElseThrow(() -> new BusinessException("Estudiante no encontrado"));
		if (!e.isActivo()) return; // ya inactivo
		String antes = EstudianteMapper.toDTO(e).toString();
		e.setActivo(false);
		e.setFechaBaja(java.time.LocalDate.now());
		Estudiante saved = repository.save(e);
		if (auditoriaRepository != null) {
			Auditoria a = new Auditoria();
			a.setFecha(java.time.LocalDateTime.now());
			a.setUsuario(SessionManager.getInstance().getCurrentUser());
			a.setAccion("INACTIVAR_ESTUDIANTE");
			a.setEntidad("Estudiante");
			a.setIdEntidad(saved.getId());
			a.setDetalleAntes(antes);
			a.setDetalleDespues(EstudianteMapper.toDTO(saved).toString());
			auditoriaRepository.save(a);
		}
	}

	/**
	 * Reactivar un estudiante previamente inactivado.
	 * Requiere permiso: ESTUDIANTE_INACTIVAR
	 */
	public void reactivate(Long id) {
		if (id == null) throw new BusinessException("Id de estudiante no proporcionado");
		authorizationService.requirePermission("ESTUDIANTE_INACTIVAR");
		Estudiante e = repository.findById(id).orElseThrow(() -> new BusinessException("Estudiante no encontrado"));
		if (e.isActivo()) return; // ya activo
		String antes = EstudianteMapper.toDTO(e).toString();
		e.setActivo(true);
		e.setFechaBaja(null);
		Estudiante saved = repository.save(e);
		if (auditoriaRepository != null) {
			Auditoria a = new Auditoria();
			a.setFecha(java.time.LocalDateTime.now());
			a.setUsuario(SessionManager.getInstance().getCurrentUser());
			a.setAccion("REACTIVAR_ESTUDIANTE");
			a.setEntidad("Estudiante");
			a.setIdEntidad(saved.getId());
			a.setDetalleAntes(antes);
			a.setDetalleDespues(EstudianteMapper.toDTO(saved).toString());
			auditoriaRepository.save(a);
		}
	}

	/**
	 * Alias orientados a UI para mantener el mismo contrato conceptual del módulo maestros.
	 */
	public List<EstudianteDTO> listarEstudiantesActivos() {
		if (!tienePermisoLectura()) {
			throw new AuthException("Permiso denegado: ESTUDIANTE_VER o ESTUDIANTE_LISTAR");
		}
		return repository.search(null, null, null, true, 0, Integer.MAX_VALUE, "nombre", true)
				.stream()
				.map(EstudianteMapper::toDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Lista estudiantes aplicando filtros opcionales de nombre, documento y estado.
	 */
	public List<EstudianteDTO> listarEstudiantes(String nombre, String numeroDocumento, Boolean activo) {
		if (!tienePermisoLectura()) {
			throw new AuthException("Permiso denegado: ESTUDIANTE_VER o ESTUDIANTE_LISTAR");
		}
		return repository.search(nombre, null, numeroDocumento, activo, 0, Integer.MAX_VALUE, "nombre", true)
				.stream()
				.map(EstudianteMapper::toDTO)
				.collect(Collectors.toList());
	}

	public EstudianteDTO guardarEstudiante(EstudianteDTO dto) {
		dto.setId(null);
		dto.setActivo(true);
		return save(dto);
	}

	public EstudianteDTO actualizarEstudiante(EstudianteDTO dto) {
		if (dto == null || dto.getId() == null) {
			throw new BusinessException("El estudiante debe tener un ID para actualizar");
		}
		return save(dto);
	}

	public void inactivarEstudiante(Long id) {
		inactivate(id);
	}

	private boolean tienePermisoLectura() {
		return authorizationService.hasPermission(PERMISO_ESTUDIANTE_VER)
				|| authorizationService.hasPermission(PERMISO_ESTUDIANTE_LISTAR);
	}
}

