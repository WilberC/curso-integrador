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
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.enums.TipoAlerta;

@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlerta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false)
    private int diasParaVencer;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAlerta estado = EstadoAlerta.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "id_lote", nullable = false)
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "id_usuario_atiende", nullable = true)
    private Usuario usuarioAtiende;

    @Transient
    private String loteNumero;

    protected Alerta() {
    }

    public Alerta(int id, TipoAlerta tipoAlerta, int diasParaVencer, String loteNumero, EstadoAlerta estado) {
        this(id, tipoAlerta, diasParaVencer, LocalDate.now(), loteNumero, estado);
    }

    public Alerta(
            int id,
            TipoAlerta tipoAlerta,
            int diasParaVencer,
            LocalDate fechaGeneracion,
            String loteNumero,
            EstadoAlerta estado
    ) {
        this.idAlerta = id;
        this.tipoAlerta = tipoAlerta;
        this.diasParaVencer = diasParaVencer;
        this.fechaGeneracion = fechaGeneracion.atStartOfDay();
        this.loteNumero = loteNumero;
        this.estado = estado;
    }

    public Integer getIdAlerta() {
        return idAlerta;
    }

    public int getId() {
        return idAlerta == null ? 0 : idAlerta;
    }

    public void setIdAlerta(Integer idAlerta) {
        this.idAlerta = idAlerta;
    }

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(TipoAlerta tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public int getDiasParaVencer() {
        return diasParaVencer;
    }

    public void setDiasParaVencer(int diasParaVencer) {
        this.diasParaVencer = diasParaVencer;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlerta estado) {
        this.estado = estado;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Usuario getUsuarioAtiende() {
        return usuarioAtiende;
    }

    public void setUsuarioAtiende(Usuario usuarioAtiende) {
        this.usuarioAtiende = usuarioAtiende;
    }

    public String getLoteNumero() {
        if (lote != null) {
            return lote.getNumeroLote() + " - " + lote.getProducto();
        }
        return loteNumero;
    }

    public boolean esCritica() {
        return tipoAlerta == TipoAlerta.VENCIDO || diasParaVencer <= 2;
    }
}
