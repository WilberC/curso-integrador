package pe.plazavea.perecibles.config;

import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.CategoriaRepository;
import pe.plazavea.perecibles.repository.ConfiguracionAlertaRepository;
import pe.plazavea.perecibles.repository.LoteRepository;
import pe.plazavea.perecibles.repository.ProductoPerecibleRepository;
import pe.plazavea.perecibles.repository.UsuarioRepository;

@Component
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoPerecibleRepository productoRepository;
    private final ConfiguracionAlertaRepository configRepository;
    private final LoteRepository loteRepository;
    private boolean seeded;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoPerecibleRepository productoRepository,
            ConfiguracionAlertaRepository configRepository,
            LoteRepository loteRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.configRepository = configRepository;
        this.loteRepository = loteRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (seeded || usuarioRepository.count() > 0) {
            return;
        }
        seeded = true;

        Usuario supervisor = usuario("Supervisor", "Principal", "supervisor@plazavea.com", RolUsuario.SUPERVISOR);
        Usuario operario = usuario("Operario", "Turno", "operario@plazavea.com", RolUsuario.OPERARIO);
        usuarioRepository.saveAll(List.of(supervisor, operario));

        Categoria lacteos = categoria("Lacteos", "Productos lacteos refrigerados");
        Categoria carnes = categoria("Carnes", "Carnes frescas y marinadas");
        Categoria embutidos = categoria("Embutidos", "Embutidos y fiambres");
        Categoria panaderia = categoria("Panaderia", "Productos de panaderia perecible");
        Categoria frutasVerduras = categoria("Frutas y Verduras", "Productos frescos de sala");
        Categoria otros = categoria("Otros", "Categoria general");
        categoriaRepository.saveAll(List.of(lacteos, carnes, embutidos, panaderia, frutasVerduras, otros));

        ProductoPerecible leche = producto("Leche fresca", lacteos, "litro");
        ProductoPerecible yogurt = producto("Yogurt natural", lacteos, "unidad");
        ProductoPerecible pollo = producto("Pechuga de pollo", carnes, "kg");
        ProductoPerecible carne = producto("Carne molida", carnes, "kg");
        ProductoPerecible jamon = producto("Jamon del pais", embutidos, "kg");
        ProductoPerecible pan = producto("Pan molde integral", panaderia, "unidad");
        productoRepository.saveAll(List.of(leche, yogurt, pollo, carne, jamon, pan));

        loteRepository.saveAll(List.of(
                lote("PV-LAC-001", leche, 120, 120, 14, "Camara lacteos", EstadoLote.DISPONIBLE, operario),
                lote("PV-LAC-002", yogurt, 80, 80, 5, "Gondola refrigerada", EstadoLote.PROXIMO_VENCER, operario),
                lote("PV-CAR-001", pollo, 45, 45, 2, "Camara carnes", EstadoLote.PROXIMO_VENCER, operario),
                lote("PV-CAR-002", carne, 30, 30, -1, "Camara carnes", EstadoLote.VENCIDO, supervisor),
                lote("PV-EMB-001", jamon, 35, 35, 21, "Vitrina embutidos", EstadoLote.DISPONIBLE, operario),
                lote("PV-PAN-001", pan, 60, 60, 3, "Panaderia", EstadoLote.PROXIMO_VENCER, operario)
        ));

        ConfiguracionAlerta config = new ConfiguracionAlerta();
        config.setDiasCriticos(1);
        config.setDiasAdvertencia(3);
        config.setDiasAvisoAnticipado(7);
        config.setActivo(true);
        config.setUsuarioConfig(supervisor);
        configRepository.save(config);
    }

    private Usuario usuario(String nombre, String apellido, String email, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setContrasena(BCrypt.hashpw("admin", BCrypt.gensalt()));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private Categoria categoria(String nombre, String descripcion) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        return categoria;
    }

    private ProductoPerecible producto(String nombre, Categoria categoria, String unidadMedida) {
        ProductoPerecible producto = new ProductoPerecible();
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        return producto;
    }

    private Lote lote(
            String numero,
            ProductoPerecible producto,
            double cantidadInicial,
            double cantidadActual,
            int venceEnDias,
            String ubicacion,
            EstadoLote estado,
            Usuario usuario
    ) {
        Lote lote = new Lote();
        lote.setNumeroLote(numero);
        lote.setProducto(producto);
        lote.setCantidadInicial(cantidadInicial);
        lote.setCantidadActual(cantidadActual);
        lote.setFechaIngreso(java.time.LocalDate.now());
        lote.setFechaVencimiento(java.time.LocalDate.now().plusDays(venceEnDias));
        lote.setUbicacion(ubicacion);
        lote.setEstado(estado);
        lote.setUsuarioRegistro(usuario);
        return lote;
    }
}
