package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.Navigator;
import pe.plazavea.perecibles.ui.component.Buttons;

public final class LoginPanel extends JPanel {

    public LoginPanel(Navigator navigator) {
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.CANVAS_DARK);
        center.add(buildCard(navigator), new GridBagConstraints());

        add(center, BorderLayout.CENTER);
        add(new ShortcutBar(), BorderLayout.SOUTH);
    }

    private JPanel buildCard(Navigator navigator) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_CARD);
        card.setPreferredSize(new Dimension(400, 360));
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XL, Theme.SP_XL, Theme.SP_XL, Theme.SP_XL));

        JLabel wordmark = new JLabel("Plaza Vea");
        wordmark.setFont(Fonts.inter(Font.BOLD, 20f));
        wordmark.setForeground(Theme.PRIMARY);

        JLabel title = new JLabel("Control de Perecibles");
        title.setFont(Fonts.inter(Font.BOLD, 20f));
        title.setForeground(Theme.ON_DARK);

        JLabel user = new JLabel("%s %s - %s".formatted(
                MockData.getSupervisor().getNombre(),
                MockData.getSupervisor().getApellido(),
                MockData.getSupervisor().getRol()
        ));
        user.setFont(Fonts.inter(Font.PLAIN, 11f));
        user.setForeground(Theme.MUTED);

        card.add(wordmark);
        card.add(Box.createVerticalStrut(Theme.SP_XXS));
        card.add(title);
        card.add(Box.createVerticalStrut(Theme.SP_XS));
        card.add(user);
        card.add(Box.createVerticalStrut(Theme.SP_LG));

        card.add(field("Usuario", new JTextField("supervisor@plazavea.pe")));
        card.add(Box.createVerticalStrut(Theme.SP_SM));
        card.add(field("Contrasena", new JPasswordField("demo")));
        card.add(Box.createVerticalStrut(Theme.SP_LG));

        var login = Buttons.primary("Iniciar Sesion");
        login.addActionListener(event -> navigator.show("dashboard"));
        login.setAlignmentX(LEFT_ALIGNMENT);
        card.add(login);

        return card;
    }

    private JPanel field(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(0, Theme.SP_XS));
        row.setBackground(Theme.SURFACE_CARD);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.inter(Font.PLAIN, 13f));
        label.setForeground(Theme.MUTED);

        field.setFont(Fonts.inter(Font.PLAIN, 13f));
        field.setPreferredSize(new Dimension(0, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE_DARK, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_SM, Theme.SP_XS, Theme.SP_SM)
        ));

        row.add(label, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private static final class ShortcutBar extends JPanel {
        private ShortcutBar() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBackground(Theme.SURFACE_ELEVATED);
            setPreferredSize(new Dimension(0, 28));
            setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD));
            addHint("Enter", "Ingresar");
            addHint("Tab", "Mover foco");
        }

        private void addHint(String keyText, String description) {
            JLabel key = new JLabel("[" + keyText + "]");
            key.setFont(Fonts.mono(Font.PLAIN, 11f));
            key.setForeground(Theme.PRIMARY);
            JLabel desc = new JLabel(" " + description);
            desc.setFont(Fonts.inter(Font.PLAIN, 11f));
            desc.setForeground(Theme.MUTED);
            add(key);
            add(desc);
            add(Box.createHorizontalStrut(Theme.SP_LG));
        }
    }
}
