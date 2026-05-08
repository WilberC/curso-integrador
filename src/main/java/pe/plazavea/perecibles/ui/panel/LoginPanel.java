package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.Navigator;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.util.SessionManager;

public final class LoginPanel extends JPanel {

    private final JTextField emailField = new JTextField("supervisor@plazavea.com");
    private final JPasswordField passwordField = new JPasswordField("admin");
    private final JLabel errorLabel = new JLabel("Credenciales incorrectas");
    private final Navigator navigator;

    public LoginPanel(Navigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.CANVAS_DARK);
        center.add(buildCard(), new GridBagConstraints());

        add(center, BorderLayout.CENTER);
        add(new LoginShortcutBar(), BorderLayout.SOUTH);
        registerLoginShortcut();
    }

    private JPanel buildCard() {
        JPanel card = new RoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 380));
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XL, Theme.SP_XL, Theme.SP_XL, Theme.SP_XL));

        JLabel wordmark = new JLabel("Plaza Vea");
        wordmark.setFont(Fonts.inter(Font.BOLD, 22f));
        wordmark.setForeground(Theme.PRIMARY);

        JLabel title = new JLabel("Control de Perecibles");
        title.setFont(Fonts.inter(Font.BOLD, 20f));
        title.setForeground(Theme.ON_DARK);

        errorLabel.setFont(Fonts.inter(Font.PLAIN, 12f));
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setVisible(false);

        card.add(wordmark);
        card.add(Box.createVerticalStrut(Theme.SP_XXS));
        card.add(title);
        card.add(Box.createVerticalStrut(Theme.SP_LG));
        card.add(field("Usuario", emailField));
        card.add(Box.createVerticalStrut(Theme.SP_SM));
        card.add(field("Contraseña", passwordField));
        card.add(Box.createVerticalStrut(Theme.SP_SM));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(Theme.SP_MD));

        var login = Buttons.primary("Iniciar Sesión");
        login.addActionListener(event -> attemptLogin());
        login.setAlignmentX(LEFT_ALIGNMENT);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if ("operario@plazavea.com".equalsIgnoreCase(email) && "admin".equals(password)) {
            SessionManager.setCurrentUser(MockData.getOperario());
            navigator.show("dashboard");
            return;
        }
        if ("supervisor@plazavea.com".equalsIgnoreCase(email) && "admin".equals(password)) {
            SessionManager.setCurrentUser(MockData.getSupervisor());
            navigator.show("dashboard");
            return;
        }
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    private void registerLoginShortcut() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
        getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                attemptLogin();
            }
        });
    }

    private static final class RoundedCard extends JPanel {
        private RoundedCard() {
            setBackground(Theme.SURFACE_CARD);
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

    private static final class LoginShortcutBar extends JPanel {
        private LoginShortcutBar() {
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
