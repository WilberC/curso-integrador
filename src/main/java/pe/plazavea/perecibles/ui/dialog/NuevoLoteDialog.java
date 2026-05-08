package pe.plazavea.perecibles.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.miginfocom.swing.MigLayout;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;

public final class NuevoLoteDialog extends JDialog {

    public NuevoLoteDialog(Frame owner) {
        super(owner, "Nuevo Lote", true);
        setSize(480, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildLayout());
        registerEscapeKey();
    }

    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE_CARD);

        JLabel title = new JLabel("Nuevo Lote");
        title.setFont(Fonts.inter(Font.BOLD, 18f));
        title.setForeground(Theme.ON_DARK);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE_DARK),
                BorderFactory.createEmptyBorder(20, Theme.SP_LG, Theme.SP_MD, Theme.SP_LG)
        ));
        header.add(title, BorderLayout.WEST);

        JPanel body = new JPanel(new MigLayout("insets 24, gapy 12", "[right][grow, fill]"));
        body.setBackground(Theme.SURFACE_CARD);
        addFormRow(body, "Producto", new JComboBox<>(new String[]{"Leche Gloria 1L", "Yogur Fresa 500g", "Pollo Entero 1.8kg"}));
        addFormRow(body, "Nro. Lote", textField("L-007"));
        addFormRow(body, "Cantidad", textField("24"));
        addFormRow(body, "Ubicacion", textField("Anaquel A4"));
        addFormRow(body, "Vencimiento", textField("Hoy + 15"));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, Theme.SP_MD));
        footer.setBackground(Theme.SURFACE_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE_DARK));
        footer.add(Buttons.secondary("Cancelar"));
        footer.add(Buttons.primary("Registrar"));

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
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
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.HAIRLINE_DARK, 1, true),
                BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_SM, Theme.SP_XS, Theme.SP_SM)
        ));
        return field;
    }

    private void registerEscapeKey() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dispose();
            }
        });
    }
}
