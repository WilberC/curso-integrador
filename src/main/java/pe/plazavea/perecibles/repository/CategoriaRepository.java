package pe.plazavea.perecibles.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    List<Categoria> findAllByOrderByNombreAsc();
}
