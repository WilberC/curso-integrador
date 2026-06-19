package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
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
import javax.swing.Scrollable;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.plazavea.perecibles.enums.RolUsuario;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.service.ConfiguracionServicio;
import pe.plazavea.perecibles.service.UsuarioServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.table.CategoriaTableModel;
import pe.plazavea.perecibles.ui.table.ProductoTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;
import pe.plazavea.perecibles.ui.table.UsuarioTableModel;
import pe.plazavea.perecibles.util.SessionManager;

@Component
@Lazy
public final class ConfiguracionPanel extends JPanel {

    private static final int DIALOG_FORM_MIN_WIDTH = 420;
    private static final String DEFAULT_UNIDAD_MEDIDA = "unidad";
    private static final String[] UNIDADES_MEDIDA = {
            "unidad",
            "kg",
            "g",
            "litro",
            "ml",
            "paquete",
            "bandeja",
            "caja"
    };

    private final JSpinner criticos = new JSpinner(new SpinnerNumberModel(1, 0, 365, 1));
    private final JSpinner advertencia = new JSpinner(new SpinnerNumberModel(3, 0, 365, 1));
    private final JSpinner aviso = new JSpinner(new SpinnerNumberModel(7, 0, 365, 1));
    private final UsuarioTableModel usuarioModel = new UsuarioTableModel();
    private final CategoriaTableModel categoriaModel = new CategoriaTableModel();
    private final ProductoTableModel productoModel = new ProductoTableModel();
    private final JTable usuarioTable = TableFactory.simpleTable(usuarioModel);
    private final JTable categoriaTable = TableFactory.simpleTable(categoriaModel);
    private final JTable productoTable = TableFactory.simpleTable(productoModel);
    private final ConfiguracionServicio configuracionServicio;
    private final UsuarioServicio usuarioServicio;
    private int catalogTabIndex;
    private boolean contentBuilt;
    private boolean loaded;
    private boolean refreshInFlight;

