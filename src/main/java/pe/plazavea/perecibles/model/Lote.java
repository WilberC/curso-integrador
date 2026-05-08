package pe.plazavea.perecibles.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import pe.plazavea.perecibles.enums.EstadoLote;

@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLote;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(name = "cantidad_inicial", nullable = false)
    private Double cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false)
    private Double cantidadActual;

    private LocalDate fechaIngreso;

    private LocalDate fechaVencimiento;

    @Column(length = 100)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private ProductoPerecible producto;

    @ManyToOne
    @JoinColumn(name = "id_usuario_reg", nullable = false)
    private Usuario usuarioRegistro;

    protected Lote() {
    }

    public Lote(
            int id,
            String numeroLote,
            String producto,
            String categoria,
            int cantidadInicial,
            int cantidadActual,
            LocalDate fechaIngreso,
            LocalDate fechaVencimiento,
            String ubicacion,
            EstadoLote estado
    ) {
        this.idLote = id;
        this.numeroLote = numeroLote;
        this.producto = new ProductoPerecible(producto, new Categoria(categoria));
        this.cantidadInicial = (double) cantidadInicial;
        this.cantidadActual = (double) cantidadActual;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.usuarioRegistro = new Usuario(0, "Sistema", "Mock", pe.plazavea.perecibles.enums.RolUsuario.OPERARIO);
    }

    public Integer getIdLote() {
        return idLote;
    }

    public int getId() {
        return idLote == null ? 0 : idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public String getProducto() {
        return producto == null ? "" : producto.getNombre();
    }

    public ProductoPerecible getProductoEntity() {
        return producto;
    }

    public void setProducto(ProductoPerecible producto) {
        this.producto = producto;
    }

    public String getCategoria() {
        if (producto == null || producto.getCategoriaEntity() == null) {
            return "";
        }
        return producto.getCategoriaEntity().getNombre();
    }

    public int getCantidadInicial() {
        return cantidadInicial == null ? 0 : cantidadInicial.intValue();
    }

    public Double getCantidadInicialValue() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Double cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public int getCantidadActual() {
        return cantidadActual == null ? 0 : cantidadActual.intValue();
    }

    public Double getCantidadActualValue() {
        return cantidadActual;
    }

    public void setCantidadActual(Double cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }

    public Usuario getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(Usuario usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    public boolean estaVencido() {
        return getDiasParaVencer() < 0;
    }

    public boolean estaProximoAVencer(int umbralDias) {
        long dias = getDiasParaVencer();
        return dias >= 0 && dias <= umbralDias;
    }

    public void actualizarEstado(int diasAmarilla, int diasRoja) {
        if (estaVencido()) {
            estado = EstadoLote.VENCIDO;
        } else if (estaProximoAVencer(diasAmarilla)) {
            estado = EstadoLote.PROXIMO_VENCER;
        } else {
            estado = EstadoLote.DISPONIBLE;
        }
    }
}
