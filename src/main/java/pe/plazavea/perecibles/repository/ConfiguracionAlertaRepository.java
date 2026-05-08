package pe.plazavea.perecibles.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;

public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, Integer> {
    Optional<ConfiguracionAlerta> findFirstByActivoTrue();

    Optional<ConfiguracionAlerta> findByUsuarioConfigIdUsuario(Integer idUsuario);
}
