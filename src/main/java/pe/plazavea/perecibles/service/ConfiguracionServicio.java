package pe.plazavea.perecibles.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.CategoriaRepository;
import pe.plazavea.perecibles.repository.ConfiguracionAlertaRepository;

@Service
public class ConfiguracionServicio {

    private final ConfiguracionAlertaRepository configuracionRepository;
    private final CategoriaRepository categoriaRepository;

    public ConfiguracionServicio(
            ConfiguracionAlertaRepository configuracionRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.configuracionRepository = configuracionRepository;
        this.categoriaRepository = categoriaRepository;
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
}
