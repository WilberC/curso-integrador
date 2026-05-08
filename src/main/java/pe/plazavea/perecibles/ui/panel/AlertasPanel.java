package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.table.AlertaTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

public final class AlertasPanel extends JPanel {

    public AlertasPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        add(TableFactory.scrollPane(TableFactory.alertaTable(new AlertaTableModel(MockData.getAlertas()))), BorderLayout.CENTER);
    }
}
