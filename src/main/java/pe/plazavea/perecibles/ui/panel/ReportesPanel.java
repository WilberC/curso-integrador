package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;

public final class ReportesPanel extends JPanel {

    public ReportesPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

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
        filters.add(Buttons.secondary("Stock"));
        filters.add(Box.createVerticalStrut(Theme.SP_XS));
        filters.add(Buttons.secondary("Vencidos"));
        filters.add(Box.createVerticalStrut(Theme.SP_XS));
        filters.add(Buttons.primary("Exportar"));

        JTextArea preview = new JTextArea("""
                REPORTE DE PERECIBLES

                Lotes monitoreados: 6
                Proximos a vencer: 2
                Vencidos: 1

                Vista preliminar operativa para exportacion.
                """);
        preview.setEditable(false);
        preview.setFont(Fonts.mono(Font.PLAIN, 14f));
        preview.setBackground(Theme.SURFACE_CARD);
        preview.setForeground(Theme.BODY);
        preview.setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filters, new JScrollPane(preview));
        splitPane.setDividerLocation(240);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        add(splitPane, BorderLayout.CENTER);
    }
}
