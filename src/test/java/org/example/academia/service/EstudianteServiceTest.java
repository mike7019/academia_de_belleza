package org.example.academia.service;

import org.example.academia.dto.EstudianteDTO;
import org.example.academia.domain.entity.Estudiante;
import org.example.academia.repository.EstudianteRepository;
import org.example.academia.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EstudianteServiceTest {

    private EstudianteService service;
    private FakeRepository repo;
    private AuthorizationService auth;

    @BeforeEach
    public void setUp() {
        repo = new FakeRepository();
        auth = new AuthorizationService() {
            @Override
            public void requirePermission(String codigoPermiso) {
                // permitir siempre en tests
            }
        };
        service = new EstudianteService(repo, auth, null);
    }

    @Test
    public void testSaveSuccess() {
        EstudianteDTO dto = new EstudianteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setNumeroDocumento("12345678");

        EstudianteDTO saved = service.save(dto);
        assertNotNull(saved.getId());
        assertEquals("Ana", saved.getNombre());
        assertEquals("12345678", saved.getNumeroDocumento());
    }

    @Test
    public void testSaveDuplicateDocumentoThrows() {
        // guardar primero un estudiante con documento 111
        Estudiante existing = new Estudiante();
        existing.setNombre("Carlos");
        existing.setApellido("Lopez");
        existing.setNumeroDocumento("111");
        repo.save(existing);

        EstudianteDTO dto = new EstudianteDTO();
        dto.setNombre("María");
        dto.setApellido("Gomez");
        dto.setNumeroDocumento("111");

        assertThrows(BusinessException.class, () -> service.save(dto));
    }

    @Test
    public void testSaveMissingNombreThrows() {
        EstudianteDTO dto = new EstudianteDTO();
        dto.setApellido("Gomez");
        dto.setNumeroDocumento("222");

        assertThrows(BusinessException.class, () -> service.save(dto));
    }

    @Test
    public void testInactivateAndReactivate() {
        // crear estudiante
        Estudiante e = new Estudiante();
        e.setNombre("Luis");
        e.setApellido("Martinez");
        e.setNumeroDocumento("999");
        repo.save(e);

        // inactivar
        service.inactivate(e.getId());
        Optional<Estudiante> fromRepo = repo.findById(e.getId());
        assertTrue(fromRepo.isPresent());
        assertFalse(fromRepo.get().isActivo());
        assertNotNull(fromRepo.get().getFechaBaja());

        // reactivar
        service.reactivate(e.getId());
        Optional<Estudiante> afterReact = repo.findById(e.getId());
        assertTrue(afterReact.isPresent());
        assertTrue(afterReact.get().isActivo());
        assertNull(afterReact.get().getFechaBaja());
    }

    // Repositorio en memoria para pruebas
    static class FakeRepository implements EstudianteRepository {
        private final Map<Long, Estudiante> store = new HashMap<>();
        private long sequence = 1;

        @Override
        public Optional<Estudiante> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Estudiante> findByNumeroDocumento(String numeroDocumento) {
            return store.values().stream().filter(e -> Objects.equals(e.getNumeroDocumento(), numeroDocumento)).findFirst();
        }

        @Override
        public Estudiante save(Estudiante estudiante) {
            if (estudiante.getId() == null) {
                estudiante.setId(sequence++);
            }
            store.put(estudiante.getId(), estudiante);
            return estudiante;
        }

        @Override
        public List<Estudiante> findAll() {
            return new ArrayList<>(store.values());
        }
    }
}

