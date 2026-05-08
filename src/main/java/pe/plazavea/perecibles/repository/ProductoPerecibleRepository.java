package pe.plazavea.perecibles.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.plazavea.perecibles.model.ProductoPerecible;

public interface ProductoPerecibleRepository extends JpaRepository<ProductoPerecible, Integer> {
    List<ProductoPerecible> findByCategoriaIdCategoria(Integer idCategoria);

    List<ProductoPerecible> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoPerecible> findAllByOrderByNombreAsc();
}
