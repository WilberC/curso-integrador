package pe.plazavea.perecibles.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.repository.LoteRepository;

class InventarioServicioTest {

    @Test
    void generaNumeroLoteConPrefijoDeCategoriaYSiguienteCorrelativo() {
        FakeLotes fakeLotes = new FakeLotes();
        fakeLotes.add("PV-LAC-001");
        fakeLotes.add("PV-LAC-002");
        InventarioServicio servicio = new InventarioServicio(fakeLotes.repository(), null, null, null);

        String numeroLote = servicio.generarNumeroLote(producto("Leche fresca", "Lacteos"));

        assertEquals("PV-LAC-003", numeroLote);
    }

    @Test
    void generaPrimerNumeroLoteParaCategoriaSinLotesPrevios() {
        FakeLotes fakeLotes = new FakeLotes();
        InventarioServicio servicio = new InventarioServicio(fakeLotes.repository(), null, null, null);

        String numeroLote = servicio.generarNumeroLote(producto("Producto test", "Test cate"));

        assertEquals("PV-TES-001", numeroLote);
    }

    private static ProductoPerecible producto(String nombre, String categoriaNombre) {
        ProductoPerecible producto = new ProductoPerecible();
        producto.setNombre(nombre);
        producto.setCategoria(new Categoria(categoriaNombre));
        return producto;
    }

    private static final class FakeLotes implements InvocationHandler {
        private final List<Lote> lotes = new ArrayList<>();

        LoteRepository repository() {
            return (LoteRepository) Proxy.newProxyInstance(
                    LoteRepository.class.getClassLoader(),
                    new Class<?>[]{LoteRepository.class},
                    this
            );
        }

        void add(String numeroLote) {
            Lote lote = new Lote();
            lote.setNumeroLote(numeroLote);
            lotes.add(lote);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findByNumeroLoteStartingWith" -> findByNumeroLoteStartingWith((String) args[0]);
                case "toString" -> "FakeLotes";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private List<Lote> findByNumeroLoteStartingWith(String prefix) {
            return lotes.stream()
                    .filter(lote -> lote.getNumeroLote().startsWith(prefix))
                    .toList();
        }
    }
}
