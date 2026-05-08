package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.dialog.NuevoLoteDialog;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

public final class InventarioPanel extends JPanel {

    public InventarioPanel(java.awt.Frame owner) {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        LoteTableModel model = new LoteTableModel(MockData.getLotes());
        add(buildActions(owner), BorderLayout.NORTH);
        add(TableFactory.scrollPane(TableFactory.loteTable(model)), BorderLayout.CENTER);
    }

    private JPanel buildActions(java.awt.Frame owner) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Theme.CANVAS_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

        var nuevo = Buttons.primary("Nuevo Lote");
        nuevo.addActionListener(event -> new NuevoLoteDialog(owner).setVisible(true));

        JComboBox<String> categoria = new JComboBox<>(new String[]{"Todas", "Lacteos", "Carnes", "Embutidos", "Panaderia"});
        JComboBox<String> estado = new JComboBox<>(new String[]{"Todos", "Disponible", "Proximo vencer", "Vencido"});
        JTextField search = new JTextField("Buscar producto o lote");
        search.setFont(Fonts.inter(java.awt.Font.PLAIN, 13f));
        search.setPreferredSize(new Dimension(220, 36));
        search.setMaximumSize(new Dimension(260, 36));

        panel.add(nuevo);
        panel.add(Box.createHorizontalStrut(Theme.SP_MD));
        panel.add(categoria);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(estado);
        panel.add(Box.createHorizontalGlue());
        panel.add(search);
        return panel;
    }
}
