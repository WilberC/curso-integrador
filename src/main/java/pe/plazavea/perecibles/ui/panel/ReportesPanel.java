package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.springframework.context.annotation.Lazy;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.enums.TipoReporte;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Merma;
import pe.plazavea.perecibles.model.Reporte;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.service.ReporteServicio;
import pe.plazavea.perecibles.service.ReporteServicio.ReporteResultado;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.DatePickerField;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.component.StatusChip;
import pe.plazavea.perecibles.ui.table.TableFactory;
import pe.plazavea.perecibles.util.SessionManager;

@org.springframework.stereotype.Component
@Lazy
public final class ReportesPanel extends JPanel {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0.##");

    private final JComboBox<TipoReporte> tipoReporte = new JComboBox<>(TipoReporte.values());
    private final DatePickerField desde = new DatePickerField("Desde");
    private final DatePickerField hasta = new DatePickerField("Hasta");
    private final JTextField search = new JTextField();
    private final ReportTableModel tableModel = new ReportTableModel();
    private final JTable table = TableFactory.simpleTable(tableModel);
    private final TableRowSorter<ReportTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JLabel status = new JLabel("Listo");
    private final JLabel metricOneLabel = metricLabel("Lotes");
    private final JLabel metricOneValue = metricValue();
    private final JLabel metricTwoLabel = metricLabel("Unidades");
    private final JLabel metricTwoValue = metricValue();
    private final JLabel metricThreeLabel = metricLabel("Proximos 7d");
    private final JLabel metricThreeValue = metricValue();
    private final JLabel metricFourLabel = metricLabel("Vencidos");
    private final JLabel metricFourValue = metricValue();
    private final JButton generar = Buttons.primary("Generar");
    private final JButton exportarCsv = Buttons.secondary("Exportar CSV");
    private final JButton exportarXlsx = Buttons.secondary("Exportar XLSX");
    private final ReporteServicio reporteServicio;
    private Reporte currentReporte;
    private ReporteResultado currentResultado;

