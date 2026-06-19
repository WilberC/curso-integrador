package pe.plazavea.perecibles.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
    List<Alerta> findByEstado(EstadoAlerta estado);

    List<Alerta> findByEstadoOrderByFechaGeneracionDesc(EstadoAlerta estado);

    List<Alerta> findAllByOrderByFechaGeneracionDesc();

    List<Alerta> findByLoteIdLote(Integer idLote);

    Optional<Alerta> findByLoteIdLoteAndEstado(Integer idLote, EstadoAlerta estado);

    Optional<Alerta> findFirstByLoteIdLoteOrderByFechaGeneracionDesc(Integer idLote);

    long countByEstado(EstadoAlerta estado);
}
