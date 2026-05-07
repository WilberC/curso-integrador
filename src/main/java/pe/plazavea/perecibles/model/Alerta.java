package pe.plazavea.perecibles.model;

import java.time.LocalDate;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.enums.TipoAlerta;

public class Alerta {

    private final int id;
    private final TipoAlerta tipoAlerta;
    private final int diasParaVencer;
    private final LocalDate fechaGeneracion;
    private final EstadoAlerta estado;
    private final String loteNumero;

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
        this.id = id;
        this.tipoAlerta = tipoAlerta;
        this.diasParaVencer = diasParaVencer;
        this.fechaGeneracion = fechaGeneracion;
        this.estado = estado;
        this.loteNumero = loteNumero;
    }

    public int getId() {
        return id;
    }

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public int getDiasParaVencer() {
        return diasParaVencer;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public String getLoteNumero() {
        return loteNumero;
    }
}
