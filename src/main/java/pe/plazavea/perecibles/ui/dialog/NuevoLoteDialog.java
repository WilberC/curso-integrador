package pe.plazavea.perecibles.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import net.miginfocom.swing.MigLayout;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.service.InventarioServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.util.DateParser;
import pe.plazavea.perecibles.util.SessionManager;

public final class NuevoLoteDialog extends JDialog {

    private final JComboBox<ProductoPerecible> productoField;
    private final JTextField numeroField = textField("");
    private final JTextField cantidadField = textField("");
    private final JTextField ubicacionField = textField("");
    private final JTextField vencimientoField = textField("Hoy + 15");
    private final JLabel datePreview = new JLabel(" ");
    private final InventarioServicio inventarioServicio;

    public NuevoLoteDialog(Frame owner, InventarioServicio inventarioServicio, List<ProductoPerecible> productos) {
        super(owner, "Nuevo Lote", true);
        this.inventarioServicio = inventarioServicio;
        productoField = new JComboBox<>(productos.toArray(ProductoPerecible[]::new));
        productoField.setRenderer((list, value, index, selected, focused) -> new JLabel(value == null ? "" : value.getNombre()));
        productoField.addActionListener(event -> updateNumeroLotePreview());
        numeroField.setEditable(false);
        numeroField.setFocusable(false);
        numeroField.setBackground(Theme.CANVAS);
        setSize(480, 520);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildLayout());
        setLocationRelativeTo(owner);
        registerKeys();
        installDigitFilter();
        updateDatePreview();
        updateNumeroLotePreview();
    }

    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE_SOFT);

        JLabel title = new JLabel("Nuevo Lote");
        title.setFont(Fonts.inter(Font.BOLD, 18f));
        title.setForeground(Theme.INK);
        JButton close = Buttons.secondary("x");
        close.addActionListener(event -> dispose());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE_SOFT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE),
                BorderFactory.createEmptyBorder(20, Theme.SP_LG, Theme.SP_MD, Theme.SP_LG)
        ));
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel body = new JPanel(new MigLayout("insets 24, gapy 12", "[right][grow, fill]"));
        body.setBackground(Theme.SURFACE_SOFT);
        addFormRow(body, "Producto", productoField);
        addFormRow(body, "Nro. Lote", numeroField);
        addFormRow(body, "Cantidad inicial", cantidadField);
        addFormRow(body, "Ubicación", ubicacionField);
        addFormRow(body, "Vencimiento", buildDateInput());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, Theme.SP_MD));
        footer.setBackground(Theme.SURFACE_SOFT);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE));
        JButton cancel = Buttons.secondary("Cancelar");
        cancel.addActionListener(event -> dispose());
        JButton save = Buttons.primary("Registrar");
        save.addActionListener(event -> save());
        footer.add(cancel);
        footer.add(save);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildDateInput() {
        JPanel panel = new JPanel(new BorderLayout(0, Theme.SP_XXS));
        panel.setBackground(Theme.SURFACE_SOFT);
        datePreview.setFont(Fonts.mono(Font.PLAIN, 12f));
        vencimientoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                updateDatePreview();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                updateDatePreview();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                updateDatePreview();
            }
        });
        panel.add(vencimientoField, BorderLayout.NORTH);
        panel.add(datePreview, BorderLayout.SOUTH);
        return panel;
    }

    private void addFormRow(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.inter(Font.PLAIN, 13f));
        label.setForeground(Theme.MUTED);
        panel.add(label);
        panel.add(field, "growx, wrap");
    }

    private JTextField textField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(Fonts.inter(Font.PLAIN, 13f));
        field.setBorder(defaultBorder());
        return field;
    }

    private void save() {
        boolean valid = true;
        valid &= requireText(cantidadField);
        valid &= requireText(ubicacionField);
        var parsedDate = DateParser.parse(vencimientoField.getText());
        if (parsedDate.isEmpty()) {
            vencimientoField.setBorder(BorderFactory.createLineBorder(Theme.DANGER, 1, true));
            valid = false;
        }
        ProductoPerecible producto = (ProductoPerecible) productoField.getSelectedItem();
        if (!valid || producto == null) {
            return;
        }

        Lote lote = new Lote();
        int cantidad = Integer.parseInt(cantidadField.getText());
        lote.setNumeroLote(null);
        lote.setProducto(producto);
        lote.setCantidadInicial((double) cantidad);
        lote.setCantidadActual((double) cantidad);
        lote.setFechaVencimiento(parsedDate.orElseThrow());
        lote.setUbicacion(ubicacionField.getText().trim());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                inventarioServicio.registrarIngreso(lote, SessionManager.getCurrentUser());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            NuevoLoteDialog.this,
                            "No se pudo registrar el lote: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void updateNumeroLotePreview() {
        Object selected = productoField.getSelectedItem();
        if (!(selected instanceof ProductoPerecible producto)) {
            numeroField.setText("");
            return;
        }
        numeroField.setText("Generando...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return inventarioServicio.generarNumeroLote(producto);
            }

            @Override
            protected void done() {
                if (producto != productoField.getSelectedItem()) {
                    return;
                }
                try {
                    numeroField.setText(get());
                    numeroField.setBorder(defaultBorder());
                } catch (Exception exception) {
                    numeroField.setText("No disponible");
                    numeroField.setBorder(BorderFactory.createLineBorder(Theme.DANGER, 1, true));
                }
            }
        }.execute();
    }

    private boolean requireText(JTextField field) {
        boolean valid = !field.getText().trim().isBlank();
        field.setBorder(valid ? defaultBorder() : BorderFactory.createLineBorder(Theme.DANGER, 1, true));
        return valid;
    }

    private void updateDatePreview() {
        DateParser.parse(vencimientoField.getText()).ifPresentOrElse(date -> {
            datePreview.setText("-> " + DateParser.formatLong(date));
            datePreview.setForeground(Theme.SAFE);
            vencimientoField.setBorder(defaultBorder());
        }, () -> {
            datePreview.setText("Fecha no reconocida");
            datePreview.setForeground(Theme.DANGER);
        });
    }

    private void installDigitFilter() {
        ((AbstractDocument) cantidadField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.chars().allMatch(Character::isDigit)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.chars().allMatch(Character::isDigit)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void registerKeys() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispose();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "saveDialog");
        getRootPane().getActionMap().put("saveDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                save();
            }
        });
    }

    private javax.swing.border.Border defaultBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_SM, Theme.SP_XS, Theme.SP_SM)
        );
    }
}
