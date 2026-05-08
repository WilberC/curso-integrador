package pe.plazavea.perecibles.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.panel.AlertasPanel;
import pe.plazavea.perecibles.ui.panel.DashboardPanel;
import pe.plazavea.perecibles.ui.panel.InventarioPanel;
import pe.plazavea.perecibles.ui.panel.LoginPanel;
import pe.plazavea.perecibles.ui.panel.ReportesPanel;
import pe.plazavea.perecibles.ui.panel.ShortcutBar;
import pe.plazavea.perecibles.ui.panel.SidebarPanel;
import pe.plazavea.perecibles.ui.panel.ToolbarPanel;
import pe.plazavea.perecibles.util.KeyboardHandler;
import pe.plazavea.perecibles.util.SessionManager;

public final class MainFrame extends JFrame implements Navigator {

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootCards = new JPanel(rootLayout);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final ToolbarPanel toolbar = new ToolbarPanel();
    private final ShortcutBar shortcutBar = new ShortcutBar();
    private final SidebarPanel sidebar = new SidebarPanel(this);
    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final InventarioPanel inventarioPanel = new InventarioPanel(this);
    private final AlertasPanel alertasPanel = new AlertasPanel();
    private final ReportesPanel reportesPanel = new ReportesPanel();
    private String currentScreen = "login";

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
            currentScreen = "login";
            rootLayout.show(rootCards, "login");
            return;
        }
        if ("reportes".equals(screen)
                && (SessionManager.getCurrentUser() == null
                || SessionManager.getCurrentUser().getRol() != RolUsuario.SUPERVISOR)) {
            screen = "dashboard";
        }

        sidebar.refreshSession();
        rootLayout.show(rootCards, "shell");
        contentLayout.show(contentCards, screen);
        currentScreen = screen;
        sidebar.setActive(screen);
        toolbar.setScreenTitle(titleFor(screen));
        shortcutBar.setHints(hintsForScreen(screen));
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.CANVAS_DARK);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.CANVAS_DARK);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(contentCards, BorderLayout.CENTER);

        contentCards.setBackground(Theme.CANVAS_DARK);
        contentCards.add(dashboardPanel, "dashboard");
        contentCards.add(inventarioPanel, "inventario");
        contentCards.add(alertasPanel, "alertas");
        contentCards.add(reportesPanel, "reportes");

        shell.add(sidebar, BorderLayout.WEST);
        shell.add(center, BorderLayout.CENTER);
        shell.add(shortcutBar, BorderLayout.SOUTH);
        return shell;
    }

    private void registerShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(event -> KeyboardHandler.handle(event, this));
    }

    @Override
    public String getCurrentName() {
        return currentScreen;
    }

    @Override
    public void refreshCurrentScreen() {
        switch (currentScreen) {
            case "dashboard" -> dashboardPanel.refreshDashboard();
            case "inventario" -> inventarioPanel.refreshTable();
            case "alertas" -> alertasPanel.refreshAlerts();
            case "reportes" -> reportesPanel.generateReport();
            default -> {
            }
        }
        toolbar.setScreenTitle(titleFor(currentScreen) + " - Actualizado");
    }

    @Override
    public void toggleShortcutBar() {
        shortcutBar.toggle();
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

    private List<ShortcutBar.ShortcutHint> hintsForScreen(String screen) {
        return switch (screen) {
            case "inventario" -> List.of(
                    new ShortcutBar.ShortcutHint("N", "Nuevo"),
                    new ShortcutBar.ShortcutHint("V", "Vencido"),
                    new ShortcutBar.ShortcutHint("R", "Remate"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
            case "alertas" -> List.of(
                    new ShortcutBar.ShortcutHint("V", "Atender"),
                    new ShortcutBar.ShortcutHint("I", "Ignorar"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
            case "reportes" -> List.of(
                    new ShortcutBar.ShortcutHint("Ctrl+G", "Dashboard"),
                    new ShortcutBar.ShortcutHint("Ctrl+I", "Inventario"),
                    new ShortcutBar.ShortcutHint("Ctrl+A", "Alertas"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar")
            );
            default -> List.of(
                    new ShortcutBar.ShortcutHint("Ctrl+G", "Dashboard"),
                    new ShortcutBar.ShortcutHint("Ctrl+I", "Inventario"),
                    new ShortcutBar.ShortcutHint("Ctrl+A", "Alertas"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
        };
    }
}
