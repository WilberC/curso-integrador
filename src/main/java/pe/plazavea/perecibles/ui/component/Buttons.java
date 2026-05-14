package pe.plazavea.perecibles.ui.component;

import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class Buttons {

    private Buttons() {
    }

    public static JButton primary(String text) {
        JButton button = base(text);
        button.setBackground(Theme.PRIMARY);
        button.setForeground(Theme.ON_PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_MD, Theme.SP_XS, Theme.SP_MD));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    public static JButton secondary(String text) {
        JButton button = base(text);
        button.setBackground(Theme.CANVAS);
        button.setForeground(Theme.INK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_MD, Theme.SP_XS, Theme.SP_MD)
        ));
        return button;
    }

    public static JButton danger(String text) {
        JButton button = base(text);
        button.setBackground(Theme.DANGER_TINT);
        button.setForeground(Theme.DANGER);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.DANGER, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_MD, Theme.SP_XS, Theme.SP_MD)
        ));
        return button;
    }

    private static JButton base(String text) {
        JButton button = new JButton(text);
        button.setFont(Fonts.inter(Font.BOLD, 13f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setFocusPainted(false);
        return button;
    }
}
