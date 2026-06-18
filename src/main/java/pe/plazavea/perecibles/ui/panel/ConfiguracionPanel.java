package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.service.ConfiguracionServicio;
import pe.plazavea.perecibles.service.UsuarioServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.table.CategoriaTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;
import pe.plazavea.perecibles.ui.table.UsuarioTableModel;
import pe.plazavea.perecibles.util.SessionManager;

@Component
@Lazy
public final class ConfiguracionPanel extends JPanel {

    private static final int DIALOG_FORM_MIN_WIDTH = 420;

    private final JSpinner criticos = new JSpinner(new SpinnerNumberModel(1, 0, 365, 1));
    private final JSpinner advertencia = new JSpinner(new SpinnerNumberModel(3, 0, 365, 1));
    private final JSpinner aviso = new JSpinner(new SpinnerNumberModel(7, 0, 365, 1));
    private final UsuarioTableModel usuarioModel = new UsuarioTableModel();
    private final CategoriaTableModel categoriaModel = new CategoriaTableModel();
    private final JTable usuarioTable = TableFactory.simpleTable(usuarioModel);
    private final JTable categoriaTable = TableFactory.simpleTable(categoriaModel);
    private final ConfiguracionServicio configuracionServicio;
    private final UsuarioServicio usuarioServicio;

