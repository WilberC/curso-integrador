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
import java.time.LocalDateTime;
import pe.plazavea.perecibles.enums.TipoMovimiento;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @Column(length = 255)
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_lote", nullable = false)
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    protected MovimientoInventario() {
    }

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public Lote getLote() {
        return lote;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
