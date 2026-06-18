package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import pe.plazavea.perecibles.util.DateParser;
import pe.plazavea.perecibles.util.SessionManager;

@org.springframework.context.annotation.Lazy
@org.springframework.stereotype.Component
public final class InventarioPanel extends JPanel {

    private final LoteTableModel model = new LoteTableModel(List.of());
    private final JTable table = TableFactory.loteTable(model);
    private final JTextField search = new JTextField();
    private final JTextField fechaDesde = new JTextField();
    private final JTextField fechaHasta = new JTextField();
    private final JComboBox<String> categoria = new JComboBox<>(new String[]{"Todas", "Lacteos", "Carnes", "Embutidos", "Panaderia"});
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Activos", "Todos", "Disponible", "Próximo a vencer", "Vencido", "Retirado"});
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
        configureDateFilter(fechaDesde, "Desde");
        configureDateFilter(fechaHasta, "Hasta");
        categoria.addActionListener(event -> applyFilters());
        estado.addActionListener(event -> applyFilters());
        var limpiarFechas = Buttons.secondary("Limpiar fechas");
        limpiarFechas.addActionListener(event -> clearDateFilters());

        panel.add(nuevo);
        panel.add(Box.createHorizontalStrut(Theme.SP_MD));
        panel.add(toolbarLabel("Categoria"));
        panel.add(categoria);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(toolbarLabel("Estado"));
        panel.add(estado);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(toolbarLabel("Vencimiento"));
        panel.add(fechaDesde);
        panel.add(Box.createHorizontalStrut(Theme.SP_XXS));
        panel.add(fechaHasta);
        panel.add(Box.createHorizontalStrut(Theme.SP_XS));
        panel.add(limpiarFechas);
        panel.add(Box.createHorizontalGlue());
        panel.add(search);
        return panel;
    }

    private void configureDateFilter(JTextField field, String placeholder) {
        field.setFont(Fonts.inter(Font.PLAIN, 13f));
        field.setPreferredSize(new Dimension(96, 36));
        field.setMaximumSize(new Dimension(110, 36));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setToolTipText("dd/mm/aaaa o Hoy + 7");
        field.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
    }

    private void clearDateFilters() {
        fechaDesde.setText("");
        fechaHasta.setText("");
        setDateFieldValid(fechaDesde, true, "dd/mm/aaaa o Hoy + 7");
        setDateFieldValid(fechaHasta, true, "dd/mm/aaaa o Hoy + 7");
        applyFilters();
    }

    private JLabel toolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.inter(Font.PLAIN, 12f));
        label.setForeground(Theme.MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XXS));
        return label;
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
        if (lote.getEstado() == EstadoLote.RETIRADO) {
            Dialogs.showMessage(
                    this,
                    "Este lote ya fue retirado y queda solo como historial.",
                    "Lote retirado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        java.awt.Frame owner = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
        new NuevoLoteDialog(owner, inventarioServicio, productos, lote).setVisible(true);
        refreshTable();
    }

    public void refreshTable() {
        new SwingWorker<List<Lote>, Void>() {
            @Override
            protected List<Lote> doInBackground() {
                productos = inventarioServicio.listarProductos();
                return inventarioServicio.consultarInventario();
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
        DateRange dateRange = selectedDateRange();
        if (!dateRange.valid()) {
            return;
        }
        model.setData(currentLotes.stream()
                .filter(lote -> query.isBlank()
                        || lote.getProducto().toLowerCase().contains(query)
                        || lote.getNumeroLote().toLowerCase().contains(query))
                .filter(lote -> "Todas".equals(selectedCategory) || lote.getCategoria().equals(selectedCategory))
                .filter(lote -> switch (selectedState) {
                    case "Activos" -> lote.getEstado() != EstadoLote.RETIRADO;
                    case "Todos" -> true;
                    default -> stateLabel(lote.getEstado()).equals(selectedState);
                })
                .filter(lote -> matchesDateRange(lote, dateRange))
                .toList());
    }

    private DateRange selectedDateRange() {
        ParsedDate from = parseDateFilter(fechaDesde);
        ParsedDate to = parseDateFilter(fechaHasta);
        if (from.valid() && to.valid() && from.date().isPresent() && to.date().isPresent()
                && from.date().orElseThrow().isAfter(to.date().orElseThrow())) {
            setDateFieldValid(fechaDesde, false, "Desde no puede ser mayor que Hasta");
            setDateFieldValid(fechaHasta, false, "Hasta no puede ser menor que Desde");
            return new DateRange(from.date(), to.date(), false);
        }
        return new DateRange(from.date(), to.date(), from.valid() && to.valid());
    }

    private ParsedDate parseDateFilter(JTextField field) {
        String text = field.getText().trim();
        if (text.isBlank()) {
            setDateFieldValid(field, true, "dd/mm/aaaa o Hoy + 7");
            return new ParsedDate(Optional.empty(), true);
        }
        Optional<LocalDate> parsed = DateParser.parse(text);
        setDateFieldValid(field, parsed.isPresent(), parsed.isPresent() ? "dd/mm/aaaa o Hoy + 7" : "Fecha no reconocida");
        return new ParsedDate(parsed, parsed.isPresent());
    }

    private void setDateFieldValid(JTextField field, boolean valid, String tooltip) {
        field.putClientProperty("JComponent.outline", valid ? null : "error");
        field.setToolTipText(tooltip);
    }

    private boolean matchesDateRange(Lote lote, DateRange dateRange) {
        LocalDate vencimiento = lote.getFechaVencimiento();
        if (vencimiento == null) {
            return false;
        }
        boolean afterStart = dateRange.from().isEmpty() || !vencimiento.isBefore(dateRange.from().orElseThrow());
        boolean beforeEnd = dateRange.to().isEmpty() || !vencimiento.isAfter(dateRange.to().orElseThrow());
        return afterStart && beforeEnd;
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
                    case VENCIDO -> updateLote(lote, TipoMovimiento.RETIRO, "Retiro por vencimiento", "Vencido");
                    case REMATE -> updateLote(lote, TipoMovimiento.REMATE, "Remate preventivo", "Remate");
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
        updateLote(lote, tipo, motivo, tipo == TipoMovimiento.REMATE ? "Remate" : "Vencido");
    }

    private void updateLote(Lote lote, TipoMovimiento tipo, String motivo, String actionLabel) {
        if (lote.getEstado() == EstadoLote.RETIRADO) {
            Dialogs.showMessage(
                    this,
                    "Este lote ya fue retirado. Puede revisarlo con el filtro Estado: Retirado.",
                    "Lote retirado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        int result = Dialogs.showConfirm(
                this,
                confirmMessage(lote, actionLabel),
                "Confirmar " + actionLabel.toLowerCase(),
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
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
                    estado.setSelectedItem("Retirado");
                    refreshTable();
                    Dialogs.showMessage(
                            InventarioPanel.this,
                            "Lote retirado. Puede verlo con el filtro Estado: Retirado.",
                            "Lote actualizado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
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

    private String confirmMessage(Lote lote, String actionLabel) {
        return """
                Esta accion registrara el lote como %s y retirara todo su stock.

                Lote: %s
                Producto: %s
                Cantidad: %d

                Luego podra revisarlo con el filtro Estado: Retirado.
                """.formatted(actionLabel, lote.getNumeroLote(), lote.getProducto(), lote.getCantidadActual());
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

    private record ParsedDate(Optional<LocalDate> date, boolean valid) {
    }

    private record DateRange(Optional<LocalDate> from, Optional<LocalDate> to, boolean valid) {
    }

    private enum LoteAction {
        EDITAR,
        VENCIDO,
        REMATE
    }
}
