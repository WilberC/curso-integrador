package pe.plazavea.perecibles.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.TipoMovimiento;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Merma;
import pe.plazavea.perecibles.model.MovimientoInventario;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.LoteRepository;
import pe.plazavea.perecibles.repository.MermaRepository;
import pe.plazavea.perecibles.repository.MovimientoInventarioRepository;
import pe.plazavea.perecibles.repository.ProductoPerecibleRepository;

@Service
public class InventarioServicio {

    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final MermaRepository mermaRepository;
    private final ProductoPerecibleRepository productoRepository;

    public InventarioServicio(
            LoteRepository loteRepository,
            MovimientoInventarioRepository movimientoRepository,
            MermaRepository mermaRepository,
            ProductoPerecibleRepository productoRepository
    ) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.mermaRepository = mermaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Lote registrarIngreso(Lote lote, Usuario usuario) {
        lote.setFechaIngreso(LocalDate.now());
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setCantidadActual(lote.getCantidadInicialValue());
        lote.setUsuarioRegistro(usuario);
        Lote saved = loteRepository.save(lote);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(TipoMovimiento.INGRESO);
        movimiento.setCantidad(saved.getCantidadInicialValue());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setLote(saved);
        movimiento.setUsuario(usuario);
        movimientoRepository.save(movimiento);

        return saved;
    }

    @Transactional
    public void registrarRetiro(Integer idLote, Double cantidad, TipoMovimiento tipo, String motivo, Usuario usuario) {
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        if (cantidad > lote.getCantidadActualValue()) {
            throw new RuntimeException("Cantidad mayor al stock disponible");
        }

        lote.setCantidadActual(lote.getCantidadActualValue() - cantidad);
        if (lote.getCantidadActualValue() <= 0) {
            lote.setEstado(EstadoLote.RETIRADO);
        } else if (tipo == TipoMovimiento.RETIRO) {
            lote.setEstado(EstadoLote.VENCIDO);
        }
        loteRepository.save(lote);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMotivo(motivo);
        movimiento.setLote(lote);
        movimiento.setUsuario(usuario);
        MovimientoInventario savedMovimiento = movimientoRepository.save(movimiento);

        if (tipo == TipoMovimiento.RETIRO || tipo == TipoMovimiento.REMATE || tipo == TipoMovimiento.DONACION) {
            Merma merma = new Merma();
            merma.setCantidad(cantidad);
            merma.setFechaRegistro(LocalDateTime.now());
            merma.setMotivo(motivo);
            merma.setLote(lote);
            merma.setMovimiento(savedMovimiento);
            merma.setUsuario(usuario);
            mermaRepository.save(merma);
        }
    }

    public List<Lote> consultarStock() {
        return loteRepository.findByEstadoNot(EstadoLote.RETIRADO);
    }

    public List<Lote> buscarPorProducto(Integer idProducto) {
        return loteRepository.findByProductoIdProductoAndEstadoNot(idProducto, EstadoLote.RETIRADO);
    }

    public List<Lote> buscarProximosAVencer(int dias) {
        return loteRepository.findProximosAVencer(LocalDate.now(), LocalDate.now().plusDays(dias));
    }

    public List<ProductoPerecible> listarProductos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }
}
