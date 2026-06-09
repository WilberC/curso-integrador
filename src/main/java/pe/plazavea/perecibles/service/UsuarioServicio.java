package pe.plazavea.perecibles.service;

import java.time.LocalDate;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.UsuarioRepository;

@Service
public class UsuarioServicio {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServicio(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!BCrypt.checkpw(password, usuario.getContrasena())) {
            throw new RuntimeException("Contrasena incorrecta");
        }
        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        return usuario;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void registrar(Usuario nuevo, String passwordPlano) {
        validarNombreAcceso(nuevo.getEmail());
        String email = nuevo.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El usuario ya existe");
        }
        nuevo.setEmail(email);
        nuevo.setContrasena(BCrypt.hashpw(passwordPlano, BCrypt.gensalt()));
        nuevo.setFechaCreacion(LocalDate.now());
        usuarioRepository.save(nuevo);
    }

    @Transactional
    public void editar(Integer idUsuario, String nombreCompleto, RolUsuario rol, boolean activo, Usuario solicitante) {
        if (solicitante == null || !solicitante.esSupervisor()) {
            throw new RuntimeException("Permiso denegado");
        }
        Usuario target = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        validarCambioEstado(target, activo, solicitante);
        validarCambioRol(target, rol, activo);
        String[] nombres = splitNombreCompleto(nombreCompleto);
        target.setNombre(nombres[0]);
        target.setApellido(nombres[1]);
        target.setRol(rol);
        target.setActivo(activo);
        usuarioRepository.save(target);
    }

    @Transactional
    public void cambiarEstado(Integer idUsuario, boolean activo, Usuario solicitante) {
        if (solicitante == null || !solicitante.esSupervisor()) {
            throw new RuntimeException("Permiso denegado");
        }
        Usuario target = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        validarCambioEstado(target, activo, solicitante);
        target.setActivo(activo);
        usuarioRepository.save(target);
    }

    private void validarNombreAcceso(String usuario) {
        if (usuario == null || !usuario.matches("^[A-Za-z0-9._-]+$")) {
            throw new RuntimeException("El usuario no acepta espacios ni caracteres especiales");
        }
    }

    private void validarCambioEstado(Usuario target, boolean activo, Usuario solicitante) {
        if (target.getIdUsuario().equals(solicitante.getIdUsuario()) && !activo) {
            throw new RuntimeException("El Supervisor activo no puede desactivarse a si mismo");
        }
        if (target.esSupervisor() && target.isActivo() && !activo
                && usuarioRepository.findByRolAndActivo(RolUsuario.SUPERVISOR, true).size() <= 1) {
            throw new RuntimeException("Debe existir al menos un Supervisor activo");
        }
    }

    private void validarCambioRol(Usuario target, RolUsuario nuevoRol, boolean activo) {
        if (target.esSupervisor() && target.isActivo() && (!activo || nuevoRol != RolUsuario.SUPERVISOR)
                && usuarioRepository.findByRolAndActivo(RolUsuario.SUPERVISOR, true).size() <= 1) {
            throw new RuntimeException("Debe existir al menos un Supervisor activo");
        }
    }

    private String[] splitNombreCompleto(String nombreCompleto) {
        String value = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        if (value.isBlank()) {
            throw new RuntimeException("El nombre completo es obligatorio");
        }
        int separator = value.lastIndexOf(' ');
        if (separator < 0) {
            return new String[]{value, ""};
        }
        return new String[]{value.substring(0, separator), value.substring(separator + 1)};
    }
}
