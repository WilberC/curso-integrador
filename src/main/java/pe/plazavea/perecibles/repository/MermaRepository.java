package pe.plazavea.perecibles.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.model.Merma;

public interface MermaRepository extends JpaRepository<Merma, Integer> {
    List<Merma> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Merma> findByLoteIdLote(Integer idLote);
}
