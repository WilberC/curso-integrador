package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.service.InventarioServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.GaugeCard;
import pe.plazavea.perecibles.ui.component.GaugeState;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

@Component
@Lazy
public final class DashboardPanel extends JPanel {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JPanel gauges = new JPanel(new GridLayout(2, 2, Theme.SP_LG, Theme.SP_LG));
    private final LoteTableModel urgentModel = new LoteTableModel(List.of());
    private final JLabel timestampLabel = new JLabel();
    private final Timer timer = new Timer(60_000, event -> refreshDashboard());
    private final Map<String, Integer> snapshotAnterior = new HashMap<>();
    private final InventarioServicio inventarioServicio;

    public DashboardPanel(InventarioServicio inventarioServicio) {
        this.inventarioServicio = inventarioServicio;
        setLayout(new BorderLayout(0, Theme.SP_LG));
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        gauges.setBackground(Theme.CANVAS);
        JTable table = TableFactory.loteTable(urgentModel);
        JScrollPane scrollPane = TableFactory.scrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gauges, scrollPane);
        splitPane.setResizeWeight(0.68);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(8);

        add(buildToolbar(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        refreshDashboard();
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (timer != null) {
            if (visible) {
                timer.start();
                refreshDashboard();
            } else {
                timer.stop();
            }
        }
    }

    public void refreshDashboard() {
        new SwingWorker<List<Lote>, Void>() {
            @Override
            protected List<Lote> doInBackground() {
                return inventarioServicio.consultarStock();
            }

            @Override
            protected void done() {
                try {
                    updateGauges(get());
                } catch (Exception exception) {
                    timestampLabel.setText("Error al actualizar: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private void updateGauges(List<Lote> lotes) {
        int activeTotal = (int) lotes.stream().filter(lote -> lote.getEstado() != EstadoLote.RETIRADO).count();
        int proximos = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.PROXIMO_VENCER).count();
        int vencidos = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO).count();
        int proximosPct = percent(proximos, activeTotal);
        int vencidosPct = percent(vencidos, activeTotal);
        int mermasDia = 3;
        GaugeState state = vencidos > 0 ? GaugeState.DANGER : proximosPct >= 15 ? GaugeState.WARNING : GaugeState.SAFE;

        gauges.removeAll();
        gauges.add(new GaugeCard("Total lotes activos", activeTotal, Math.max(activeTotal, 1), trend("total", activeTotal), state));
        gauges.add(new GaugeCard("% Próximos a vencer", proximosPct, 100, trend("proximos", proximosPct), proximosPct >= 15 ? GaugeState.WARNING : GaugeState.SAFE));
        gauges.add(new GaugeCard("% Vencidos", vencidosPct, 100, trend("vencidos", vencidosPct), vencidos > 0 ? GaugeState.DANGER : GaugeState.SAFE));
        gauges.add(new GaugeCard("Mermas del día", mermasDia, 10, trend("mermas", mermasDia), GaugeState.WARNING));
        snapshotAnterior.put("total", activeTotal);
        snapshotAnterior.put("proximos", proximosPct);
        snapshotAnterior.put("vencidos", vencidosPct);
        snapshotAnterior.put("mermas", mermasDia);
        urgentModel.setData(lotes.stream()
                .filter(lote -> lote.getEstado() != EstadoLote.RETIRADO)
                .sorted(Comparator.comparingLong(Lote::getDiasParaVencer))
                .limit(10)
                .toList());
        timestampLabel.setText("Actualizado " + TIMESTAMP.format(LocalDateTime.now()));
        gauges.revalidate();
        gauges.repaint();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.CANVAS);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

        timestampLabel.setFont(Fonts.mono(Font.PLAIN, 12f));
        timestampLabel.setForeground(Theme.MUTED_STRONG);

        JButton refresh = Buttons.secondary("Refrescar");
        refresh.addActionListener(event -> refreshDashboard());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setBackground(Theme.CANVAS);
        actions.add(refresh);

        JLabel title = new JLabel("Riesgo operativo");
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.INK);
        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(timestampLabel, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        return toolbar;
    }

    private int percent(int value, int total) {
        return total > 0 ? (int) Math.round(value * 100.0 / total) : 0;
    }

    private int trend(String key, int value) {
        return value - snapshotAnterior.getOrDefault(key, value);
    }
}
