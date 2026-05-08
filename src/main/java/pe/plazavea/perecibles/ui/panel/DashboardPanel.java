package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.GaugeCard;
import pe.plazavea.perecibles.ui.component.GaugeState;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

public final class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(0, Theme.SP_LG));
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        List<Lote> lotes = MockData.getLotes();
        int total = lotes.size();
        int disponibles = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.DISPONIBLE).count();
        int proximos = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.PROXIMO_VENCER).count();
        int vencidos = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO).count();
        int unidades = lotes.stream().mapToInt(Lote::getCantidadActual).sum();

        JPanel gauges = new JPanel(new GridLayout(2, 2, Theme.SP_LG, Theme.SP_LG));
        gauges.setBackground(Theme.CANVAS_DARK);
        gauges.add(new GaugeCard("Lotes disponibles", disponibles, total, 2, GaugeState.SAFE));
        gauges.add(new GaugeCard("Proximos a vencer", proximos, total, 1, proximos > 0 ? GaugeState.WARNING : GaugeState.SAFE));
        gauges.add(new GaugeCard("Vencidos", vencidos, total, 0, vencidos > 0 ? GaugeState.DANGER : GaugeState.SAFE));
        gauges.add(new GaugeCard("Unidades en stock", unidades, unidades, -8, GaugeState.SAFE));

        List<Lote> urgentes = lotes.stream()
                .sorted(Comparator.comparingLong(Lote::getDiasParaVencer))
                .limit(10)
                .toList();
        JTable table = TableFactory.loteTable(new LoteTableModel(urgentes));

        add(gauges, BorderLayout.CENTER);
        add(TableFactory.scrollPane(table), BorderLayout.SOUTH);
    }
}
