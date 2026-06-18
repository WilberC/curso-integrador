package pe.plazavea.perecibles.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.model.Lote;

public interface LoteRepository extends JpaRepository<Lote, Integer> {
    List<Lote> findAllByOrderByFechaIngresoDesc();

    List<Lote> findByEstado(EstadoLote estado);

    List<Lote> findByEstadoNot(EstadoLote estado);

    List<Lote> findByNumeroLoteStartingWith(String prefix);

    List<Lote> findByFechaVencimientoBeforeAndEstadoNot(LocalDate fecha, EstadoLote estado);

    List<Lote> findByProductoIdProductoAndEstadoNot(Integer idProducto, EstadoLote estado);

    @Query("""
            SELECT l FROM Lote l
            WHERE l.fechaVencimiento BETWEEN :inicio AND :fin
              AND l.estado NOT IN (
                  pe.plazavea.perecibles.enums.EstadoLote.RETIRADO,
                  pe.plazavea.perecibles.enums.EstadoLote.VENCIDO
              )
            ORDER BY l.fechaVencimiento ASC
            """)
    List<Lote> findProximosAVencer(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}
