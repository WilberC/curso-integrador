package pe.plazavea.perecibles.service;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    public ReporteResultado resultado(Reporte reporte) {
        List<Lote> lotes = lotesPara(reporte);
        List<Merma> mermas = mermasEnRango(reporte);
        int unidades = lotes.stream().mapToInt(Lote::getCantidadActual).sum();
        long vencidos = lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO || lote.estaVencido()).count();
        long proximos = lotes.stream().filter(lote -> lote.estaProximoAVencer(7)).count();
        double unidadesMermadas = mermas.stream()
                .map(Merma::getCantidad)
                .filter(cantidad -> cantidad != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        return new ReporteResultado(lotes, mermas, unidades, vencidos, proximos, unidadesMermadas);
    }

    public String resumen(Reporte reporte) {
        ReporteResultado resultado = resultado(reporte);
        String detail = switch (reporte.getTipo()) {
            case STOCK -> "Unidades en stock: " + resultado.unidades();
            case VENCIDOS -> "Lotes vencidos: " + resultado.vencidos();
            case PROXIMOS_VENCER -> "Lotes proximos a vencer: " + resultado.proximos();
            case MERMAS -> "Mermas registradas: " + resultado.mermas().size();
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
                resultado.lotes().size(),
                detail
        );
    }

    public Path exportarCSV(Reporte reporte) throws IOException {
        Path temp = Files.createTempFile("reporte_" + reporte.getTipo().name().toLowerCase() + "_", ".csv");
        exportarCSV(reporte, temp);
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(temp.toFile());
        }
        return temp;
    }

    public Path exportarCSV(Reporte reporte, Path destino) throws IOException {
        Files.writeString(destino, buildCsvContent(reporte), StandardCharsets.UTF_8);
        return destino;
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
                Lote lote = merma.getLote();
                Usuario usuario = merma.getUsuario();
                csv.append(merma.getFechaRegistro()).append(',')
                        .append(value(lote == null ? "" : lote.getNumeroLote())).append(',')
                        .append(value(lote == null ? "" : lote.getProducto())).append(',')
                        .append(merma.getCantidad()).append(',')
                        .append(value(merma.getMotivo())).append(',')
                        .append(value(usuario == null ? "" : usuario.getNombreCompleto())).append('\n');
            }
            return csv.toString();
        }

        StringBuilder csv = new StringBuilder("lote,producto,categoria,cantidad_actual,vencimiento,dias,estado\n");
        for (Lote lote : lotesPara(reporte)) {
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

    private List<Lote> lotesPara(Reporte reporte) {
        return lotesBasePara(reporte.getTipo()).stream()
                .filter(lote -> coincideConRango(lote, reporte))
                .sorted(Comparator.comparing(Lote::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<Lote> lotesBasePara(TipoReporte tipo) {
        return switch (tipo) {
            case STOCK -> loteRepository.findByEstadoNot(EstadoLote.RETIRADO);
            case VENCIDOS -> loteRepository.findByEstado(EstadoLote.VENCIDO);
            case PROXIMOS_VENCER -> loteRepository.findByEstadoNot(EstadoLote.RETIRADO).stream()
                    .filter(lote -> lote.getEstado() != EstadoLote.VENCIDO)
                    .toList();
            case MERMAS -> loteRepository.findByEstadoNot(EstadoLote.RETIRADO);
        };
    }

    private boolean coincideConRango(Lote lote, Reporte reporte) {
        LocalDate fecha = switch (reporte.getTipo()) {
            case STOCK, MERMAS -> lote.getFechaIngreso();
            case VENCIDOS, PROXIMOS_VENCER -> lote.getFechaVencimiento();
        };
        if (fecha == null) {
            return true;
        }
        boolean despuesDeInicio = reporte.getFechaInicio() == null || !fecha.isBefore(reporte.getFechaInicio());
        boolean antesDeFin = reporte.getFechaFin() == null || !fecha.isAfter(reporte.getFechaFin());
        return despuesDeInicio && antesDeFin;
    }

    private List<Merma> mermasEnRango(Reporte reporte) {
        if (reporte.getFechaInicio() == null && reporte.getFechaFin() == null) {
            return mermaRepository.findAll();
        }
        if (reporte.getFechaInicio() == null) {
            return mermaRepository.findByFechaRegistroBetween(
                    LocalDateTime.MIN,
                    reporte.getFechaFin().plusDays(1).atStartOfDay()
            );
        }
        if (reporte.getFechaFin() == null) {
            return mermaRepository.findByFechaRegistroBetween(
                    reporte.getFechaInicio().atStartOfDay(),
                    LocalDateTime.now().plusDays(1)
            );
        }
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

    public record ReporteResultado(
            List<Lote> lotes,
            List<Merma> mermas,
            int unidades,
            long vencidos,
            long proximos,
            double unidadesMermadas
    ) {
    }
}
