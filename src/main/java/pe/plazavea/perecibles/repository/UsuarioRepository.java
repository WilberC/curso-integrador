package pe.plazavea.perecibles.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(RolUsuario rol);

    List<Usuario> findByRolAndActivo(RolUsuario rol, boolean activo);
}