    public ConfiguracionPanel(ConfiguracionServicio configuracionServicio, UsuarioServicio usuarioServicio) {
        this.configuracionServicio = configuracionServicio;
        this.usuarioServicio = usuarioServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        refreshConfiguracion();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            refreshConfiguracion();
        }
    }

    public void refreshConfiguracion() {
        removeAll();
        Usuario current = SessionManager.getCurrentUser();
        if (current == null || !current.esSupervisor()) {
            add(accessDenied(), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }
        JScrollPane contentScroll = new JScrollPane(buildContent());
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(contentScroll, BorderLayout.CENTER);
        loadData();
        revalidate();
        repaint();
    }

    private JComponent buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.CANVAS);
        content.add(sectionTitle("Umbrales de alertas"));
        content.add(thresholdsPanel());
        content.add(Box.createVerticalStrut(Theme.SP_LG));
        content.add(sectionTitle("Gestión de usuarios"));
        content.add(usersPanel());
        content.add(Box.createVerticalStrut(Theme.SP_LG));
        content.add(sectionTitle("Categorías de productos"));
        content.add(categoriesPanel());
        return content;
    }

    private JPanel thresholdsPanel() {
        JPanel panel = sectionPanel(new GridLayout(1, 4, Theme.SP_MD, 0));
        panel.add(spinnerField("Días críticos", criticos));
        panel.add(spinnerField("Días de advertencia", advertencia));
        panel.add(spinnerField("Aviso anticipado", aviso));
        var save = Buttons.primary("Guardar umbrales");
        save.addActionListener(event -> saveThresholds());
        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, Theme.SP_LG));
        action.setBackground(Theme.SURFACE_SOFT);
        action.add(save);
        panel.add(action);
        return panel;
    }

    private JPanel usersPanel() {
        JPanel panel = sectionPanel(new BorderLayout(0, Theme.SP_SM));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, 0));
        actions.setBackground(Theme.SURFACE_SOFT);
        var nuevo = Buttons.primary("Nuevo usuario");
        var editar = Buttons.secondary("Editar usuario");
        var estado = Buttons.secondary("Activar / Desactivar");
        nuevo.addActionListener(event -> openNewUserDialog());
        editar.addActionListener(event -> openEditUserDialog());
        estado.addActionListener(event -> toggleSelectedUser());
        actions.add(nuevo);
        actions.add(editar);
        actions.add(estado);
        JScrollPane scrollPane = TableFactory.scrollPane(usuarioTable);
        scrollPane.setPreferredSize(new Dimension(0, 160));
        panel.add(actions, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel categoriesPanel() {
        JPanel panel = sectionPanel(new BorderLayout(0, Theme.SP_SM));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, 0));
        actions.setBackground(Theme.SURFACE_SOFT);
        var nueva = Buttons.primary("Nueva categoría");
        var estado = Buttons.secondary("Activar / Desactivar");
        nueva.addActionListener(event -> openNewCategoryDialog());
        estado.addActionListener(event -> toggleSelectedCategory());
        actions.add(nueva);
        actions.add(estado);
        JScrollPane scrollPane = TableFactory.scrollPane(categoriaTable);
        scrollPane.setPreferredSize(new Dimension(0, 140));
        panel.add(actions, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            private ConfiguracionAlerta config;
            private List<Usuario> usuarios;
            private List<Categoria> categorias;

            @Override
            protected Void doInBackground() {
                config = configuracionServicio.obtenerConfiguracionActiva();
                usuarios = usuarioServicio.listarUsuarios();
                categorias = configuracionServicio.listarCategorias();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    criticos.setValue(config.getDiasCriticos());
                    advertencia.setValue(config.getDiasAdvertencia());
                    aviso.setValue(config.getDiasAvisoAnticipado());
                    usuarioModel.setData(usuarios);
                    categoriaModel.setData(categorias);
                } catch (Exception exception) {
                    showError("No se pudo cargar configuracion", exception);
                }
            }
        }.execute();
    }

    private void saveThresholds() {
        runAsync(() -> configuracionServicio.guardarUmbrales(
                (Integer) criticos.getValue(),
                (Integer) advertencia.getValue(),
                (Integer) aviso.getValue(),
                SessionManager.getCurrentUser()
        ), "Umbrales guardados");
    }

    private void openNewUserDialog() {
        JTextField email = new JTextField();
        JTextField nombre = new JTextField();
        JPasswordField password = new JPasswordField();
        JComboBox<RolUsuario> rol = new JComboBox<>(RolUsuario.values());
        JPanel form = dialogForm();
        form.add(label("Correo electrónico"));
        form.add(email);
        form.add(label("Nombre completo"));
        form.add(nombre);
        form.add(label("Contraseña"));
        form.add(password);
        form.add(label("Rol"));
        form.add(rol);
        int result = Dialogs.showConfirm(this, form, "Nuevo usuario", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        Usuario nuevo = new Usuario();
        nuevo.setEmail(email.getText());
        setNombreCompleto(nuevo, nombre.getText());
        nuevo.setRol((RolUsuario) rol.getSelectedItem());
        nuevo.setActivo(true);
        runAsync(() -> {
            usuarioServicio.registrar(nuevo, new String(password.getPassword()));
            return null;
        }, "Usuario creado");
    }

    private void openEditUserDialog() {
        Usuario selected = selectedUser();
        if (selected == null) {
            return;
        }
        JTextField nombre = new JTextField(selected.getNombreCompleto());
        JComboBox<RolUsuario> rol = new JComboBox<>(RolUsuario.values());
        rol.setSelectedItem(selected.getRol());
        JComboBox<String> estado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        estado.setSelectedItem(selected.isActivo() ? "Activo" : "Inactivo");
        JPanel form = dialogForm();
        form.add(label("Correo electrónico"));
        form.add(new JLabel(selected.getEmail()));
        form.add(label("Nombre completo"));
        form.add(nombre);
        form.add(label("Rol"));
        form.add(rol);
        form.add(label("Estado"));
        form.add(estado);
        int result = Dialogs.showConfirm(this, form, "Editar usuario", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        runAsync(() -> {
            usuarioServicio.editar(
                    selected.getIdUsuario(),
                    nombre.getText(),
                    (RolUsuario) rol.getSelectedItem(),
                    "Activo".equals(estado.getSelectedItem()),
                    SessionManager.getCurrentUser()
            );
            return null;
        }, "Usuario actualizado");
    }

    private void toggleSelectedUser() {
        Usuario selected = selectedUser();
        if (selected == null) {
            return;
        }
        runAsync(() -> {
            usuarioServicio.cambiarEstado(selected.getIdUsuario(), !selected.isActivo(), SessionManager.getCurrentUser());
            return null;
        }, "Estado de usuario actualizado");
    }

    private void openNewCategoryDialog() {
        String nombre = Dialogs.showInput(this, "Nombre de la categoría", "Nueva categoría", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null) {
            return;
        }
        runAsync(() -> {
            configuracionServicio.crearCategoria(nombre);
            return null;
        }, "Categoria creada");
    }

    private void toggleSelectedCategory() {
        Categoria selected = selectedCategory();
        if (selected == null) {
            return;
        }
        runAsync(() -> {
            configuracionServicio.cambiarEstadoCategoria(selected.getIdCategoria(), !selected.isActivo());
            return null;
        }, "Estado de categoria actualizado");
    }

    private Usuario selectedUser() {
        int row = usuarioTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showMessage(
                    this,
                    "Seleccione un usuario de la tabla",
                    "Usuario requerido",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        return usuarioModel.getUsuarioAt(usuarioTable.convertRowIndexToModel(row));
    }

    private Categoria selectedCategory() {
        int row = categoriaTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return categoriaModel.getCategoriaAt(categoriaTable.convertRowIndexToModel(row));
    }

    private void runAsync(WorkerAction action, String successMessage) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                action.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.showMessage(
                            ConfiguracionPanel.this,
                            successMessage,
                            "Operacion completada",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    refreshConfiguracion();
                } catch (Exception exception) {
                    showError("No se pudo completar la operacion", exception);
                }
            }
        }.execute();
    }

    private JPanel accessDenied() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CANVAS);
        JLabel label = new JLabel("Acceso denegado. Este modulo es exclusivo para Supervisores.");
        label.setFont(Fonts.inter(Font.BOLD, 16f));
        label.setForeground(Theme.DANGER);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JPanel sectionPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Theme.SURFACE_SOFT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_MD, Theme.SP_MD, Theme.SP_MD, Theme.SP_MD)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.INK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_SM, 0));
        return title;
    }

    private JPanel spinnerField(String text, JSpinner spinner) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.SURFACE_SOFT);
        JLabel label = label(text);
        spinner.setMaximumSize(new Dimension(180, 34));
        panel.add(label);
        panel.add(Box.createVerticalStrut(Theme.SP_XS));
        panel.add(spinner);
        return panel;
    }

    private JPanel dialogForm() {
        return new JPanel(new GridLayout(0, 2, Theme.SP_SM, Theme.SP_SM)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                return new Dimension(Math.max(DIALOG_FORM_MIN_WIDTH, preferred.width), preferred.height);
            }
        };
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 12f));
        label.setForeground(Theme.MUTED);
        return label;
    }

    private void setNombreCompleto(Usuario usuario, String nombreCompleto) {
        String value = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        int split = value.lastIndexOf(' ');
        if (split < 0) {
            usuario.setNombre(value);
            usuario.setApellido("");
            return;
        }
        usuario.setNombre(value.substring(0, split));
        usuario.setApellido(value.substring(split + 1));
    }

    private void showError(String prefix, Exception exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        Dialogs.showMessage(this, prefix + ": " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface WorkerAction {
        Object run() throws Exception;
    }
}
