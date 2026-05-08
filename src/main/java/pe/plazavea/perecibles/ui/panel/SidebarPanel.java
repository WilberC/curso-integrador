package pe.plazavea.perecibles.ui.panel;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.Navigator;
import pe.plazavea.perecibles.util.SessionManager;

public final class SidebarPanel extends JPanel {

    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private final JLabel footerName = new JLabel();
    private final JLabel footerRole = new JLabel();

    public SidebarPanel(Navigator navigator) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 0));
        setBackground(Theme.SURFACE_CARD);

        add(wordmark());
        add(separator());
        addItem("dashboard", "Dashboard", navigator);
        addItem("inventario", "Inventario", navigator);
        addItem("alertas", "Alertas", navigator);
        addItem("reportes", "Reportes", navigator);
        add(Box.createVerticalGlue());
        add(userFooter());
        refreshSession();
    }

    public void setActive(String screen) {
        items.forEach((key, item) -> item.setActive(key.equals(screen)));
    }

    public void refreshSession() {
        var user = SessionManager.getCurrentUser();
        if (user == null) {
            user = MockData.getSupervisor();
        }
        footerName.setText(user.getNombre() + " " + user.getApellido());
        footerRole.setText(user.getRol().name());
        NavItem reportes = items.get("reportes");
        if (reportes != null) {
            reportes.setVisible(user.getRol() == RolUsuario.SUPERVISOR);
        }
        revalidate();
        repaint();
    }

    private JPanel wordmark() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.SURFACE_CARD);
        panel.setPreferredSize(new Dimension(200, 56));
        panel.setMaximumSize(new Dimension(200, 56));
        panel.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel label = new JLabel("Plaza Vea");
        label.setFont(Fonts.inter(Font.BOLD, 20f));
        label.setForeground(Theme.PRIMARY);
        panel.add(label);
        return panel;
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.HAIRLINE_DARK);
        separator.setMaximumSize(new Dimension(200, 1));
        return separator;
    }

    private void addItem(String key, String label, Navigator navigator) {
        NavItem item = new NavItem(label);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                navigator.show(key);
            }
        });
        items.put(key, item);
        add(item);
    }

    private JPanel userFooter() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.SURFACE_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SP_MD, Theme.SP_MD, Theme.SP_MD, Theme.SP_MD));
        panel.setMaximumSize(new Dimension(200, 96));

        footerName.setFont(Fonts.inter(Font.BOLD, 13f));
        footerName.setForeground(Theme.BODY);
        footerRole.setFont(Fonts.inter(Font.PLAIN, 11f));
        footerRole.setForeground(Theme.MUTED);

        panel.add(footerName);
        panel.add(Box.createVerticalStrut(Theme.SP_XXS));
        panel.add(footerRole);
        return panel;
    }

    private static final class NavItem extends JPanel {
        private final JLabel label = new JLabel();
        private boolean active;

        private NavItem(String text) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension(200, 48));
            setMaximumSize(new Dimension(200, 48));
            setBackground(Theme.SURFACE_CARD);
            setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            label.setText(text);
            label.setFont(Fonts.inter(Font.PLAIN, 13f));
            label.setForeground(Theme.MUTED);
            add(label);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    setBackground(Theme.SURFACE_ELEVATED);
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    setBackground(active ? Theme.SURFACE_ELEVATED : Theme.SURFACE_CARD);
                }
            });
        }

        private void setActive(boolean active) {
            this.active = active;
            setBackground(active ? Theme.SURFACE_ELEVATED : Theme.SURFACE_CARD);
            label.setForeground(active ? Theme.ON_DARK : Theme.MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (active) {
                graphics.setColor(Theme.PRIMARY);
                graphics.fillRect(0, 0, 3, getHeight());
            }
        }
    }
}
