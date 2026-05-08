package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
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
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.enums.TipoReporte;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.util.SessionManager;

public final class ReportesPanel extends JPanel {

    private final JComboBox<TipoReporte> tipoReporte = new JComboBox<>(TipoReporte.values());
    private final JFormattedTextField desde = new JFormattedTextField("01/05/2026");
    private final JFormattedTextField hasta = new JFormattedTextField("31/05/2026");
    private final JTextArea preview = new JTextArea();

    public ReportesPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
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
        if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getRol() != RolUsuario.SUPERVISOR) {
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
        filters.setBackground(Theme.SURFACE_CARD);
        filters.setPreferredSize(new Dimension(240, 0));
        filters.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JLabel title = new JLabel("Filtros");
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.ON_DARK);
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
        exportar.setEnabled(false);
        exportar.addActionListener(event -> JOptionPane.showMessageDialog(this, "Esta funcion estara disponible en la version completa"));
        filters.add(exportar);

        preview.setEditable(false);
        preview.setFont(Fonts.mono(Font.PLAIN, 14f));
        preview.setBackground(Theme.SURFACE_CARD);
        preview.setForeground(Theme.BODY);
        preview.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filters, new JScrollPane(preview));
        splitPane.setDividerLocation(240);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        return splitPane;
    }

    private JPanel accessDenied() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CANVAS_DARK);
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

    private void generateReport() {
        TipoReporte tipo = (TipoReporte) tipoReporte.getSelectedItem();
        long vencidos = MockData.getLotes().stream().filter(lote -> lote.estaVencido()).count();
        long proximos = MockData.getLotes().stream().filter(lote -> lote.estaProximoAVencer(7)).count();
        int stock = MockData.getLotes().stream().mapToInt(lote -> lote.getCantidadActual()).sum();
        String detail = switch (tipo) {
            case STOCK -> "Unidades en stock: " + stock;
            case VENCIDOS -> "Lotes vencidos: " + vencidos;
            case PROXIMOS_VENCER -> "Lotes proximos a vencer: " + proximos;
            case MERMAS -> "Mermas registradas hoy: 3";
        };
        preview.setText("""
                REPORTE DE PERECIBLES

                Tipo: %s
                Desde: %s
                Hasta: %s
                Generado: %s

                Lotes monitoreados: %d
                %s
                """.formatted(tipo, desde.getText(), hasta.getText(), LocalDate.now(), MockData.getLotes().size(), detail));
    }
}

