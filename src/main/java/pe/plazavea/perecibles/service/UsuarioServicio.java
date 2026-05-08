package pe.plazavea.perecibles.service;

import java.time.LocalDate;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void registrar(Usuario nuevo, String passwordPlano) {
        nuevo.setEmail(nuevo.getEmail().trim().toLowerCase());
        nuevo.setContrasena(BCrypt.hashpw(passwordPlano, BCrypt.gensalt()));
        nuevo.setFechaCreacion(LocalDate.now());
        usuarioRepository.save(nuevo);
    }

    @Transactional
    public void cambiarEstado(Integer idUsuario, boolean activo, Usuario solicitante) {
        if (solicitante == null || !solicitante.esSupervisor()) {
            throw new RuntimeException("Permiso denegado");
        }
        Usuario target = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        target.setActivo(activo);
        usuarioRepository.save(target);
    }
}
