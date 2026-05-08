package pe.plazavea.perecibles.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import net.miginfocom.swing.MigLayout;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.util.DateParser;

public final class NuevoLoteDialog extends JDialog {

    private final Map<String, String> categoriasPorProducto = new LinkedHashMap<>();
    private final JComboBox<String> productoField;
    private final JTextField numeroField = textField("");
    private final JTextField cantidadField = textField("");
    private final JTextField ubicacionField = textField("");
    private final JTextField vencimientoField = textField("Hoy + 15");
    private final JLabel datePreview = new JLabel(" ");

    public NuevoLoteDialog(Frame owner) {
        super(owner, "Nuevo Lote", true);
        MockData.getLotes().forEach(lote -> categoriasPorProducto.putIfAbsent(lote.getProducto(), lote.getCategoria()));
        productoField = new JComboBox<>(categoriasPorProducto.keySet().toArray(String[]::new));
        setSize(480, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildLayout());
        registerKeys();
        installDigitFilter();
        updateDatePreview();
    }

    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE_CARD);

        JLabel title = new JLabel("Nuevo Lote");
        title.setFont(Fonts.inter(Font.BOLD, 18f));
        title.setForeground(Theme.ON_DARK);
        JButton close = Buttons.secondary("x");
        close.addActionListener(event -> dispose());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE_DARK),
                BorderFactory.createEmptyBorder(20, Theme.SP_LG, Theme.SP_MD, Theme.SP_LG)
        ));
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel body = new JPanel(new MigLayout("insets 24, gapy 12", "[right][grow, fill]"));
        body.setBackground(Theme.SURFACE_CARD);
        addFormRow(body, "Producto", productoField);
        addFormRow(body, "Nro. Lote", numeroField);
        addFormRow(body, "Cantidad inicial", cantidadField);
        addFormRow(body, "Ubicacion", ubicacionField);
        addFormRow(body, "Vencimiento", buildDateInput());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, Theme.SP_MD));
        footer.setBackground(Theme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE_DARK));
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
        panel.setBackground(Theme.SURFACE_CARD);
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
        valid &= requireText(numeroField);
        valid &= requireText(cantidadField);
        valid &= requireText(ubicacionField);
        var parsedDate = DateParser.parse(vencimientoField.getText());
        if (parsedDate.isEmpty()) {
            vencimientoField.setBorder(BorderFactory.createLineBorder(Theme.DANGER, 1, true));
            valid = false;
        }
        if (!valid) {
            return;
        }
        String producto = String.valueOf(productoField.getSelectedItem());
        int cantidad = Integer.parseInt(cantidadField.getText());
        MockData.addLote(new Lote(
                MockData.nextLoteId(),
                numeroField.getText().trim(),
                producto,
                categoriasPorProducto.getOrDefault(producto, "General"),
                cantidad,
                cantidad,
                LocalDate.now(),
                parsedDate.orElseThrow(),
                ubicacionField.getText().trim(),
                EstadoLote.DISPONIBLE
        ));
        dispose();
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
                BorderFactory.createLineBorder(Theme.HAIRLINE_DARK, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_SM, Theme.SP_XS, Theme.SP_SM)
        );
    }
}

