package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.model.Alerta;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.table.AlertaTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

public final class AlertasPanel extends JPanel {

    private final AlertaTableModel model = new AlertaTableModel(List.of());
    private final JTable table = TableFactory.alertaTable(model);
    private final JLabel countBadge = new JLabel();
    private final JCheckBox showAll = new JCheckBox("Mostrar todas");

    public AlertasPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        add(buildToolbar(), BorderLayout.NORTH);
        add(TableFactory.scrollPane(table), BorderLayout.CENTER);
        registerShortcuts();
        refreshAlerts();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.CANVAS_DARK);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

        JLabel title = new JLabel("Alertas Pendientes");
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.ON_DARK);
        countBadge.setFont(Fonts.mono(Font.BOLD, 12f));
        countBadge.setForeground(Theme.WARNING);
        showAll.setFont(Fonts.inter(Font.PLAIN, 13f));
        showAll.setForeground(Theme.BODY);
        showAll.setBackground(Theme.CANVAS_DARK);
        showAll.addActionListener(event -> refreshAlerts());

        JPanel left = new JPanel();
        left.setBackground(Theme.CANVAS_DARK);
        left.add(title);
        left.add(countBadge);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(showAll, BorderLayout.EAST);
        return toolbar;
    }

    private void refreshAlerts() {
        List<Alerta> alertas = MockData.getAlertas().stream()
                .filter(alerta -> showAll.isSelected() || alerta.getEstado() == EstadoAlerta.PENDIENTE)
                .toList();
        model.setData(alertas);
        long pendientes = MockData.getAlertas().stream().filter(alerta -> alerta.getEstado() == EstadoAlerta.PENDIENTE).count();
        countBadge.setText("[" + pendientes + "]");
    }

    private void registerShortcuts() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "atender");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "ignorar");
        getActionMap().put("atender", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                updateSelected(EstadoAlerta.ATENDIDA);
            }
        });
        getActionMap().put("ignorar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                updateSelected(EstadoAlerta.IGNORADA);
            }
        });
    }

    private void updateSelected(EstadoAlerta estado) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return;
        }
        model.getAlertaAt(table.convertRowIndexToModel(selected)).setEstado(estado);
        refreshAlerts();
    }
}

