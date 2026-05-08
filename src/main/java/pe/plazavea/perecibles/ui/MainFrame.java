package pe.plazavea.perecibles.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.panel.AlertasPanel;
import pe.plazavea.perecibles.ui.panel.DashboardPanel;
import pe.plazavea.perecibles.ui.panel.InventarioPanel;
import pe.plazavea.perecibles.ui.panel.LoginPanel;
import pe.plazavea.perecibles.ui.panel.ReportesPanel;
import pe.plazavea.perecibles.ui.panel.ShortcutBar;
import pe.plazavea.perecibles.ui.panel.SidebarPanel;
import pe.plazavea.perecibles.ui.panel.ToolbarPanel;

public final class MainFrame extends JFrame implements Navigator {

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootCards = new JPanel(rootLayout);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final ToolbarPanel toolbar = new ToolbarPanel();
    private final ShortcutBar shortcutBar = new ShortcutBar();
    private final SidebarPanel sidebar = new SidebarPanel(this);

    public MainFrame() {
        super("Plaza Vea - Control de Perecibles");
        setMinimumSize(new Dimension(1024, 700));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        rootCards.add(new LoginPanel(this), "login");
        rootCards.add(buildShell(), "shell");
        setContentPane(rootCards);
        registerShortcuts();
        show("login");
    }

    @Override
    public void show(String screen) {
        if ("login".equals(screen)) {
            rootLayout.show(rootCards, "login");
            return;
        }

        rootLayout.show(rootCards, "shell");
        contentLayout.show(contentCards, screen);
        sidebar.setActive(screen);
        toolbar.setScreenTitle(titleFor(screen));
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.CANVAS_DARK);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.CANVAS_DARK);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(contentCards, BorderLayout.CENTER);

        contentCards.setBackground(Theme.CANVAS_DARK);
        contentCards.add(new DashboardPanel(), "dashboard");
        contentCards.add(new InventarioPanel(this), "inventario");
        contentCards.add(new AlertasPanel(), "alertas");
        contentCards.add(new ReportesPanel(), "reportes");

        shell.add(sidebar, BorderLayout.WEST);
        shell.add(center, BorderLayout.CENTER);
        shell.add(shortcutBar, BorderLayout.SOUTH);
        return shell;
    }

    private void registerShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            boolean ctrl = (event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
            if (ctrl && event.getKeyCode() == KeyEvent.VK_G) {
                show("dashboard");
                return true;
            }
            if (ctrl && event.getKeyCode() == KeyEvent.VK_I) {
                show("inventario");
                return true;
            }
            if (ctrl && event.getKeyCode() == KeyEvent.VK_A) {
                show("alertas");
                return true;
            }
            if (ctrl && event.getKeyCode() == KeyEvent.VK_R) {
                show("reportes");
                return true;
            }
            if (event.getKeyCode() == KeyEvent.VK_SLASH && event.isShiftDown()) {
                shortcutBar.toggle();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.VK_F5) {
                refreshCurrentScreen();
                return true;
            }
            return false;
        });
    }

    private void refreshCurrentScreen() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done() {
                toolbar.setScreenTitle("Actualizado");
            }
        }.execute();
    }

    private String titleFor(String screen) {
        return switch (screen) {
            case "dashboard" -> "Dashboard";
            case "inventario" -> "Inventario";
            case "alertas" -> "Alertas";
            case "reportes" -> "Reportes";
            default -> "Control de Perecibles";
        };
    }
}
