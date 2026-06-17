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
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.service.ConfiguracionServicio;
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
    private final ConfiguracionServicio configuracionServicio;

    public DashboardPanel(InventarioServicio inventarioServicio, ConfiguracionServicio configuracionServicio) {
        this.inventarioServicio = inventarioServicio;
        this.configuracionServicio = configuracionServicio;
        setLayout(new BorderLayout(0, Theme.SP_LG));
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        gauges.setBackground(Theme.CANVAS);
        JTable table = TableFactory.loteTable(urgentModel);
        JScrollPane scrollPane = TableFactory.scrollPane(table);

        JPanel center = new JPanel(new BorderLayout(0, Theme.SP_LG));
        center.setBackground(Theme.CANVAS);
        center.add(gauges, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        add(buildToolbar(), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
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
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() {
                return new DashboardData(
                        inventarioServicio.consultarStock(),
                        configuracionServicio.obtenerConfiguracionActiva()
                );
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateGauges(data.lotes(), data.config());
                } catch (Exception exception) {
                    timestampLabel.setText("Error al actualizar: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private void updateGauges(List<Lote> lotes, ConfiguracionAlerta config) {
        int activeTotal = (int) lotes.stream().filter(lote -> lote.getEstado() != EstadoLote.RETIRADO).count();
        int vencidos = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO).count();
        int criticos = countInRange(lotes, 0, config.getDiasCriticos());
        int advertencias = countInRange(lotes, config.getDiasCriticos() + 1L, config.getDiasAdvertencia());
        int avisos = countInRange(lotes, config.getDiasAdvertencia() + 1L, config.getDiasAvisoAnticipado());
        int alertasPct = percent(criticos + advertencias + avisos + vencidos, activeTotal);
        int vencidosPct = percent(vencidos, activeTotal);
        GaugeState state = vencidos > 0 || criticos > 0
                ? GaugeState.DANGER
                : advertencias > 0 ? GaugeState.WARNING : GaugeState.SAFE;

        gauges.removeAll();
        gauges.add(new GaugeCard("Total lotes activos", activeTotal, Math.max(activeTotal, 1), trend("total", activeTotal), state));
        gauges.add(new GaugeCard("% En alerta", alertasPct, 100, trend("alertas", alertasPct), state));
        gauges.add(new GaugeCard("% Vencidos", vencidosPct, 100, trend("vencidos", vencidosPct), vencidos > 0 ? GaugeState.DANGER : GaugeState.SAFE));
        gauges.add(new GaugeCard("Avisos anticipados", avisos, Math.max(activeTotal, 1), trend("avisos", avisos), GaugeState.SAFE));
        snapshotAnterior.put("total", activeTotal);
        snapshotAnterior.put("alertas", alertasPct);
        snapshotAnterior.put("vencidos", vencidosPct);
        snapshotAnterior.put("avisos", avisos);
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

    private int countInRange(List<Lote> lotes, long minDays, long maxDays) {
        return (int) lotes.stream()
                .filter(lote -> lote.getEstado() != EstadoLote.RETIRADO)
                .filter(lote -> lote.getDiasParaVencer() >= minDays && lote.getDiasParaVencer() <= maxDays)
                .count();
    }

    private int percent(int value, int total) {
        return total > 0 ? (int) Math.round(value * 100.0 / total) : 0;
    }

    private int trend(String key, int value) {
        return value - snapshotAnterior.getOrDefault(key, value);
    }

    private record DashboardData(List<Lote> lotes, ConfiguracionAlerta config) {
    }
}
