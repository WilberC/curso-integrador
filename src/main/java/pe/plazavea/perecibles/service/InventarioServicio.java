package pe.plazavea.perecibles.service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
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

    private static final String LOTE_PREFIX = "PV";
    private static final int CATEGORY_CODE_LENGTH = 3;

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
        if (lote.getNumeroLote() == null || lote.getNumeroLote().isBlank()) {
            lote.setNumeroLote(generarNumeroLote(lote.getProductoEntity()));
        } else {
            lote.setNumeroLote(lote.getNumeroLote().trim());
        }
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

    @Transactional
    public Lote editarLote(
            Integer idLote,
            Integer idProducto,
            Double cantidad,
            LocalDate fechaVencimiento,
            String ubicacion
    ) {
        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        if (fechaVencimiento == null) {
            throw new RuntimeException("La fecha de vencimiento es obligatoria");
        }
        if (idProducto == null) {
            throw new RuntimeException("El producto del lote es obligatorio");
        }
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        ProductoPerecible producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        lote.setProducto(producto);
        lote.setCantidadInicial(cantidad);
        lote.setCantidadActual(cantidad);
        lote.setFechaVencimiento(fechaVencimiento);
        lote.setUbicacion(limpiarUbicacion(ubicacion));
        if (lote.getEstado() == EstadoLote.RETIRADO) {
            lote.setEstado(EstadoLote.DISPONIBLE);
        }
        return loteRepository.save(lote);
    }

    public String generarNumeroLote(ProductoPerecible producto) {
        String prefix = prefixFor(producto);
        int nextSequence = loteRepository.findByNumeroLoteStartingWith(prefix).stream()
                .map(Lote::getNumeroLote)
                .mapToInt(numero -> sequenceFrom(numero, prefix))
                .max()
                .orElse(0) + 1;
        return prefix + String.format(Locale.ROOT, "%03d", nextSequence);
    }

    private String prefixFor(ProductoPerecible producto) {
        if (producto == null || producto.getCategoriaEntity() == null) {
            throw new RuntimeException("Seleccione un producto con categoria");
        }
        String categoryCode = categoryCode(producto.getCategoriaEntity().getNombre());
        return LOTE_PREFIX + "-" + categoryCode + "-";
    }

    private String categoryCode(String categoria) {
        String normalized = Normalizer.normalize(categoria == null ? "" : categoria, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new RuntimeException("La categoria del producto no permite generar el lote");
        }
        return normalized.length() <= CATEGORY_CODE_LENGTH
                ? normalized
                : normalized.substring(0, CATEGORY_CODE_LENGTH);
    }

    private int sequenceFrom(String numeroLote, String prefix) {
        if (numeroLote == null || !numeroLote.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(numeroLote.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String limpiarUbicacion(String ubicacion) {
        String value = ubicacion == null ? "" : ubicacion.trim().replaceAll("\\s+", " ");
        if (value.isBlank()) {
            throw new RuntimeException("La ubicacion es obligatoria");
        }
        return value;
    }
}
