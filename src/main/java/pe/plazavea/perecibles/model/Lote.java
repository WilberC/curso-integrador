package pe.plazavea.perecibles.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import pe.plazavea.perecibles.enums.EstadoLote;

public class Lote {

    private final int id;
    private final String numeroLote;
    private final String producto;
    private final String categoria;
    private final int cantidadInicial;
    private final int cantidadActual;
    private final LocalDate fechaIngreso;
    private final LocalDate fechaVencimiento;
    private final String ubicacion;
    private EstadoLote estado;

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
        this.id = id;
        this.numeroLote = numeroLote;
        this.producto = producto;
        this.categoria = categoria;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public String getProducto() {
        return producto;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getCantidadInicial() {
        return cantidadInicial;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }

    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    public boolean estaVencido() {
        return getDiasParaVencer() < 0;
    }

    public boolean estaProximoAVencer(int diasUmbral) {
        long diasParaVencer = getDiasParaVencer();
        return diasParaVencer >= 0 && diasParaVencer <= diasUmbral;
    }
}
