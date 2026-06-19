package pe.plazavea.perecibles.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.config.SpringContext;
import pe.plazavea.perecibles.service.AlertaServicio;
import pe.plazavea.perecibles.service.UsuarioServicio;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.panel.AlertasPanel;
import pe.plazavea.perecibles.ui.panel.ConfiguracionPanel;
import pe.plazavea.perecibles.ui.panel.DashboardPanel;
import pe.plazavea.perecibles.ui.panel.InventarioPanel;
import pe.plazavea.perecibles.ui.panel.LoginPanel;
import pe.plazavea.perecibles.ui.panel.ReportesPanel;
import pe.plazavea.perecibles.ui.panel.ShortcutBar;
import pe.plazavea.perecibles.ui.panel.SidebarPanel;
import pe.plazavea.perecibles.ui.panel.ToolbarPanel;
import pe.plazavea.perecibles.util.KeyboardHandler;
import pe.plazavea.perecibles.util.SessionManager;

@Component
@Lazy
public final class MainFrame extends JFrame implements Navigator {

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootCards = new JPanel(rootLayout);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final ToolbarPanel toolbar = new ToolbarPanel();
    private final ShortcutBar shortcutBar = new ShortcutBar();
    private final SidebarPanel sidebar = new SidebarPanel(this);
    private final LoginPanel loginPanel;
    private final ObjectProvider<DashboardPanel> dashboardPanel;
    private final ObjectProvider<InventarioPanel> inventarioPanel;
    private final ObjectProvider<AlertasPanel> alertasPanel;
    private final ObjectProvider<ReportesPanel> reportesPanel;
    private final ObjectProvider<ConfiguracionPanel> configuracionPanel;
    private final UsuarioServicio usuarioServicio;
    private final Set<String> loadedScreens = new HashSet<>();
    private String currentScreen = "login";

    public MainFrame(
            ObjectProvider<DashboardPanel> dashboardPanel,
            ObjectProvider<InventarioPanel> inventarioPanel,
            ObjectProvider<AlertasPanel> alertasPanel,
            ObjectProvider<ReportesPanel> reportesPanel,
            ObjectProvider<ConfiguracionPanel> configuracionPanel,
            UsuarioServicio usuarioServicio
    ) {
        super("Plaza Vea - Control de Perecibles");
        this.dashboardPanel = dashboardPanel;
        this.inventarioPanel = inventarioPanel;
        this.alertasPanel = alertasPanel;
        this.reportesPanel = reportesPanel;
        this.configuracionPanel = configuracionPanel;
        this.usuarioServicio = usuarioServicio;
        setMinimumSize(new Dimension(1024, 700));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        loginPanel = new LoginPanel(this, usuarioServicio);
        rootCards.add(loginPanel, "login");
        rootCards.add(buildShell(), "shell");
        setContentPane(rootCards);
        registerShortcuts();
        registerShutdown();
        show("login");
    }

    @Override
    public void show(String screen) {
        if ("login".equals(screen)) {
            currentScreen = "login";
            loginPanel.resetForm();
            rootLayout.show(rootCards, "login");
            return;
        }
        if (("reportes".equals(screen) || "configuracion".equals(screen))
                && (SessionManager.getCurrentUser() == null
                || SessionManager.getCurrentUser().getRol() != RolUsuario.SUPERVISOR)) {
            if ("reportes".equals(screen)) {
                screen = "dashboard";
            }
        }

        sidebar.refreshSession();
        rootLayout.show(rootCards, "shell");
        ensureScreenLoaded(screen);
        contentLayout.show(contentCards, screen);
        currentScreen = screen;
        sidebar.setActive(screen);
        toolbar.setScreenTitle(titleFor(screen));
        shortcutBar.setHints(hintsForScreen(screen));
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.CANVAS);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.CANVAS);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(contentCards, BorderLayout.CENTER);

        contentCards.setBackground(Theme.CANVAS);

        shell.add(sidebar, BorderLayout.WEST);
        shell.add(center, BorderLayout.CENTER);
        shell.add(shortcutBar, BorderLayout.SOUTH);
        return shell;
    }

    private void ensureScreenLoaded(String screen) {
        if (!loadedScreens.add(screen)) {
            return;
        }
        JPanel panel = switch (screen) {
            case "dashboard" -> dashboardPanel.getObject();
            case "inventario" -> inventarioPanel.getObject();
            case "alertas" -> alertasPanel.getObject();
            case "reportes" -> reportesPanel.getObject();
            case "configuracion" -> configuracionPanel.getObject();
            default -> null;
        };
        if (panel != null) {
            contentCards.add(panel, screen);
            triggerInitialLoad(screen);
        }
    }

    private void triggerInitialLoad(String screen) {
        switch (screen) {
            case "dashboard" -> dashboardPanel.getObject().refreshDashboard();
            case "inventario" -> inventarioPanel.getObject().refreshTable();
            case "alertas" -> alertasPanel.getObject().refreshAlerts();
            case "reportes" -> reportesPanel.getObject().generateReport();
            case "configuracion" -> configuracionPanel.getObject().refreshConfiguracion();
            default -> {
            }
        }
    }

    private void registerShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(event -> KeyboardHandler.handle(event, this));
    }

    private void registerShutdown() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                SpringContext.getBean(AlertaServicio.class).getScheduler().shutdown();
            }
        });
    }

    @Override
    public String getCurrentName() {
        return currentScreen;
    }

    @Override
    public void refreshCurrentScreen() {
        switch (currentScreen) {
            case "dashboard" -> dashboardPanel.getObject().refreshDashboard();
            case "inventario" -> inventarioPanel.getObject().refreshTable();
            case "alertas" -> alertasPanel.getObject().refreshAlerts();
            case "reportes" -> reportesPanel.getObject().generateReport();
            case "configuracion" -> configuracionPanel.getObject().refreshConfiguracion();
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
            case "configuracion" -> "Configuración";
            default -> "Control de Perecibles";
        };
    }

    private List<ShortcutBar.ShortcutHint> hintsForScreen(String screen) {
        return switch (screen) {
            case "inventario" -> List.of(
                    new ShortcutBar.ShortcutHint("N", "Nuevo lote"),
                    new ShortcutBar.ShortcutHint("V", "Marcar vencido"),
                    new ShortcutBar.ShortcutHint("R", "Marcar remate"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
            case "alertas" -> List.of(
                    new ShortcutBar.ShortcutHint("V", "Atender alerta"),
                    new ShortcutBar.ShortcutHint("I", "Ignorar alerta"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
            case "reportes" -> List.of(
                    new ShortcutBar.ShortcutHint("Ctrl+G", "Dashboard"),
                    new ShortcutBar.ShortcutHint("Ctrl+I", "Inventario"),
                    new ShortcutBar.ShortcutHint("Ctrl+A", "Alertas"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar")
            );
            case "configuracion" -> List.of(
                    new ShortcutBar.ShortcutHint("Ctrl+,", "Configuracion"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar")
            );
            default -> List.of(
                    new ShortcutBar.ShortcutHint("Ctrl+G", "Dashboard"),
                    new ShortcutBar.ShortcutHint("Ctrl+I", "Inventario"),
                    new ShortcutBar.ShortcutHint("Ctrl+A", "Alertas"),
                    new ShortcutBar.ShortcutHint("Ctrl+,", "Configuracion"),
                    new ShortcutBar.ShortcutHint("F5", "Refrescar"),
                    new ShortcutBar.ShortcutHint("?", "Atajos")
            );
        };
    }
}
