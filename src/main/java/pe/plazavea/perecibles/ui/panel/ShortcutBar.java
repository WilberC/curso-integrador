package pe.plazavea.perecibles.ui.panel;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class ShortcutBar extends JPanel {

    public ShortcutBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Theme.SURFACE_ELEVATED);
        setPreferredSize(new Dimension(0, 28));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE_DARK),
                BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD)
        ));
        setHints(List.of(
                new ShortcutHint("Ctrl+G", "Dashboard"),
                new ShortcutHint("Ctrl+I", "Inventario"),
                new ShortcutHint("Ctrl+A", "Alertas"),
                new ShortcutHint("F5", "Refrescar"),
                new ShortcutHint("?", "Atajos")
        ));
    }

    public void setHints(List<ShortcutHint> hints) {
        removeAll();
        for (ShortcutHint hint : hints) {
            JLabel key = new JLabel("[" + hint.key() + "]");
            key.setFont(Fonts.mono(Font.PLAIN, 11f));
            key.setForeground(Theme.PRIMARY);

            JLabel desc = new JLabel(" " + hint.description());
            desc.setFont(Fonts.inter(Font.PLAIN, 11f));
            desc.setForeground(Theme.MUTED);

            add(key);
            add(desc);
            add(Box.createHorizontalStrut(Theme.SP_LG));
        }
        revalidate();
        repaint();
    }

    public void toggle() {
        setVisible(!isVisible());
    }

    public record ShortcutHint(String key, String description) {
    }
}
