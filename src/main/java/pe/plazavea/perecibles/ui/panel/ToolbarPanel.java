package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class ToolbarPanel extends JPanel {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final JLabel title = new JLabel();
    private final JLabel timestamp = new JLabel();

    public ToolbarPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 40));
        setBackground(Theme.SURFACE_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE_DARK),
                BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD)
        ));

        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.ON_DARK);
        timestamp.setFont(Fonts.mono(Font.PLAIN, 12f));
        timestamp.setForeground(Theme.MUTED_STRONG);

        add(title, BorderLayout.WEST);
        add(timestamp, BorderLayout.EAST);
    }

    public void setScreenTitle(String screenTitle) {
        title.setText(screenTitle);
        timestamp.setText(FORMAT.format(LocalDateTime.now()));
    }
}
