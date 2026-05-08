package pe.plazavea.perecibles.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.model.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {
    List<MovimientoInventario> findByLoteIdLote(Integer idLote);

    List<MovimientoInventario> findByUsuarioIdUsuario(Integer idUsuario);

    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);
}
