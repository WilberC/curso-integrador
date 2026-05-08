package pe.plazavea.perecibles.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import pe.plazavea.perecibles.enums.EstadoLote;
import org.junit.jupiter.api.Test;

class LoteTest {

    @Test
    void calculaDiasParaVencerConFechasReales() {
        Lote lote = loteConVencimiento(LocalDate.now().plusDays(5));

        assertEquals(5, lote.getDiasParaVencer());
    }

    @Test
    void actualizaEstadoSegunFechaDeVencimiento() {
        Lote disponible = loteConVencimiento(LocalDate.now().plusDays(10));
        Lote proximo = loteConVencimiento(LocalDate.now().plusDays(7));
        Lote vencido = loteConVencimiento(LocalDate.now().minusDays(1));

        disponible.actualizarEstado(7, 2);
        proximo.actualizarEstado(7, 2);
        vencido.actualizarEstado(7, 2);

        assertEquals(EstadoLote.DISPONIBLE, disponible.getEstado());
        assertEquals(EstadoLote.PROXIMO_VENCER, proximo.getEstado());
        assertEquals(EstadoLote.VENCIDO, vencido.getEstado());
    }

    private Lote loteConVencimiento(LocalDate fechaVencimiento) {
        return new Lote(
                1,
                "L-TEST",
                "Producto Test",
                "Categoria Test",
                10,
                10,
                LocalDate.now(),
                fechaVencimiento,
                "Camara Test",
                EstadoLote.DISPONIBLE
        );
    }
}