    public ReportesPanel(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        configureControls();
        rebuild();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            rebuild();
        }
    }

    private void configureControls() {
        configureField(desde);
        configureField(hasta);
        setRange(LocalDate.now().withDayOfMonth(1), LocalDate.now());
        tipoReporte.setRenderer(reportTypeRenderer());
        configureTable();

        generar.addActionListener(event -> generateReport());
        exportarCsv.addActionListener(event -> exportCurrentReport(ExportFormat.CSV));
        exportarXlsx.addActionListener(event -> exportCurrentReport(ExportFormat.XLSX));
        exportarCsv.setEnabled(false);
        exportarXlsx.setEnabled(false);

        search.setFont(Fonts.inter(Font.PLAIN, 13f));
        search.putClientProperty("JTextField.placeholderText", "Buscar producto, lote, estado o usuario");
        search.getDocument().addDocumentListener(new SimpleDocumentListener(this::applySearchFilter));
    }

    private void configureField(DatePickerField field) {
        field.setPreferredSize(new Dimension(104, 36));
        field.setMaximumSize(new Dimension(112, 36));
    }

    private void configureTable() {
        table.setRowSorter(sorter);
        table.setDefaultRenderer(Object.class, new ReportCellRenderer());
        table.setDefaultRenderer(Number.class, new ReportCellRenderer());
        table.setDefaultRenderer(LocalDate.class, new ReportCellRenderer());
        table.setDefaultRenderer(LocalDateTime.class, new ReportCellRenderer());
        table.setDefaultRenderer(EstadoLote.class, new StatusRenderer());
    }

    private ListCellRenderer<? super TipoReporte> reportTypeRenderer() {
        return (list, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : reportTypeLabel(value));
            label.setOpaque(true);
            label.setFont(Fonts.inter(Font.PLAIN, 13f));
            label.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XS));
            label.setBackground(selected ? Theme.TABLE_SELECTION : Theme.CANVAS);
            label.setForeground(selected ? Theme.INK : Theme.BODY);
            return label;
        };
    }

    private String reportTypeLabel(TipoReporte tipo) {
        return switch (tipo) {
            case STOCK -> "Inventario disponible";
            case VENCIDOS -> "Lotes vencidos";
            case PROXIMOS_VENCER -> "Proximos a vencer";
            case MERMAS -> "Mermas registradas";
        };
    }

    private void rebuild() {
        removeAll();
        Usuario currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRol() != RolUsuario.SUPERVISOR) {
            add(accessDenied(), BorderLayout.CENTER);
        } else {
            add(reportContent(), BorderLayout.CENTER);
            generateReport();
        }
        revalidate();
        repaint();
    }

    private JSplitPane reportContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterPanel(), previewPanel());
        splitPane.setDividerLocation(272);
        splitPane.setResizeWeight(0.25);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel filterPanel() {
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));
        filters.setBackground(Theme.SURFACE_SOFT);
        filters.setPreferredSize(new Dimension(272, 0));
        filters.setMinimumSize(new Dimension(248, 0));
        filters.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JLabel title = new JLabel("Reportes");
        title.setFont(Fonts.inter(Font.BOLD, 18f));
        title.setForeground(Theme.INK);
        filters.add(title);
        filters.add(supportingText("Genera informacion operativa con rango, tabla y CSV."));
        filters.add(Box.createVerticalStrut(Theme.SP_LG));

        addLabel(filters, "Tipo");
        tipoReporte.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        filters.add(tipoReporte);
        filters.add(Box.createVerticalStrut(Theme.SP_SM));

        addLabel(filters, "Rango de fechas");
        filters.add(dateRow());
        filters.add(Box.createVerticalStrut(Theme.SP_SM));
        filters.add(quickRangeRow());
        filters.add(Box.createVerticalStrut(Theme.SP_LG));

        generar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        exportarCsv.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        exportarXlsx.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        filters.add(generar);
        filters.add(Box.createVerticalStrut(Theme.SP_XS));
        filters.add(exportarXlsx);
        filters.add(Box.createVerticalStrut(Theme.SP_XS));
        filters.add(exportarCsv);
        filters.add(Box.createVerticalGlue());

        status.setFont(Fonts.inter(Font.PLAIN, 12f));
        status.setForeground(Theme.MUTED);
        filters.add(status);
        return filters;
    }

    private JPanel previewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, Theme.SP_MD));
        panel.setBackground(Theme.CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_LG, 0, 0));

        JPanel header = new JPanel(new BorderLayout(Theme.SP_MD, 0));
        header.setBackground(Theme.CANVAS);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(Theme.CANVAS);
        JLabel title = new JLabel("Vista previa");
        title.setFont(Fonts.inter(Font.BOLD, 20f));
        title.setForeground(Theme.INK);
        titleBlock.add(title);
        titleBlock.add(supportingText("Los resultados se actualizan al generar el reporte."));
        header.add(titleBlock, BorderLayout.WEST);

        search.setPreferredSize(new Dimension(320, 36));
        header.add(search, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(metricsAndTable(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel metricsAndTable() {
        JPanel panel = new JPanel(new BorderLayout(0, Theme.SP_MD));
        panel.setBackground(Theme.CANVAS);
        panel.add(metricRow(), BorderLayout.NORTH);

        JScrollPane scrollPane = TableFactory.scrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder()
        ));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel metricRow() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Theme.CANVAS);
        panel.add(metricCard(metricOneLabel, metricOneValue));
        panel.add(Box.createHorizontalStrut(Theme.SP_SM));
        panel.add(metricCard(metricTwoLabel, metricTwoValue));
        panel.add(Box.createHorizontalStrut(Theme.SP_SM));
        panel.add(metricCard(metricThreeLabel, metricThreeValue));
        panel.add(Box.createHorizontalStrut(Theme.SP_SM));
        panel.add(metricCard(metricFourLabel, metricFourValue));
        return panel;
    }

    private JPanel metricCard(JLabel label, JLabel value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_SOFT);
        card.setPreferredSize(new Dimension(148, 72));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_SM, Theme.SP_MD, Theme.SP_SM, Theme.SP_MD)
        ));
        card.add(label);
        card.add(Box.createVerticalStrut(Theme.SP_XXS));
        card.add(value);
        return card;
    }

    private JPanel dateRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.add(desde);
        row.add(Box.createHorizontalStrut(Theme.SP_XS));
        row.add(hasta);
        return row;
    }

    private JPanel quickRangeRow() {
        JPanel row = new JPanel(new GridLayout(2, 2, Theme.SP_XS, Theme.SP_XS));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.add(rangeButton("Este mes", LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        row.add(rangeButton("7 dias", LocalDate.now().minusDays(6), LocalDate.now()));
        row.add(rangeButton("30 dias", LocalDate.now().minusDays(29), LocalDate.now()));
        row.add(allTimeButton());
        return row;
    }

    private JButton rangeButton(String text, LocalDate inicio, LocalDate fin) {
        JButton button = Buttons.secondary(text);
        button.setFont(Fonts.inter(Font.BOLD, 11f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XXS, Theme.SP_XS, Theme.SP_XXS, Theme.SP_XS)
        ));
        button.addActionListener(event -> {
            setRange(inicio, fin);
            generateReport();
        });
        return button;
    }

    private JButton allTimeButton() {
        JButton button = Buttons.secondary("Todo");
        button.setFont(Fonts.inter(Font.BOLD, 11f));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XXS, Theme.SP_XS, Theme.SP_XXS, Theme.SP_XS)
        ));
        button.addActionListener(event -> {
            desde.clear();
            hasta.clear();
            generateReport();
        });
        return button;
    }

    private JPanel accessDenied() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CANVAS);
        JLabel title = new JLabel("Acceso denegado");
        title.setFont(Fonts.inter(Font.BOLD, 20f));
        title.setForeground(Theme.DANGER);
        JLabel detail = supportingText("Reportes esta disponible solo para usuarios supervisores.");
        panel.add(title, BorderLayout.NORTH);
        panel.add(detail, BorderLayout.CENTER);
        return panel;
    }

    private JLabel supportingText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 12f));
        label.setForeground(Theme.MUTED);
        return label;
    }

    private static JLabel metricLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 12f));
        label.setForeground(Theme.MUTED);
        return label;
    }

    private static JLabel metricValue() {
        JLabel label = new JLabel("0");
        label.setFont(Fonts.mono(Font.BOLD, 24f));
        label.setForeground(Theme.INK);
        return label;
    }

    private void addLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 13f));
        label.setForeground(Theme.MUTED);
        panel.add(label);
        panel.add(Box.createVerticalStrut(Theme.SP_XXS));
    }

    private void setRange(LocalDate inicio, LocalDate fin) {
        desde.setDate(inicio);
        hasta.setDate(fin);
    }

    public void generateReport() {
        Usuario currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRol() != RolUsuario.SUPERVISOR) {
            tableModel.clear();
            currentReporte = null;
            currentResultado = null;
            return;
        }

        DateRange range = selectedDateRange();
        if (range == null) {
            return;
        }

        TipoReporte tipo = (TipoReporte) tipoReporte.getSelectedItem();
        if (tipo == null) {
            return;
        }

        setBusy(true, "Generando reporte...");
        new SwingWorker<ReportViewData, Void>() {
            @Override
            protected ReportViewData doInBackground() {
                Reporte reporte = switch (tipo) {
                    case STOCK -> reporteServicio.generarReporteStock(range.inicio(), range.fin(), currentUser);
                    case VENCIDOS -> reporteServicio.generarReporteVencidos(range.inicio(), range.fin(), currentUser);
                    case PROXIMOS_VENCER -> reporteServicio.generarReporteProximosAVencer(range.inicio(), range.fin(), currentUser);
                    case MERMAS -> reporteServicio.generarReporteMermas(range.inicio(), range.fin(), currentUser);
                };
                return new ReportViewData(reporte, reporteServicio.resultado(reporte));
            }

            @Override
            protected void done() {
                try {
                    ReportViewData data = get();
                    currentReporte = data.reporte();
                    currentResultado = data.resultado();
                    renderResult(tipo, data.resultado());
                    status.setText("Generado " + DISPLAY_DATE_TIME.format(currentReporte.getFechaGeneracion()));
                    exportarCsv.setEnabled(true);
                    exportarXlsx.setEnabled(true);
                } catch (Exception exception) {
                    tableModel.clear();
                    currentReporte = null;
                    currentResultado = null;
                    exportarCsv.setEnabled(false);
                    exportarXlsx.setEnabled(false);
                    status.setText("No se pudo generar");
                    Dialogs.showMessage(
                            ReportesPanel.this,
                            "No se pudo generar el reporte: " + rootMessage(exception),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false, status.getText());
                }
            }
        }.execute();
    }

    private DateRange selectedDateRange() {
        if (desde.getDate().isEmpty() && hasta.getDate().isEmpty()) {
            return new DateRange(null, null);
        }
        if (desde.getDate().isEmpty() || hasta.getDate().isEmpty()) {
            Dialogs.showMessage(
                    this,
                    "Seleccione ambas fechas o use Todo para un reporte completo.",
                    "Rango incompleto",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        LocalDate inicio = desde.getDate().orElseThrow();
        LocalDate fin = hasta.getDate().orElseThrow();
        if (inicio.isAfter(fin)) {
            Dialogs.showMessage(
                    this,
                    "La fecha Desde no puede ser posterior a Hasta.",
                    "Rango invalido",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        return new DateRange(inicio, fin);
    }

    private void renderResult(TipoReporte tipo, ReporteResultado resultado) {
        updateMetrics(tipo, resultado);
        if (tipo == TipoReporte.MERMAS) {
            tableModel.setData(
                    List.of("Fecha", "Producto", "Lote", "Cantidad", "Motivo", "Usuario"),
                    rowsForMermas(resultado.mermas())
            );
        } else {
            tableModel.setData(
                    List.of("Producto", "Lote", "Categoria", "Stock", "Vencimiento", "Dias", "Estado", "Ubicacion"),
                    rowsForLotes(resultado.lotes())
            );
        }
        applySearchFilter();
    }

    private void updateMetrics(TipoReporte tipo, ReporteResultado resultado) {
        if (tipo == TipoReporte.MERMAS) {
            long lotesAfectados = resultado.mermas().stream()
                    .map(Merma::getLote)
                    .filter(Objects::nonNull)
                    .map(Lote::getId)
                    .distinct()
                    .count();
            metricOneLabel.setText("Mermas");
            metricOneValue.setText(NUMBER.format(resultado.mermas().size()));
            metricTwoLabel.setText("Unidades merma");
            metricTwoValue.setText(NUMBER.format(resultado.unidadesMermadas()));
            metricThreeLabel.setText("Lotes afectados");
            metricThreeValue.setText(NUMBER.format(lotesAfectados));
            metricFourLabel.setText("Dias");
            metricFourValue.setText(daysInRangeLabel());
            return;
        }

        metricOneLabel.setText("Lotes");
        metricOneValue.setText(NUMBER.format(resultado.lotes().size()));
        metricTwoLabel.setText("Unidades");
        metricTwoValue.setText(NUMBER.format(resultado.unidades()));
        metricThreeLabel.setText("Proximos 7d");
        metricThreeValue.setText(NUMBER.format(resultado.proximos()));
        metricFourLabel.setText("Vencidos");
        metricFourValue.setText(NUMBER.format(resultado.vencidos()));
    }

    private String daysInRangeLabel() {
        DateRange range = selectedDateRange();
        if (range == null || range.isAllTime()) {
            return "Todo";
        }
        return NUMBER.format(ChronoUnit.DAYS.between(range.inicio(), range.fin()) + 1) + " d";
    }

    private List<ReportRow> rowsForLotes(List<Lote> lotes) {
        return lotes.stream()
                .map(lote -> new ReportRow(List.of(
                        lote.getProducto(),
                        lote.getNumeroLote(),
                        lote.getCategoria(),
                        lote.getCantidadActual(),
                        lote.getFechaVencimiento(),
                        lote.getDiasParaVencer(),
                        lote.getEstado(),
                        nullToEmpty(lote.getUbicacion())
                )))
                .toList();
    }

    private List<ReportRow> rowsForMermas(List<Merma> mermas) {
        return mermas.stream()
                .map(merma -> new ReportRow(List.of(
                        merma.getFechaRegistro(),
                        merma.getLote() == null ? "" : merma.getLote().getProducto(),
                        merma.getLote() == null ? "" : merma.getLote().getNumeroLote(),
                        merma.getCantidad() == null ? 0 : merma.getCantidad(),
                        nullToEmpty(merma.getMotivo()),
                        merma.getUsuario() == null ? "" : merma.getUsuario().getNombreCompleto()
                )))
                .toList();
    }

    private void applySearchFilter() {
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        sorter.setRowFilter(query.isBlank() ? null : new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ReportTableModel, ? extends Integer> entry) {
                ReportRow row = tableModel.getRow(entry.getIdentifier());
                return row.searchable().contains(query);
            }
        });
    }

    private void exportCurrentReport(ExportFormat format) {
        if (currentReporte == null || currentResultado == null) {
            Dialogs.showMessage(this, "Primero genere un reporte.", "Sin reporte", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar reporte " + format.extension().toUpperCase(Locale.ROOT));
        chooser.setFileFilter(new FileNameExtensionFilter(format.description(), format.extension()));
        chooser.setSelectedFile(new File(defaultExportName(format.extension())));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path destino = ensureExtension(chooser.getSelectedFile(), format.extension()).toPath();
        setBusy(true, "Exportando " + format.extension().toUpperCase(Locale.ROOT) + "...");
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return switch (format) {
                    case CSV -> reporteServicio.exportarCSV(currentReporte, destino);
                    case XLSX -> reporteServicio.exportarXLSX(currentReporte, destino);
                };
            }

            @Override
            protected void done() {
                try {
                    Path exported = get();
                    status.setText(format.extension().toUpperCase(Locale.ROOT) + " guardado en " + exported.getFileName());
                    Dialogs.showMessage(
                            ReportesPanel.this,
                            "Reporte exportado en:\n" + exported,
                            format.extension().toUpperCase(Locale.ROOT) + " exportado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            ReportesPanel.this,
                            "No se pudo exportar el " + format.extension().toUpperCase(Locale.ROOT) + ": " + rootMessage(exception),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false, status.getText());
                }
            }
        }.execute();
    }

    private String defaultExportName(String extension) {
        return "reporte_" + currentReporte.getTipo().name().toLowerCase(Locale.ROOT) + "_"
                + currentReporte.getFechaGeneracion().toLocalDate() + "." + extension;
    }

    private File ensureExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        return path.toLowerCase(Locale.ROOT).endsWith("." + extension) ? file : new File(path + "." + extension);
    }

    private void setBusy(boolean busy, String message) {
        tipoReporte.setEnabled(!busy);
        desde.setEnabled(!busy);
        hasta.setEnabled(!busy);
        search.setEnabled(!busy);
        generar.setEnabled(!busy);
        exportarCsv.setEnabled(!busy && currentReporte != null);
        exportarXlsx.setEnabled(!busy && currentReporte != null);
        status.setText(message);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String rootMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record DateRange(LocalDate inicio, LocalDate fin) {
        private boolean isAllTime() {
            return inicio == null && fin == null;
        }
    }

    private record ReportViewData(Reporte reporte, ReporteResultado resultado) {
    }

    private enum ExportFormat {
        CSV("csv", "CSV"),
        XLSX("xlsx", "Excel XLSX");

        private final String extension;
        private final String description;

        ExportFormat(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        private String extension() {
            return extension;
        }

        private String description() {
            return description;
        }
    }

    private record ReportRow(List<Object> values, String searchable) {
        private ReportRow(List<Object> values) {
            this(values, values.stream()
                    .map(value -> value == null ? "" : value.toString().toLowerCase(Locale.ROOT))
                    .reduce("", (left, right) -> left + " " + right));
        }
    }

    private static final class ReportTableModel extends AbstractTableModel {
        private List<String> columns = List.of();
        private List<ReportRow> rows = List.of();

        private void setData(List<String> columns, List<ReportRow> rows) {
            this.columns = columns;
            this.rows = rows;
            fireTableStructureChanged();
        }

        private void clear() {
            this.columns = List.of();
            this.rows = List.of();
            fireTableStructureChanged();
        }

        private ReportRow getRow(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return rows.stream()
                    .map(row -> row.values().get(column))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(Object::getClass)
                    .orElse(Object.class);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex).values().get(columnIndex);
        }
    }

    private static final class ReportCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setBackground(selected ? Theme.TABLE_SELECTION : Theme.SURFACE_SOFT);
            label.setForeground(selected ? Theme.INK : Theme.BODY);
            label.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XS));
            label.setFont(Fonts.inter(Font.PLAIN, 13f));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            if (value instanceof LocalDate date) {
                label.setText(DISPLAY_DATE.format(date));
                label.setFont(Fonts.mono(Font.PLAIN, 13f));
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (value instanceof LocalDateTime dateTime) {
                label.setText(DISPLAY_DATE_TIME.format(dateTime));
                label.setFont(Fonts.mono(Font.PLAIN, 13f));
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (value instanceof Number number) {
                label.setText(NUMBER.format(number));
                label.setFont(Fonts.mono(Font.PLAIN, 13f));
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return label;
        }
    }

    private static final class StatusRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            if (value instanceof EstadoLote estado) {
                StatusChip chip = new StatusChip(estado);
                chip.setHorizontalAlignment(SwingConstants.CENTER);
                chip.setBackground(selected ? Theme.TABLE_SELECTION : Theme.SURFACE_SOFT);
                return chip;
            }
            return new JLabel(value == null ? "" : value.toString());
        }
    }

    private static final class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            action.run();
        }
    }
}
