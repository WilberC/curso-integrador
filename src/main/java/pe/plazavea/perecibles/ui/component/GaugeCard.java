package pe.plazavea.perecibles.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class GaugeCard extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel trendLabel = new JLabel();

    public GaugeCard(String title, int value, int total, int trend, GaugeState state) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SURFACE_CARD);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        titleLabel.setFont(Fonts.inter(Font.BOLD, 14f));
        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(Fonts.mono(Font.BOLD, 32f));

        progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(Theme.SURFACE_ELEVATED);

        trendLabel.setFont(Fonts.inter(Font.PLAIN, 11f));
        trendLabel.setForeground(Theme.MUTED);

        add(titleLabel);
        add(Box.createVerticalStrut(Theme.SP_SM));
        add(valueLabel);
        add(Box.createVerticalStrut(Theme.SP_SM));
        add(progressBar);
        add(Box.createVerticalStrut(Theme.SP_XS));
        add(trendLabel);

        setData(title, value, total, trend);
        applyState(state);
    }

    public void setData(String title, int value, int total, int trend) {
        titleLabel.setText(title);
        valueLabel.setText(String.valueOf(value));
        progressBar.setValue(total > 0 ? (int) ((value * 100.0) / total) : 0);
        trendLabel.setText(trend > 0 ? "↑ +%d".formatted(trend) : trend < 0 ? "↓ %d".formatted(trend) : "— sin cambio");
    }

    public void applyState(GaugeState state) {
        Color color = switch (state) {
            case DANGER -> Theme.DANGER;
            case WARNING -> Theme.WARNING;
            case SAFE -> Theme.SAFE;
        };
        valueLabel.setForeground(color);
        progressBar.setForeground(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(getBackground());
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_XL * 2, Theme.RADIUS_XL * 2);
        graphics2D.dispose();
    }
}
