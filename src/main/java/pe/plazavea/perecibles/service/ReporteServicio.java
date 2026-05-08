package pe.plazavea.perecibles.service;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.TipoReporte;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Merma;
import pe.plazavea.perecibles.model.Reporte;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.LoteRepository;
import pe.plazavea.perecibles.repository.MermaRepository;
import pe.plazavea.perecibles.repository.ReporteRepository;

@Service
public class ReporteServicio {

    private final ReporteRepository reporteRepository;
    private final LoteRepository loteRepository;
    private final MermaRepository mermaRepository;

    public ReporteServicio(
            ReporteRepository reporteRepository,
            LoteRepository loteRepository,
            MermaRepository mermaRepository
    ) {
        this.reporteRepository = reporteRepository;
        this.loteRepository = loteRepository;
        this.mermaRepository = mermaRepository;
    }

    @Transactional
    public Reporte generarReporteStock(LocalDate inicio, LocalDate fin, Usuario usuario) {
        return reporteRepository.save(buildReporte(TipoReporte.STOCK, inicio, fin, usuario));
    }

    @Transactional
    public Reporte generarReporteMermas(LocalDate inicio, LocalDate fin, Usuario usuario) {
        return reporteRepository.save(buildReporte(TipoReporte.MERMAS, inicio, fin, usuario));
    }

    @Transactional
    public Reporte generarReporteVencidos(LocalDate inicio, LocalDate fin, Usuario usuario) {
        return reporteRepository.save(buildReporte(TipoReporte.VENCIDOS, inicio, fin, usuario));
    }

    @Transactional
    public Reporte generarReporteProximosAVencer(LocalDate inicio, LocalDate fin, Usuario usuario) {
        return reporteRepository.save(buildReporte(TipoReporte.PROXIMOS_VENCER, inicio, fin, usuario));
    }

    public String resumen(Reporte reporte) {
        List<Lote> lotes = lotesPara(reporte.getTipo());
        long vencidos = lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO || lote.estaVencido()).count();
        long proximos = lotes.stream().filter(lote -> lote.estaProximoAVencer(7)).count();
        int stock = lotes.stream().mapToInt(Lote::getCantidadActual).sum();
        long mermas = mermasEnRango(reporte).size();
        String detail = switch (reporte.getTipo()) {
            case STOCK -> "Unidades en stock: " + stock;
            case VENCIDOS -> "Lotes vencidos: " + vencidos;
            case PROXIMOS_VENCER -> "Lotes proximos a vencer: " + proximos;
            case MERMAS -> "Mermas registradas: " + mermas;
        };
        return """
                REPORTE DE PERECIBLES

                Tipo: %s
                Desde: %s
                Hasta: %s
                Generado: %s

                Lotes monitoreados: %d
                %s
                """.formatted(
                reporte.getTipo(),
                reporte.getFechaInicio(),
                reporte.getFechaFin(),
                reporte.getFechaGeneracion().toLocalDate(),
                lotes.size(),
                detail
        );
    }

    public void exportarCSV(Reporte reporte) throws IOException {
        Path temp = Files.createTempFile("reporte_" + reporte.getTipo().name().toLowerCase() + "_", ".csv");
        Files.writeString(temp, buildCsvContent(reporte), StandardCharsets.UTF_8);
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(temp.toFile());
        }
    }

    private Reporte buildReporte(TipoReporte tipo, LocalDate inicio, LocalDate fin, Usuario usuario) {
        Reporte reporte = new Reporte();
        reporte.setTipo(tipo);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setFechaInicio(inicio);
        reporte.setFechaFin(fin);
        reporte.setUsuario(usuario);
        return reporte;
    }

    private String buildCsvContent(Reporte reporte) {
        if (reporte.getTipo() == TipoReporte.MERMAS) {
            StringBuilder csv = new StringBuilder("fecha,lote,producto,cantidad,motivo,usuario\n");
            for (Merma merma : mermasEnRango(reporte)) {
                csv.append(merma.getFechaRegistro()).append(',')
                        .append(value(merma.getLote().getNumeroLote())).append(',')
                        .append(value(merma.getLote().getProducto())).append(',')
                        .append(merma.getCantidad()).append(',')
                        .append(value(merma.getMotivo())).append(',')
                        .append(value(merma.getUsuario().getNombreCompleto())).append('\n');
            }
            return csv.toString();
        }

        StringBuilder csv = new StringBuilder("lote,producto,categoria,cantidad_actual,vencimiento,dias,estado\n");
        for (Lote lote : lotesPara(reporte.getTipo())) {
            csv.append(value(lote.getNumeroLote())).append(',')
                    .append(value(lote.getProducto())).append(',')
                    .append(value(lote.getCategoria())).append(',')
                    .append(lote.getCantidadActual()).append(',')
                    .append(lote.getFechaVencimiento()).append(',')
                    .append(lote.getDiasParaVencer()).append(',')
                    .append(lote.getEstado()).append('\n');
        }
        return csv.toString();
    }

    private List<Lote> lotesPara(TipoReporte tipo) {
        return switch (tipo) {
            case STOCK -> loteRepository.findByEstadoNot(EstadoLote.RETIRADO);
            case VENCIDOS -> loteRepository.findByEstado(EstadoLote.VENCIDO);
            case PROXIMOS_VENCER -> loteRepository.findProximosAVencer(LocalDate.now(), LocalDate.now().plusDays(7));
            case MERMAS -> loteRepository.findByEstadoNot(EstadoLote.RETIRADO);
        };
    }

    private List<Merma> mermasEnRango(Reporte reporte) {
        return mermaRepository.findByFechaRegistroBetween(
                reporte.getFechaInicio().atStartOfDay(),
                reporte.getFechaFin().plusDays(1).atStartOfDay()
        );
    }

    private String value(String text) {
        if (text == null) {
            return "";
        }
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
