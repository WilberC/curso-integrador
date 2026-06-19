package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import pe.plazavea.perecibles.ui.component.BarChartPanel;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.GaugeCard;
import pe.plazavea.perecibles.ui.component.GaugeState;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

@Component
@Lazy
public final class DashboardPanel extends JPanel {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color CRITICAL_COLOR = Color.decode("#b91c1c");
    private static final Color DANGER_COLOR = Color.decode("#ef4444");
    private static final Color WARNING_COLOR = Color.decode("#f59e0b");
    private static final Color NOTICE_COLOR = Color.decode("#eab308");
    private static final Color SAFE_COLOR = Color.decode("#16a34a");

    private final JPanel gauges = new JPanel(new GridLayout(1, 4, Theme.SP_MD, Theme.SP_MD));
    private final JPanel charts = new JPanel(new GridLayout(1, 2, Theme.SP_LG, 0));
    private final BarChartPanel expiryChart = new BarChartPanel("Distribución por vencimiento");
    private final BarChartPanel categoryRiskChart = new BarChartPanel("Riesgo por categoría");
    private final LoteTableModel urgentModel = new LoteTableModel(List.of(), false);
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
        charts.setBackground(Theme.CANVAS);
        charts.add(expiryChart);
        charts.add(categoryRiskChart);
        JTable table = TableFactory.loteTable(urgentModel);
        JScrollPane scrollPane = TableFactory.scrollPane(table);

        JLabel urgentTitle = new JLabel("Lotes más urgentes");
        urgentTitle.setFont(Fonts.inter(Font.BOLD, 13f));
        urgentTitle.setForeground(Theme.INK);
        urgentTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_XS, 0));

        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setBackground(Theme.CANVAS);
        tableSection.setPreferredSize(new Dimension(0, 280));
        tableSection.add(urgentTitle, BorderLayout.NORTH);
        tableSection.add(scrollPane, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.CANVAS);
        addCenterRow(center, gauges, 0, 0.0, Theme.SP_LG);
        addCenterRow(center, charts, 1, 0.0, Theme.SP_LG);
        addCenterRow(center, tableSection, 2, 1.0, 0);

        add(buildToolbar(), BorderLayout.NORTH);
        JScrollPane contentScroll = new JScrollPane(center);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(contentScroll, BorderLayout.CENTER);
        refreshDashboard();
        timer.setRepeats(true);
        timer.start();
    }

    private void addCenterRow(JPanel center, JPanel component, int row, double weightY, int bottomInset) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.weighty = weightY;
        constraints.fill = weightY > 0.0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        constraints.insets = new java.awt.Insets(0, 0, bottomInset, 0);
        center.add(component, constraints);
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
                        inventarioServicio.consultarInventario(),
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
        List<Lote> activos = lotes.stream()
                .filter(lote -> lote.getEstado() != EstadoLote.RETIRADO)
                .toList();
        int activeTotal = activos.size();
        int retirados = (int) lotes.stream().filter(lote -> lote.getEstado() == EstadoLote.RETIRADO).count();
        int vencidos = (int) activos.stream().filter(lote -> lote.getEstado() == EstadoLote.VENCIDO || lote.estaVencido()).count();
        int criticos = countInRange(activos, 0, config.getDiasCriticos());
        int advertencias = countInRange(activos, config.getDiasCriticos() + 1L, config.getDiasAdvertencia());
        int avisos = countInRange(activos, config.getDiasAdvertencia() + 1L, config.getDiasAvisoAnticipado());
        int alertasPct = percent(criticos + advertencias + avisos + vencidos, activeTotal);
        GaugeState state = vencidos > 0 || criticos > 0
                ? GaugeState.DANGER
                : advertencias > 0 ? GaugeState.WARNING : GaugeState.SAFE;

        gauges.removeAll();
        gauges.add(new GaugeCard("Total lotes activos", activeTotal, Math.max(activeTotal, 1), trend("total", activeTotal), state));
        gauges.add(new GaugeCard("% En alerta", alertasPct, 100, trend("alertas", alertasPct), state));
        gauges.add(new GaugeCard("Lotes vencidos", vencidos, Math.max(activeTotal, 1), trend("vencidos", vencidos), vencidos > 0 ? GaugeState.DANGER : GaugeState.SAFE));
        gauges.add(new GaugeCard("Lotes retirados", retirados, Math.max(lotes.size(), 1), trend("retirados", retirados), GaugeState.WARNING));
        snapshotAnterior.put("total", activeTotal);
        snapshotAnterior.put("alertas", alertasPct);
        snapshotAnterior.put("vencidos", vencidos);
        snapshotAnterior.put("retirados", retirados);
        expiryChart.setData(expiryDistribution(activos));
        categoryRiskChart.setData(categoryRisk(activos, config));
        urgentModel.setData(activos.stream()
                .sorted(Comparator.comparingLong(Lote::getDiasParaVencer))
                .limit(10)
                .toList());
        timestampLabel.setText("Actualizado " + TIMESTAMP.format(LocalDateTime.now()));
        gauges.revalidate();
        gauges.repaint();
        charts.revalidate();
        charts.repaint();
    }

    private List<BarChartPanel.Entry> expiryDistribution(List<Lote> lotes) {
        int vencidos = 0;
        int dosDias = 0;
        int sieteDias = 0;
        int quinceDias = 0;
        int saludables = 0;
        for (Lote lote : lotes) {
            long dias = lote.getDiasParaVencer();
            if (dias < 0 || lote.getEstado() == EstadoLote.VENCIDO) {
                vencidos++;
            } else if (dias <= 2) {
                dosDias++;
            } else if (dias <= 7) {
                sieteDias++;
            } else if (dias <= 15) {
                quinceDias++;
            } else {
                saludables++;
            }
        }
        return List.of(
                new BarChartPanel.Entry("Vencidos", vencidos, CRITICAL_COLOR),
                new BarChartPanel.Entry("0-2 días", dosDias, DANGER_COLOR),
                new BarChartPanel.Entry("3-7 días", sieteDias, WARNING_COLOR),
                new BarChartPanel.Entry("8-15 días", quinceDias, NOTICE_COLOR),
                new BarChartPanel.Entry("16+ días", saludables, SAFE_COLOR)
        );
    }

    private List<BarChartPanel.Entry> categoryRisk(List<Lote> lotes, ConfiguracionAlerta config) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Integer> worstSeverity = new LinkedHashMap<>();
        lotes.stream()
                .filter(lote -> lote.estaVencido() || lote.getDiasParaVencer() <= config.getDiasAvisoAnticipado())
                .forEach(lote -> {
                    String category = lote.getCategoria().isBlank() ? "Sin categoría" : lote.getCategoria();
                    counts.merge(category, 1, Integer::sum);
                    worstSeverity.merge(category, severity(lote, config), Math::min);
                });
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new BarChartPanel.Entry(entry.getKey(), entry.getValue(), colorForSeverity(worstSeverity.get(entry.getKey()))))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private int severity(Lote lote, ConfiguracionAlerta config) {
        long dias = lote.getDiasParaVencer();
        if (lote.getEstado() == EstadoLote.VENCIDO || dias < 0) {
            return 0;
        }
        if (dias <= config.getDiasCriticos()) {
            return 1;
        }
        if (dias <= config.getDiasAdvertencia()) {
            return 2;
        }
        return 3;
    }

    private Color colorForSeverity(Integer severity) {
        return switch (severity == null ? 3 : severity) {
            case 0 -> CRITICAL_COLOR;
            case 1 -> DANGER_COLOR;
            case 2 -> WARNING_COLOR;
            default -> NOTICE_COLOR;
        };
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
