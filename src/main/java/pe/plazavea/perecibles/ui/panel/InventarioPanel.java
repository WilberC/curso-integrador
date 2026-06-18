package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.TipoMovimiento;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.service.InventarioServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Buttons;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.dialog.NuevoLoteDialog;
import pe.plazavea.perecibles.ui.table.LoteTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;
import pe.plazavea.perecibles.util.SessionManager;

@org.springframework.context.annotation.Lazy
@org.springframework.stereotype.Component
public final class InventarioPanel extends JPanel {

    private final LoteTableModel model = new LoteTableModel(List.of());
    private final JTable table = TableFactory.loteTable(model);
    private final JTextField search = new JTextField();
    private final JComboBox<String> categoria = new JComboBox<>(new String[]{"Todas", "Lacteos", "Carnes", "Embutidos", "Panaderia"});
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Todos", "Disponible", "Próximo a vencer", "Vencido", "Retirado"});
    private final InventarioServicio inventarioServicio;
    private List<Lote> currentLotes = List.of();
    private List<ProductoPerecible> productos = List.of();

    public InventarioPanel(InventarioServicio inventarioServicio) {
        this.inventarioServicio = inventarioServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        add(buildActions(), BorderLayout.NORTH);
        add(TableFactory.scrollPane(table), BorderLayout.CENTER);
        installTableActions();
        registerShortcuts();
        refreshTable();
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Theme.CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

        var nuevo = Buttons.primary("+  Nuevo Lote  [N]");
        nuevo.addActionListener(event -> openNuevoLote());

        search.setFont(Fonts.inter(Font.PLAIN, 13f));
        search.setPreferredSize(new Dimension(220, 36));
        search.setMaximumSize(new Dimension(260, 36));
        search.putClientProperty("JTextField.placeholderText", "Buscar producto o lote");
        search.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        categoria.addActionListener(event -> applyFilters());
        estado.addActionListener(event -> applyFilters());

        panel.add(nuevo);
        panel.add(Box.createHorizontalStrut(Theme.SP_MD));
        panel.add(categoria);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(estado);
        panel.add(Box.createHorizontalGlue());
        panel.add(search);
        return panel;
    }

    public void openNuevoLote() {
        java.awt.Frame owner = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
        new NuevoLoteDialog(owner, inventarioServicio, productos).setVisible(true);
        refreshTable();
    }

    public void openEditarLote() {
        Lote lote = selectedLote();
        if (lote == null) {
            return;
        }
        openEditarLote(lote);
    }

    private void openEditarLote(Lote lote) {
        java.awt.Frame owner = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
        new NuevoLoteDialog(owner, inventarioServicio, productos, lote).setVisible(true);
        refreshTable();
    }

    public void refreshTable() {
        new SwingWorker<List<Lote>, Void>() {
            @Override
            protected List<Lote> doInBackground() {
                productos = inventarioServicio.listarProductos();
                return inventarioServicio.consultarStock();
            }

            @Override
            protected void done() {
                try {
                    currentLotes = get();
                    applyFilters();
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            InventarioPanel.this,
                            "No se pudo cargar el inventario: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void applyFilters() {
        String query = search.getText().trim().toLowerCase();
        String selectedCategory = String.valueOf(categoria.getSelectedItem());
        String selectedState = String.valueOf(estado.getSelectedItem());
        model.setData(currentLotes.stream()
                .filter(lote -> query.isBlank()
                        || lote.getProducto().toLowerCase().contains(query)
                        || lote.getNumeroLote().toLowerCase().contains(query))
                .filter(lote -> "Todas".equals(selectedCategory) || lote.getCategoria().equals(selectedCategory))
                .filter(lote -> "Todos".equals(selectedState) || stateLabel(lote.getEstado()).equals(selectedState))
                .toList());
    }

    private void registerShortcuts() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "nuevoLote");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "editarLote");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "marcarVencido");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "marcarRemate");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "filaAnterior");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "filaSiguiente");
        getActionMap().put("nuevoLote", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    openNuevoLote();
                }
            }
        });
        getActionMap().put("editarLote", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    openEditarLote();
                }
            }
        });
        getActionMap().put("marcarVencido", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    marcarVencido();
                }
            }
        });
        getActionMap().put("marcarRemate", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    marcarRemate();
                }
            }
        });
        getActionMap().put("filaAnterior", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    moveSelection(-1);
                }
            }
        });
        getActionMap().put("filaSiguiente", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    moveSelection(1);
                }
            }
        });
    }

    public void marcarVencido() {
        updateSelected(TipoMovimiento.RETIRO, "Retiro por vencimiento");
    }

    public void marcarRemate() {
        updateSelected(TipoMovimiento.REMATE, "Remate preventivo");
    }

    private void installTableActions() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int column = table.columnAtPoint(event.getPoint());
                if (row < 0 || column != 7) {
                    return;
                }
                table.setRowSelectionInterval(row, row);
                Lote lote = model.getLoteAt(table.convertRowIndexToModel(row));
                switch (actionAt(event, row, column)) {
                    case EDITAR -> openEditarLote(lote);
                    case VENCIDO -> updateLote(lote, TipoMovimiento.RETIRO, "Retiro por vencimiento");
                    case REMATE -> updateLote(lote, TipoMovimiento.REMATE, "Remate preventivo");
                }
            }
        });
    }

    private LoteAction actionAt(MouseEvent event, int row, int column) {
        int x = event.getX() - table.getCellRect(row, column, true).x;
        int segmentWidth = Math.max(1, table.getColumnModel().getColumn(column).getWidth() / 3);
        if (x < segmentWidth) {
            return LoteAction.EDITAR;
        }
        if (x < segmentWidth * 2) {
            return LoteAction.VENCIDO;
        }
        return LoteAction.REMATE;
    }

    private void updateSelected(TipoMovimiento tipo, String motivo) {
        Lote lote = selectedLote();
        if (lote == null) {
            return;
        }
        updateLote(lote, tipo, motivo);
    }

    private void updateLote(Lote lote, TipoMovimiento tipo, String motivo) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                inventarioServicio.registrarRetiro(
                        lote.getIdLote(),
                        lote.getCantidadActualValue(),
                        tipo,
                        motivo,
                        SessionManager.getCurrentUser()
                );
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshTable();
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            InventarioPanel.this,
                            "No se pudo actualizar el lote: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private Lote selectedLote() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            Dialogs.showMessage(
                    this,
                    "Seleccione un lote de la tabla",
                    "Lote requerido",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        return model.getLoteAt(table.convertRowIndexToModel(selected));
    }

    private void moveSelection(int delta) {
        if (table.getRowCount() == 0) {
            return;
        }
        int selected = table.getSelectedRow();
        int next = Math.max(0, Math.min(table.getRowCount() - 1, selected + delta));
        table.setRowSelectionInterval(next, next);
        table.scrollRectToVisible(table.getCellRect(next, 0, true));
    }

    private boolean isTextEditing() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return focused instanceof JTextComponent;
    }

    private String stateLabel(EstadoLote estado) {
        return switch (estado) {
            case DISPONIBLE -> "Disponible";
            case PROXIMO_VENCER -> "Próximo a vencer";
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

    private enum LoteAction {
        EDITAR,
        VENCIDO,
        REMATE
    }
}
