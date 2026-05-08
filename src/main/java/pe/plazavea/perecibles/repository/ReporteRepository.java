package pe.plazavea.perecibles.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.enums.TipoReporte;
import pe.plazavea.perecibles.model.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    List<Reporte> findByTipo(TipoReporte tipo);

    List<Reporte> findByFechaGeneracionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Reporte> findByUsuarioIdUsuario(Integer idUsuario);
}
