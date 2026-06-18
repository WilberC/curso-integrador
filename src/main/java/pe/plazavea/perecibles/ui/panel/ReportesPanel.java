package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.enums.TipoReporte;
import pe.plazavea.perecibles.model.Reporte;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.service.ReporteServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.util.SessionManager;

@Component
@Lazy
public final class ReportesPanel extends JPanel {

    private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JComboBox<TipoReporte> tipoReporte = new JComboBox<>(TipoReporte.values());
    private final JFormattedTextField desde = new JFormattedTextField(INPUT_DATE.format(LocalDate.now().withDayOfMonth(1)));
    private final JFormattedTextField hasta = new JFormattedTextField(INPUT_DATE.format(LocalDate.now()));
    private final JTextArea preview = new JTextArea();
    private final ReporteServicio reporteServicio;
    private Reporte currentReporte;

    public ReportesPanel(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        rebuild();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            rebuild();
        }
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
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));
        filters.setBackground(Theme.SURFACE_SOFT);
        filters.setPreferredSize(new Dimension(240, 0));
        filters.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JLabel title = new JLabel("Filtros");
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.INK);
        filters.add(title);
        filters.add(Box.createVerticalStrut(Theme.SP_MD));
        addLabel(filters, "Tipo de Reporte");
        filters.add(tipoReporte);
        filters.add(Box.createVerticalStrut(Theme.SP_SM));
        addLabel(filters, "Desde");
        filters.add(desde);
        filters.add(Box.createVerticalStrut(Theme.SP_SM));
        addLabel(filters, "Hasta");
        filters.add(hasta);
        filters.add(Box.createVerticalStrut(Theme.SP_LG));
        var generar = Buttons.primary("Generar Reporte");
        generar.addActionListener(event -> generateReport());
        filters.add(generar);
        filters.add(Box.createVerticalStrut(Theme.SP_XS));
        var exportar = Buttons.secondary("Exportar CSV");
        exportar.addActionListener(event -> exportCurrentReport());
        filters.add(exportar);

        preview.setEditable(false);
        preview.setFont(Fonts.mono(Font.PLAIN, 14f));
        preview.setBackground(Theme.SURFACE_SOFT);
        preview.setForeground(Theme.BODY);
        preview.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filters, new JScrollPane(preview));
        splitPane.setDividerLocation(240);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel accessDenied() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CANVAS);
        JLabel label = new JLabel("Acceso denegado: reportes solo esta disponible para Supervisores.");
        label.setFont(Fonts.inter(Font.BOLD, 15f));
        label.setForeground(Theme.DANGER);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private void addLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 13f));
        label.setForeground(Theme.MUTED);
        panel.add(label);
        panel.add(Box.createVerticalStrut(Theme.SP_XXS));
    }

    public void generateReport() {
        Usuario currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRol() != RolUsuario.SUPERVISOR) {
            preview.setText("");
            return;
        }

        TipoReporte tipo = (TipoReporte) tipoReporte.getSelectedItem();
        LocalDate inicio = LocalDate.parse(desde.getText(), INPUT_DATE);
        LocalDate fin = LocalDate.parse(hasta.getText(), INPUT_DATE);
        new SwingWorker<Reporte, Void>() {
            @Override
            protected Reporte doInBackground() {
                return switch (tipo) {
                    case STOCK -> reporteServicio.generarReporteStock(inicio, fin, currentUser);
                    case VENCIDOS -> reporteServicio.generarReporteVencidos(inicio, fin, currentUser);
                    case PROXIMOS_VENCER -> reporteServicio.generarReporteProximosAVencer(inicio, fin, currentUser);
                    case MERMAS -> reporteServicio.generarReporteMermas(inicio, fin, currentUser);
                };
            }

            @Override
            protected void done() {
                try {
                    currentReporte = get();
                    preview.setText(reporteServicio.resumen(currentReporte));
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            ReportesPanel.this,
                            "No se pudo generar el reporte: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void exportCurrentReport() {
        if (currentReporte == null) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                reporteServicio.exportarCSV(currentReporte);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            ReportesPanel.this,
                            "No se pudo exportar el CSV: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}
