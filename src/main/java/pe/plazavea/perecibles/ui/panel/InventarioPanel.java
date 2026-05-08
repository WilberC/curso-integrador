package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.dialog.NuevoLoteDialog;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;

public final class InventarioPanel extends JPanel {

    private final java.awt.Frame owner;
    private final LoteTableModel model = new LoteTableModel(List.of());
    private final JTable table = TableFactory.loteTable(model);
    private final JTextField search = new JTextField();
    private final JComboBox<String> categoria = new JComboBox<>(new String[]{"Todas", "Lacteos", "Carnes", "Embutidos", "Panaderia"});
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Todos", "Disponible", "Proximo vencer", "Vencido", "Retirado"});

    public InventarioPanel(java.awt.Frame owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS_DARK);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        add(buildActions(), BorderLayout.NORTH);
        add(TableFactory.scrollPane(table), BorderLayout.CENTER);
        registerShortcuts();
        refreshTable();
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Theme.CANVAS_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

        var nuevo = Buttons.primary("+  Nuevo Lote  [N]");
        nuevo.addActionListener(event -> openNuevoLote());

        search.setFont(Fonts.inter(Font.PLAIN, 13f));
        search.setPreferredSize(new Dimension(220, 36));
        search.setMaximumSize(new Dimension(260, 36));
        search.putClientProperty("JTextField.placeholderText", "Buscar producto o lote");
        search.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshTable));
        categoria.addActionListener(event -> refreshTable());
        estado.addActionListener(event -> refreshTable());

        panel.add(nuevo);
        panel.add(Box.createHorizontalStrut(Theme.SP_MD));
        panel.add(categoria);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(estado);
        panel.add(Box.createHorizontalGlue());
        panel.add(search);
        return panel;
    }

    private void openNuevoLote() {
        new NuevoLoteDialog(owner).setVisible(true);
        refreshTable();
    }

    private void refreshTable() {
        String query = search.getText().trim().toLowerCase();
        String selectedCategory = String.valueOf(categoria.getSelectedItem());
        String selectedState = String.valueOf(estado.getSelectedItem());
        model.setData(MockData.getLotes().stream()
                .filter(lote -> query.isBlank()
                        || lote.getProducto().toLowerCase().contains(query)
                        || lote.getNumeroLote().toLowerCase().contains(query))
                .filter(lote -> "Todas".equals(selectedCategory) || lote.getCategoria().equals(selectedCategory))
                .filter(lote -> "Todos".equals(selectedState) || stateLabel(lote.getEstado()).equals(selectedState))
                .toList());
    }

    private void registerShortcuts() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "nuevoLote");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "marcarVencido");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "marcarRemate");
        getActionMap().put("nuevoLote", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                openNuevoLote();
            }
        });
        getActionMap().put("marcarVencido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                updateSelected(EstadoLote.VENCIDO);
            }
        });
        getActionMap().put("marcarRemate", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                updateSelected(EstadoLote.RETIRADO);
            }
        });
    }

    private void updateSelected(EstadoLote newState) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return;
        }
        model.getLoteAt(table.convertRowIndexToModel(selected)).setEstado(newState);
        refreshTable();
    }

    private String stateLabel(EstadoLote estado) {
        return switch (estado) {
            case DISPONIBLE -> "Disponible";
            case PROXIMO_VENCER -> "Proximo vencer";
            case VENCIDO -> "Vencido";
            case RETIRADO -> "Retirado";
        };
    }

    private record SimpleDocumentListener(Runnable callback) implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            callback.run();
        }
    }
}

