package pe.plazavea.perecibles.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.UsuarioRepository;

class UsuarioServicioTest {

    @Test
    void registrarAceptaCorreoElectronico() {
        FakeUsuarios fakeUsuarios = new FakeUsuarios();
        UsuarioServicio servicio = new UsuarioServicio(fakeUsuarios.repository());
        Usuario nuevo = usuario(null, "Maria", "Lopez", "Maria.Lopez@PlazaVea.COM", RolUsuario.OPERARIO, true);

        servicio.registrar(nuevo, "secreto");

        Usuario saved = fakeUsuarios.findByEmail("maria.lopez@plazavea.com").orElseThrow();
        assertEquals("maria.lopez@plazavea.com", saved.getEmail());
        assertEquals("Maria", saved.getNombre());
        assertEquals("Lopez", saved.getApellido());
        assertEquals(LocalDate.now(), saved.getFechaCreacion());
        assertNotEquals("secreto", saved.getContrasena());
        assertTrue(BCrypt.checkpw("secreto", saved.getContrasena()));
    }

    @Test
    void registrarRechazaCorreoInvalido() {
        FakeUsuarios fakeUsuarios = new FakeUsuarios();
        UsuarioServicio servicio = new UsuarioServicio(fakeUsuarios.repository());
        Usuario nuevo = usuario(null, "Maria", "Lopez", "maria lopez", RolUsuario.OPERARIO, true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> servicio.registrar(nuevo, "secreto"));

        assertEquals("Ingrese un correo electronico valido", exception.getMessage());
    }

    @Test
    void editarActualizaNombreRolYEstado() {
        FakeUsuarios fakeUsuarios = new FakeUsuarios();
        Usuario supervisor = fakeUsuarios.save(usuario(1, "Supervisor", "Principal", "supervisor@plazavea.com", RolUsuario.SUPERVISOR, true));
        Usuario target = fakeUsuarios.save(usuario(2, "Operario", "Turno", "operario@plazavea.com", RolUsuario.OPERARIO, true));
        UsuarioServicio servicio = new UsuarioServicio(fakeUsuarios.repository());

        servicio.editar(target.getIdUsuario(), "Ana Maria Torres", RolUsuario.SUPERVISOR, false, supervisor);

        Usuario edited = fakeUsuarios.findById(target.getIdUsuario()).orElseThrow();
        assertEquals("Ana Maria", edited.getNombre());
        assertEquals("Torres", edited.getApellido());
        assertEquals(RolUsuario.SUPERVISOR, edited.getRol());
        assertFalse(edited.isActivo());
    }

    @Test
    void editarNoPermiteQuitarUltimoSupervisorActivo() {
        FakeUsuarios fakeUsuarios = new FakeUsuarios();
        Usuario supervisor = fakeUsuarios.save(usuario(1, "Supervisor", "Principal", "supervisor@plazavea.com", RolUsuario.SUPERVISOR, true));
        UsuarioServicio servicio = new UsuarioServicio(fakeUsuarios.repository());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> servicio.editar(supervisor.getIdUsuario(), "Supervisor Principal", RolUsuario.OPERARIO, true, supervisor)
        );

        assertEquals("Debe existir al menos un Supervisor activo", exception.getMessage());
    }

    private static Usuario usuario(Integer id, String nombre, String apellido, String email, RolUsuario rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        usuario.setContrasena("hash");
        return usuario;
    }

    private static final class FakeUsuarios implements InvocationHandler {
        private final Map<Integer, Usuario> usuarios = new HashMap<>();
        private int nextId = 1;

        UsuarioRepository repository() {
            return (UsuarioRepository) Proxy.newProxyInstance(
                    UsuarioRepository.class.getClassLoader(),
                    new Class<?>[]{UsuarioRepository.class},
                    this
            );
        }

        Usuario save(Usuario usuario) {
            if (usuario.getIdUsuario() == null) {
                usuario.setIdUsuario(nextId++);
            }
            usuarios.put(usuario.getIdUsuario(), usuario);
            return usuario;
        }

        Optional<Usuario> findById(Integer id) {
            return Optional.ofNullable(usuarios.get(id));
        }

        Optional<Usuario> findByEmail(String email) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getEmail().equals(email))
                    .findFirst();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "save" -> save((Usuario) args[0]);
                case "findById" -> findById((Integer) args[0]);
                case "findByEmail" -> findByEmail((String) args[0]);
                case "existsByEmail" -> findByEmail((String) args[0]).isPresent();
                case "findAll" -> new ArrayList<>(usuarios.values());
                case "findByRolAndActivo" -> findByRolAndActivo((RolUsuario) args[0], (boolean) args[1]);
                case "toString" -> "FakeUsuarios";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private List<Usuario> findByRolAndActivo(RolUsuario rol, boolean activo) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getRol() == rol)
                    .filter(usuario -> usuario.isActivo() == activo)
                    .toList();
        }
    }
}
