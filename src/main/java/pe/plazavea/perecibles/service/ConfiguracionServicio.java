package pe.plazavea.perecibles.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.CategoriaRepository;
import pe.plazavea.perecibles.repository.ConfiguracionAlertaRepository;
import pe.plazavea.perecibles.repository.ProductoPerecibleRepository;

@Service
public class ConfiguracionServicio {

    private final ConfiguracionAlertaRepository configuracionRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoPerecibleRepository productoRepository;

    public ConfiguracionServicio(
            ConfiguracionAlertaRepository configuracionRepository,
            CategoriaRepository categoriaRepository,
            ProductoPerecibleRepository productoRepository
    ) {
        this.configuracionRepository = configuracionRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public ConfiguracionAlerta obtenerConfiguracionActiva() {
        return configuracionRepository.findFirstByActivoTrue().orElseGet(this::crearPorDefecto);
    }

    @Transactional
    public ConfiguracionAlerta guardarUmbrales(
            int diasCriticos,
            int diasAdvertencia,
            int diasAvisoAnticipado,
            Usuario usuario
    ) {
        validarUmbrales(diasCriticos, diasAdvertencia, diasAvisoAnticipado);
        ConfiguracionAlerta config = obtenerConfiguracionActiva();
        config.setDiasCriticos(diasCriticos);
        config.setDiasAdvertencia(diasAdvertencia);
        config.setDiasAvisoAnticipado(diasAvisoAnticipado);
        config.setUsuarioConfig(usuario);
        config.setActivo(true);
        return configuracionRepository.save(config);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findByActivoTrueOrderByNombreAsc();
    }

    public List<ProductoPerecible> listarProductos() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Categoria crearCategoria(String nombre) {
        String limpio = limpiarNombre(nombre);
        categoriaRepository.findByNombreIgnoreCase(limpio).ifPresent(categoria -> {
            throw new RuntimeException("La categoria ya existe");
        });
        return categoriaRepository.save(new Categoria(limpio));
    }

    @Transactional
    public void cambiarEstadoCategoria(Integer idCategoria, boolean activo) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        categoria.setActivo(activo);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public ProductoPerecible crearProducto(
            String nombre,
            String descripcion,
            String unidadMedida,
            Integer idCategoria
    ) {
        String nombreLimpio = limpiarProducto(nombre);
        productoRepository.findByNombreIgnoreCase(nombreLimpio).ifPresent(producto -> {
            throw new RuntimeException("El producto ya existe");
        });
        ProductoPerecible producto = new ProductoPerecible();
        producto.setNombre(nombreLimpio);
        producto.setDescripcion(limpiarOpcional(descripcion));
        producto.setUnidadMedida(limpiarUnidad(unidadMedida));
        producto.setCategoria(buscarCategoria(idCategoria));
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    @Transactional
    public ProductoPerecible editarProducto(
            Integer idProducto,
            String nombre,
            String descripcion,
            String unidadMedida,
            Integer idCategoria
    ) {
        ProductoPerecible producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        String nombreLimpio = limpiarProducto(nombre);
        productoRepository.findByNombreIgnoreCase(nombreLimpio)
                .filter(existing -> !existing.getIdProducto().equals(idProducto))
                .ifPresent(existing -> {
                    throw new RuntimeException("El producto ya existe");
                });
        producto.setNombre(nombreLimpio);
        producto.setDescripcion(limpiarOpcional(descripcion));
        producto.setUnidadMedida(limpiarUnidad(unidadMedida));
        producto.setCategoria(buscarCategoria(idCategoria));
        return productoRepository.save(producto);
    }

    @Transactional
    public void cambiarEstadoProducto(Integer idProducto, boolean activo) {
        ProductoPerecible producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(activo);
        productoRepository.save(producto);
    }

    private ConfiguracionAlerta crearPorDefecto() {
        ConfiguracionAlerta config = new ConfiguracionAlerta();
        config.setDiasCriticos(1);
        config.setDiasAdvertencia(3);
        config.setDiasAvisoAnticipado(7);
        config.setActivo(true);
        return configuracionRepository.save(config);
    }

    private void validarUmbrales(int criticos, int advertencia, int aviso) {
        if (criticos < 0 || advertencia < 0 || aviso < 0) {
            throw new RuntimeException("Los umbrales no pueden ser negativos");
        }
        if (criticos > advertencia || advertencia > aviso) {
            throw new RuntimeException("Use el orden: criticos <= advertencia <= aviso anticipado");
        }
    }

    private String limpiarNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isBlank()) {
            throw new RuntimeException("El nombre de la categoria es obligatorio");
        }
        return limpio;
    }

    private Categoria buscarCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            throw new RuntimeException("La categoria del producto es obligatoria");
        }
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }

    private String limpiarProducto(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isBlank()) {
            throw new RuntimeException("El nombre del producto es obligatorio");
        }
        return limpio;
    }

    private String limpiarUnidad(String unidadMedida) {
        String limpio = limpiarOpcional(unidadMedida);
        if (limpio.isBlank()) {
            throw new RuntimeException("La unidad de medida es obligatoria");
        }
        return limpio;
    }

    private String limpiarOpcional(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
