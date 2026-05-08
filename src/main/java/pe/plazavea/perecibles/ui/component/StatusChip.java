package pe.plazavea.perecibles.ui.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class StatusChip extends JLabel {

    private final Color backgroundColor;

    public StatusChip(EstadoLote estado) {
        super(estado.name());
        setOpaque(false);
        setFont(Fonts.inter(Font.BOLD, 11f));
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        backgroundColor = switch (estado) {
            case DISPONIBLE -> {
                setForeground(Theme.SAFE);
                yield Theme.SAFE_TINT;
            }
            case PROXIMO_VENCER -> {
                setForeground(Theme.WARNING);
                yield Theme.WARNING_TINT;
            }
            case VENCIDO -> {
                setForeground(Theme.DANGER);
                yield Theme.DANGER_TINT;
            }
            case RETIRADO -> {
                setForeground(Theme.MUTED_STRONG);
                yield Theme.SURFACE_ELEVATED;
            }
        };
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(backgroundColor);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_SM * 2, Theme.RADIUS_SM * 2);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
