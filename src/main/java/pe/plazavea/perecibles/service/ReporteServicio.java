package pe.plazavea.perecibles.service;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    public Path exportarXLSX(Reporte reporte, Path destino) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); OutputStream output = Files.newOutputStream(destino)) {
            buildWorkbook(workbook, reporte, resultado(reporte));
            workbook.write(output);
        }
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

    private void buildWorkbook(Workbook workbook, Reporte reporte, ReporteResultado resultado) {
        Sheet sheet = workbook.createSheet("Reporte");
        WorkbookStyles styles = WorkbookStyles.create(workbook);

        int maxColumn = reporte.getTipo() == TipoReporte.MERMAS ? 5 : 6;
        int rowIndex = writeTitle(sheet, styles, reporte, maxColumn);
        rowIndex = writeMetadata(sheet, styles, reporte, rowIndex, maxColumn);
        rowIndex = writeMetrics(sheet, styles, reporte, resultado, rowIndex);
        rowIndex++;

        int headerRowIndex = rowIndex;
        if (reporte.getTipo() == TipoReporte.MERMAS) {
            writeHeader(sheet.createRow(rowIndex++), styles, "Fecha", "Lote", "Producto", "Cantidad", "Motivo", "Usuario");
            for (Merma merma : resultado.mermas()) {
                writeMermaRow(sheet.createRow(rowIndex++), styles, merma);
            }
        } else {
            writeHeader(sheet.createRow(rowIndex++), styles, "Lote", "Producto", "Categoria", "Cantidad actual", "Vencimiento", "Dias", "Estado");
            for (Lote lote : resultado.lotes()) {
                writeLoteRow(sheet.createRow(rowIndex++), styles, lote);
            }
        }

        if (rowIndex > headerRowIndex + 1) {
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, rowIndex - 1, 0, maxColumn));
        }
        sheet.createFreezePane(0, headerRowIndex + 1);
        for (int column = 0; column <= maxColumn; column++) {
            sheet.autoSizeColumn(column);
            sheet.setColumnWidth(column, Math.min(Math.max(sheet.getColumnWidth(column), 14 * 256), 34 * 256));
        }
    }

    private int writeTitle(Sheet sheet, WorkbookStyles styles, Reporte reporte, int maxColumn) {
        Row title = sheet.createRow(0);
        title.setHeightInPoints(28f);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Reporte de perecibles - " + tipoReporteLabel(reporte.getTipo()));
        titleCell.setCellStyle(styles.title());
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, maxColumn));

        Row subtitle = sheet.createRow(1);
        Cell subtitleCell = subtitle.createCell(0);
        subtitleCell.setCellValue("Rango: " + rangoLabel(reporte) + " | Generado: " + DATE_TIME.format(reporte.getFechaGeneracion()));
        subtitleCell.setCellStyle(styles.subtitle());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, maxColumn));
        return 3;
    }

    private int writeMetadata(Sheet sheet, WorkbookStyles styles, Reporte reporte, int rowIndex, int maxColumn) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Usuario");
        row.getCell(0).setCellStyle(styles.metaLabel());
        row.createCell(1).setCellValue(reporte.getUsuario() == null ? "" : reporte.getUsuario().getNombreCompleto());
        row.getCell(1).setCellStyle(styles.metaValue());
        row.createCell(3).setCellValue("Tipo");
        row.getCell(3).setCellStyle(styles.metaLabel());
        row.createCell(4).setCellValue(tipoReporteLabel(reporte.getTipo()));
        row.getCell(4).setCellStyle(styles.metaValue());
        if (maxColumn > 4) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 4, maxColumn));
        }
        return rowIndex + 1;
    }

    private int writeMetrics(Sheet sheet, WorkbookStyles styles, Reporte reporte, ReporteResultado resultado, int rowIndex) {
        Row labels = sheet.createRow(rowIndex++);
        Row values = sheet.createRow(rowIndex++);
        String[] metricLabels;
        String[] metricValues;
        if (reporte.getTipo() == TipoReporte.MERMAS) {
            long lotesAfectados = resultado.mermas().stream()
                    .map(Merma::getLote)
                    .filter(lote -> lote != null)
                    .map(Lote::getId)
                    .distinct()
                    .count();
            metricLabels = new String[]{"Mermas", "Unidades merma", "Lotes afectados", "Periodo"};
            metricValues = new String[]{
                String.valueOf(resultado.mermas().size()),
                String.valueOf(resultado.unidadesMermadas()),
                String.valueOf(lotesAfectados),
                rangoLabel(reporte)
            };
        } else {
            metricLabels = new String[]{"Lotes", "Unidades", "Proximos 7d", "Vencidos"};
            metricValues = new String[]{
                String.valueOf(resultado.lotes().size()),
                String.valueOf(resultado.unidades()),
                String.valueOf(resultado.proximos()),
                String.valueOf(resultado.vencidos())
            };
        }

        for (int index = 0; index < metricLabels.length; index++) {
            int column = index * 2;
            labels.createCell(column).setCellValue(metricLabels[index]);
            labels.getCell(column).setCellStyle(styles.metricLabel());
            values.createCell(column).setCellValue(metricValues[index]);
            values.getCell(column).setCellStyle(styles.metricValue());
        }
        return rowIndex;
    }

    private void writeHeader(Row row, WorkbookStyles styles, String... headers) {
        row.setHeightInPoints(22f);
        for (int index = 0; index < headers.length; index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(styles.header());
        }
    }

    private void writeLoteRow(Row row, WorkbookStyles styles, Lote lote) {
        writeText(row, 0, lote.getNumeroLote(), styles.text());
        writeText(row, 1, lote.getProducto(), styles.text());
        writeText(row, 2, lote.getCategoria(), styles.text());
        writeNumber(row, 3, lote.getCantidadActual(), styles.number());
        writeDate(row, 4, lote.getFechaVencimiento(), styles.date());
        writeNumber(row, 5, lote.getDiasParaVencer(), styles.number());
        writeText(row, 6, lote.getEstado() == null ? "" : lote.getEstado().name(), styles.status(lote.getEstado()));
    }

    private void writeMermaRow(Row row, WorkbookStyles styles, Merma merma) {
        Lote lote = merma.getLote();
        Usuario usuario = merma.getUsuario();
        writeDateTime(row, 0, merma.getFechaRegistro(), styles.dateTime());
        writeText(row, 1, lote == null ? "" : lote.getNumeroLote(), styles.text());
        writeText(row, 2, lote == null ? "" : lote.getProducto(), styles.text());
        writeNumber(row, 3, merma.getCantidad() == null ? 0 : merma.getCantidad(), styles.number());
        writeText(row, 4, merma.getMotivo(), styles.text());
        writeText(row, 5, usuario == null ? "" : usuario.getNombreCompleto(), styles.text());
    }

    private void writeText(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void writeNumber(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeDate(Row row, int column, LocalDate value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private void writeDateTime(Row row, int column, LocalDateTime value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private String tipoReporteLabel(TipoReporte tipo) {
        return switch (tipo) {
            case STOCK -> "Inventario disponible";
            case VENCIDOS -> "Lotes vencidos";
            case PROXIMOS_VENCER -> "Proximos a vencer";
            case MERMAS -> "Mermas registradas";
        };
    }

    private String rangoLabel(Reporte reporte) {
        if (reporte.getFechaInicio() == null && reporte.getFechaFin() == null) {
            return "Todos los tiempos";
        }
        if (reporte.getFechaInicio() == null) {
            return "Hasta " + DATE.format(reporte.getFechaFin());
        }
        if (reporte.getFechaFin() == null) {
            return "Desde " + DATE.format(reporte.getFechaInicio());
        }
        return DATE.format(reporte.getFechaInicio()) + " - " + DATE.format(reporte.getFechaFin());
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

    private record WorkbookStyles(
            CellStyle title,
            CellStyle subtitle,
            CellStyle metaLabel,
            CellStyle metaValue,
            CellStyle metricLabel,
            CellStyle metricValue,
            CellStyle header,
            CellStyle text,
            CellStyle number,
            CellStyle date,
            CellStyle dateTime,
            CellStyle safeStatus,
            CellStyle warningStatus,
            CellStyle dangerStatus,
            CellStyle mutedStatus
    ) {

        private static WorkbookStyles create(Workbook workbook) {
            CellStyle title = workbook.createCellStyle();
            title.setFont(font(workbook, IndexedColors.BLACK, true, 18));
            title.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle subtitle = workbook.createCellStyle();
            subtitle.setFont(font(workbook, IndexedColors.GREY_50_PERCENT, false, 10));

            CellStyle metaLabel = bordered(workbook);
            metaLabel.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            metaLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            metaLabel.setFont(font(workbook, IndexedColors.BLACK, true, 10));

            CellStyle metaValue = bordered(workbook);
            metaValue.setFont(font(workbook, IndexedColors.BLACK, false, 10));

            CellStyle metricLabel = bordered(workbook);
            metricLabel.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            metricLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            metricLabel.setFont(font(workbook, IndexedColors.GREY_80_PERCENT, true, 10));

            CellStyle metricValue = bordered(workbook);
            metricValue.setFont(font(workbook, IndexedColors.BLACK, true, 14));

            CellStyle header = bordered(workbook);
            header.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setFont(font(workbook, IndexedColors.WHITE, true, 10));

            CellStyle text = bordered(workbook);
            text.setFont(font(workbook, IndexedColors.BLACK, false, 10));

            CellStyle number = bordered(workbook);
            number.setAlignment(HorizontalAlignment.RIGHT);
            number.setFont(font(workbook, IndexedColors.BLACK, false, 10));
            number.setDataFormat(workbook.createDataFormat().getFormat("#,##0.##"));

            CellStyle date = bordered(workbook);
            date.setAlignment(HorizontalAlignment.RIGHT);
            date.setFont(font(workbook, IndexedColors.BLACK, false, 10));
            date.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle dateTime = bordered(workbook);
            dateTime.setAlignment(HorizontalAlignment.RIGHT);
            dateTime.setFont(font(workbook, IndexedColors.BLACK, false, 10));
            dateTime.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            CellStyle safeStatus = status(workbook, IndexedColors.LIGHT_GREEN, IndexedColors.GREEN);
            CellStyle warningStatus = status(workbook, IndexedColors.LIGHT_YELLOW, IndexedColors.ORANGE);
            CellStyle dangerStatus = status(workbook, IndexedColors.ROSE, IndexedColors.RED);
            CellStyle mutedStatus = status(workbook, IndexedColors.GREY_25_PERCENT, IndexedColors.GREY_80_PERCENT);

            return new WorkbookStyles(
                    title,
                    subtitle,
                    metaLabel,
                    metaValue,
                    metricLabel,
                    metricValue,
                    header,
                    text,
                    number,
                    date,
                    dateTime,
                    safeStatus,
                    warningStatus,
                    dangerStatus,
                    mutedStatus
            );
        }

        private CellStyle status(EstadoLote estado) {
            return switch (estado) {
                case DISPONIBLE -> safeStatus;
                case PROXIMO_VENCER -> warningStatus;
                case VENCIDO -> dangerStatus;
                case RETIRADO -> mutedStatus;
                case null -> mutedStatus;
            };
        }

        private static CellStyle bordered(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            return style;
        }

        private static CellStyle status(Workbook workbook, IndexedColors background, IndexedColors foreground) {
            CellStyle style = bordered(workbook);
            style.setFillForegroundColor(background.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font(workbook, foreground, true, 10));
            return style;
        }

        private static Font font(Workbook workbook, IndexedColors color, boolean bold, int height) {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) height);
            font.setColor(color.getIndex());
            font.setBold(bold);
            return font;
        }
    }
}
