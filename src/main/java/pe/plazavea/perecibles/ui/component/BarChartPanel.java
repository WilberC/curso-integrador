package pe.plazavea.perecibles.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class BarChartPanel extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final ChartCanvas canvas = new ChartCanvas();

    public BarChartPanel(String title) {
        setLayout(new BorderLayout(0, Theme.SP_SM));
        setBackground(Theme.SURFACE_SOFT);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_MD, Theme.SP_MD, Theme.SP_MD, Theme.SP_MD));
        titleLabel.setText(title);
        titleLabel.setFont(Fonts.inter(Font.BOLD, 13f));
        titleLabel.setForeground(Theme.INK);
        add(titleLabel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
    }

    public void setData(List<Entry> entries) {
        canvas.setEntries(entries);
    }

    public record Entry(String label, int value, Color color) {
    }

    private static final class ChartCanvas extends JPanel {
        private static final int BAR_HEIGHT = 18;
        private static final int BAR_GAP = 12;
        private static final int LABEL_WIDTH = 92;
        private static final int VALUE_WIDTH = 36;

        private List<Entry> entries = List.of();

        private ChartCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(320, 170));
        }

        private void setEntries(List<Entry> entries) {
            this.entries = new ArrayList<>(entries);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setFont(Fonts.inter(Font.PLAIN, 12f));
            if (entries.isEmpty()) {
                drawEmpty(graphics2D);
                graphics2D.dispose();
                return;
            }
            int max = entries.stream().mapToInt(Entry::value).max().orElse(1);
            int barAreaWidth = Math.max(1, getWidth() - LABEL_WIDTH - VALUE_WIDTH - Theme.SP_MD);
            int y = Theme.SP_XS;
            for (Entry entry : entries) {
                drawEntry(graphics2D, entry, max, barAreaWidth, y);
                y += BAR_HEIGHT + BAR_GAP;
            }
            graphics2D.dispose();
        }

        private void drawEntry(Graphics2D graphics2D, Entry entry, int max, int barAreaWidth, int y) {
            graphics2D.setColor(Theme.MUTED);
            graphics2D.drawString(entry.label(), Theme.SP_XS, y + 14);

            int x = LABEL_WIDTH;
            graphics2D.setColor(Theme.SURFACE_STRONG);
            graphics2D.fillRoundRect(x, y, barAreaWidth, BAR_HEIGHT, Theme.RADIUS_MD, Theme.RADIUS_MD);

            int width = entry.value() == 0 ? 0 : Math.max(3, (int) Math.round(barAreaWidth * (entry.value() / (double) max)));
            graphics2D.setColor(entry.color());
            graphics2D.fillRoundRect(x, y, width, BAR_HEIGHT, Theme.RADIUS_MD, Theme.RADIUS_MD);

            graphics2D.setColor(Theme.INK);
            graphics2D.setFont(Fonts.mono(Font.BOLD, 12f));
            graphics2D.drawString(String.valueOf(entry.value()), x + barAreaWidth + Theme.SP_XS, y + 14);
            graphics2D.setFont(Fonts.inter(Font.PLAIN, 12f));
        }

        private void drawEmpty(Graphics2D graphics2D) {
            graphics2D.setColor(Theme.MUTED);
            graphics2D.drawString("Sin datos para mostrar", Theme.SP_XS, Theme.SP_LG);
        }
    }
}