    public ConfiguracionPanel(ConfiguracionServicio configuracionServicio, UsuarioServicio usuarioServicio) {
        this.configuracionServicio = configuracionServicio;
        this.usuarioServicio = usuarioServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && !loaded) {
            refreshConfiguracion();
        }
    }

    public void refreshConfiguracion() {
        Usuario current = SessionManager.getCurrentUser();
        if (current == null || !current.esSupervisor()) {
            removeAll();
            contentBuilt = false;
            loaded = false;
            add(accessDenied(), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }
        if (!contentBuilt) {
            removeAll();
            JScrollPane contentScroll = new JScrollPane(buildContent());
            contentScroll.setBorder(BorderFactory.createEmptyBorder());
            contentScroll.getVerticalScrollBar().setUnitIncrement(16);
            contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(contentScroll, BorderLayout.CENTER);
            contentBuilt = true;
        }
        loadData();
        revalidate();
        repaint();
    }

    private JComponent buildContent() {
        JPanel content = new ViewportWidthPanel();
        content.setLayout(new GridBagLayout());
        content.setBackground(Theme.CANVAS);

        int row = 0;
        addContentRow(content, sectionTitle("Umbrales de alertas"), row++, 0.0, Theme.SP_SM);
        addContentRow(content, thresholdsPanel(), row++, 0.0, Theme.SP_LG);
        addContentRow(content, sectionTitle("Gestión de usuarios"), row++, 0.0, Theme.SP_SM);
        addContentRow(content, usersPanel(), row++, 1.0, Theme.SP_LG);
        addContentRow(content, sectionTitle("Configuración de catálogo"), row++, 0.0, Theme.SP_SM);
        addContentRow(content, catalogTabs(), row, 1.0, 0);
        return content;
    }

    private void addContentRow(JPanel content, JComponent component, int row, double weightY, int bottomInset) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.weighty = weightY;
        constraints.fill = weightY > 0.0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        constraints.insets = new java.awt.Insets(0, 0, bottomInset, 0);
        content.add(component, constraints);
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

    private JTabbedPane catalogTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Fonts.inter(Font.BOLD, 13f));
        tabs.setBackground(Theme.CANVAS);
        tabs.addTab("Categorías", categoriesPanel());
        tabs.addTab("Productos", productsPanel());
        tabs.setSelectedIndex(Math.min(catalogTabIndex, tabs.getTabCount() - 1));
        tabs.addChangeListener(event -> catalogTabIndex = tabs.getSelectedIndex());
        return tabs;
    }

    private JPanel productsPanel() {
        JPanel panel = sectionPanel(new BorderLayout(0, Theme.SP_SM));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, 0));
        actions.setBackground(Theme.SURFACE_SOFT);
        var nuevo = Buttons.primary("Nuevo producto");
        var editar = Buttons.secondary("Editar producto");
        var estado = Buttons.secondary("Activar / Desactivar");
        nuevo.addActionListener(event -> openNewProductDialog());
        editar.addActionListener(event -> openEditProductDialog());
        estado.addActionListener(event -> toggleSelectedProduct());
        actions.add(nuevo);
        actions.add(editar);
        actions.add(estado);
        JScrollPane scrollPane = TableFactory.scrollPane(productoTable);
        scrollPane.setPreferredSize(new Dimension(0, 160));
        panel.add(actions, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        if (refreshInFlight) {
            return;
        }
        refreshInFlight = true;
        new SwingWorker<Void, Void>() {
            private ConfiguracionAlerta config;
            private List<Usuario> usuarios;
            private List<Categoria> categorias;
            private List<ProductoPerecible> productos;

            @Override
            protected Void doInBackground() {
                config = configuracionServicio.obtenerConfiguracionActiva();
                usuarios = usuarioServicio.listarUsuarios();
                categorias = configuracionServicio.listarCategorias();
                productos = configuracionServicio.listarProductos();
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
                    productoModel.setData(productos);
                    loaded = true;
                } catch (Exception exception) {
                    showError("No se pudo cargar configuracion", exception);
                } finally {
                    refreshInFlight = false;
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

    private void openNewProductDialog() {
        JTextField nombre = new JTextField();
        JTextField descripcion = new JTextField();
        JComboBox<String> unidad = unitCombo(DEFAULT_UNIDAD_MEDIDA);
        JComboBox<Categoria> categoria = categoryCombo(null);
        if (categoria.getItemCount() == 0) {
            Dialogs.showMessage(
                    this,
                    "Cree o active una categoria antes de registrar productos",
                    "Categoria requerida",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        JPanel form = productForm(nombre, descripcion, unidad, categoria);
        int result = Dialogs.showConfirm(this, form, "Nuevo producto", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        Categoria selectedCategoria = (Categoria) categoria.getSelectedItem();
        runAsync(() -> configuracionServicio.crearProducto(
                nombre.getText(),
                descripcion.getText(),
                selectedUnit(unidad),
                selectedCategoria == null ? null : selectedCategoria.getIdCategoria()
        ), "Producto creado");
    }

    private void openEditProductDialog() {
        ProductoPerecible selected = selectedProduct();
        if (selected == null) {
            return;
        }
        JTextField nombre = new JTextField(selected.getNombre());
        JTextField descripcion = new JTextField(selected.getDescripcion());
        JComboBox<String> unidad = unitCombo(selected.getUnidadMedida());
        JComboBox<Categoria> categoria = categoryCombo(selected.getCategoriaEntity());
        JPanel form = productForm(nombre, descripcion, unidad, categoria);
        int result = Dialogs.showConfirm(this, form, "Editar producto", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        Categoria selectedCategoria = (Categoria) categoria.getSelectedItem();
        runAsync(() -> configuracionServicio.editarProducto(
                selected.getIdProducto(),
                nombre.getText(),
                descripcion.getText(),
                selectedUnit(unidad),
                selectedCategoria == null ? null : selectedCategoria.getIdCategoria()
        ), "Producto actualizado");
    }

    private void toggleSelectedProduct() {
        ProductoPerecible selected = selectedProduct();
        if (selected == null) {
            return;
        }
        runAsync(() -> {
            configuracionServicio.cambiarEstadoProducto(selected.getIdProducto(), !selected.isActivo());
            return null;
        }, "Estado de producto actualizado");
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

    private ProductoPerecible selectedProduct() {
        int row = productoTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showMessage(
                    this,
                    "Seleccione un producto de la tabla",
                    "Producto requerido",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        return productoModel.getProductoAt(productoTable.convertRowIndexToModel(row));
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
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
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
        title.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
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

    private JPanel productForm(
            JTextField nombre,
            JTextField descripcion,
            JComboBox<String> unidad,
            JComboBox<Categoria> categoria
    ) {
        JPanel form = dialogForm();
        form.add(label("Nombre"));
        form.add(nombre);
        form.add(label("Descripcion"));
        form.add(descripcion);
        form.add(label("Unidad de medida"));
        form.add(unidad);
        form.add(label("Categoria"));
        form.add(categoria);
        return form;
    }

    private JComboBox<String> unitCombo(String selected) {
        List<String> unidades = new ArrayList<>(List.of(UNIDADES_MEDIDA));
        String selectedUnit = selected == null || selected.isBlank() ? DEFAULT_UNIDAD_MEDIDA : selected.trim();
        if (!unidades.contains(selectedUnit)) {
            unidades.add(selectedUnit);
        }
        JComboBox<String> combo = new JComboBox<>(unidades.toArray(String[]::new));
        combo.setSelectedItem(selectedUnit);
        return combo;
    }

    private String selectedUnit(JComboBox<String> unidad) {
        Object selected = unidad.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private JComboBox<Categoria> categoryCombo(Categoria selected) {
        List<Categoria> categories = new ArrayList<>(configuracionServicio.listarCategoriasActivas());
        if (selected != null && categories.stream().noneMatch(categoria -> sameCategory(categoria, selected))) {
            categories.add(selected);
        }
        JComboBox<Categoria> combo = new JComboBox<>(categories.toArray(Categoria[]::new));
        combo.setRenderer((list, value, index, selectedItem, focused) ->
                new JLabel(value == null ? "" : value.getNombre())
        );
        combo.setSelectedItem(selected);
        return combo;
    }

    private boolean sameCategory(Categoria first, Categoria second) {
        return first.getIdCategoria() != null && first.getIdCategoria().equals(second.getIdCategoria());
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

    private static final class ViewportWidthPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport viewport
                    && viewport.getHeight() > getPreferredSize().height;
        }
    }
}
