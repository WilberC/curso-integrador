package pe.plazavea.perecibles.mock;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.enums.TipoAlerta;
import pe.plazavea.perecibles.model.Alerta;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Usuario;

public final class MockData {

    private static final List<Lote> LOTES = new ArrayList<>();
    private static final List<Alerta> ALERTAS = new ArrayList<>();

    static {
        LocalDate today = LocalDate.now();
        LOTES.addAll(List.of(
                new Lote(1, "L-001", "Leche Gloria 1L", "Lacteos", 100, 100, today, today.plusDays(20), "Anaquel A1", EstadoLote.DISPONIBLE),
                new Lote(2, "L-002", "Yogur Fresa 500g", "Lacteos", 60, 48, today.minusDays(3), today.plusDays(12), "Camara B2", EstadoLote.DISPONIBLE),
                new Lote(3, "L-003", "Pollo Entero 1.8kg", "Carnes", 30, 22, today.minusDays(1), today.plusDays(9), "Camara C1", EstadoLote.DISPONIBLE),
                new Lote(4, "L-004", "Jamon del Pais 200g", "Embutidos", 40, 15, today.minusDays(5), today.plusDays(5), "Anaquel A3", EstadoLote.PROXIMO_VENCER),
                new Lote(5, "L-005", "Pan de Molde Bimbo", "Panaderia", 24, 10, today.minusDays(2), today.plusDays(3), "Anaquel D1", EstadoLote.PROXIMO_VENCER),
                new Lote(6, "L-006", "Queso Fresco 250g", "Lacteos", 20, 8, today.minusDays(10), today.minusDays(1), "Anaquel A2", EstadoLote.VENCIDO)
        ));
        ALERTAS.addAll(List.of(
                new Alerta(1, TipoAlerta.PROXIMO_VENCER, 5, "L-004 - Jamon del Pais", EstadoAlerta.PENDIENTE),
                new Alerta(2, TipoAlerta.PROXIMO_VENCER, 3, "L-005 - Pan de Molde Bimbo", EstadoAlerta.PENDIENTE),
                new Alerta(3, TipoAlerta.VENCIDO, 0, "L-006 - Queso Fresco 250g", EstadoAlerta.PENDIENTE)
        ));
    }

    private MockData() {
    }

    public static List<Lote> getLotes() {
        return LOTES;
    }

    public static void addLote(Lote lote) {
        LOTES.add(lote);
    }

    public static int nextLoteId() {
        return LOTES.stream().mapToInt(Lote::getId).max().orElse(0) + 1;
    }

    public static List<Alerta> getAlertas() {
        return ALERTAS;
    }

    public static Usuario getOperario() {
        return new Usuario(1, "Carlos", "Quispe", RolUsuario.OPERARIO);
    }

    public static Usuario getSupervisor() {
        return new Usuario(2, "Ana", "Torres", RolUsuario.SUPERVISOR);
    }
}
