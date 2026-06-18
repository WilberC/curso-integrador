package pe.plazavea.perecibles.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateParserTest {

    @Test
    void interpretaDiasRelativosCortos() {
        assertEquals(LocalDate.now().plusDays(7), DateParser.parse("+7").orElseThrow());
        assertEquals(LocalDate.now().plusDays(3), DateParser.parse("3 dias").orElseThrow());
    }

    @Test
    void interpretaMesesRelativos() {
        assertEquals(LocalDate.now().plusMonths(2), DateParser.parse("2 meses").orElseThrow());
        assertEquals(LocalDate.now().plusMonths(2), DateParser.parse("+2 meses").orElseThrow());
        assertEquals(LocalDate.now().plusMonths(2), DateParser.parse("Hoy + 2 meses").orElseThrow());
    }
}
