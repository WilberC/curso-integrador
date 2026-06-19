package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.model.Alerta;
import pe.plazavea.perecibles.service.AlertaServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.table.AlertaTableModel;
import pe.plazavea.perecibles.ui.table.TableFactory;
import pe.plazavea.perecibles.util.SessionManager;

@org.springframework.context.annotation.Lazy
@org.springframework.stereotype.Component
public final class AlertasPanel extends JPanel {

    private final AlertaTableModel model = new AlertaTableModel(List.of());
    private final JTable table = TableFactory.alertaTable(model);
    private final JLabel countBadge = new JLabel();
    private final JCheckBox showAll = new JCheckBox("Ver historial completo");
    private final AlertaServicio alertaServicio;

    public AlertasPanel(AlertaServicio alertaServicio) {
        this.alertaServicio = alertaServicio;
        setLayout(new BorderLayout());
        setBackground(Theme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));
        add(buildHeader(), BorderLayout.NORTH);
        add(TableFactory.scrollPane(table), BorderLayout.CENTER);
        installTableActions();
        registerShortcuts();
        refreshAlerts();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CANVAS);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));
        header.add(buildToolbar(), BorderLayout.NORTH);
        header.add(buildActionHelp(), BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.CANVAS);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_XS, 0));

        JLabel title = new JLabel("Alertas");
        title.setFont(Fonts.inter(Font.BOLD, 15f));
        title.setForeground(Theme.INK);
        countBadge.setFont(Fonts.mono(Font.BOLD, 12f));
        countBadge.setForeground(Theme.WARNING);
        showAll.setFont(Fonts.inter(Font.PLAIN, 13f));
        showAll.setForeground(Theme.BODY);
        showAll.setBackground(Theme.CANVAS);
        showAll.addActionListener(event -> refreshAlerts());

        JPanel left = new JPanel();
        left.setBackground(Theme.CANVAS);
        left.add(title);
        left.add(countBadge);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(showAll, BorderLayout.EAST);
        return toolbar;
    }

    private JLabel buildActionHelp() {
        JLabel help = new JLabel("""
                <html><b>Atender</b>: usar cuando el lote ya fue revisado o gestionado. \
                La alerta pasa a ATENDIDA y sale de pendientes. \
                <b>Ignorar</b>: usar cuando la alerta no aplica o no requiere accion. \
                La alerta pasa a IGNORADA y sale de pendientes.</html>
                """);
        help.setFont(Fonts.inter(Font.PLAIN, 12f));
        help.setForeground(Theme.MUTED);
        help.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XS));
        return help;
    }

    public void refreshAlerts() {
        new SwingWorker<List<Alerta>, Void>() {
            @Override
            protected List<Alerta> doInBackground() {
                return showAll.isSelected() ? alertaServicio.obtenerTodas() : alertaServicio.obtenerPendientes();
            }

            @Override
            protected void done() {
                try {
                    List<Alerta> alertas = get();
                    model.setData(alertas);
                    long pendientes = alertas.stream()
                            .filter(alerta -> alerta.getEstado() == EstadoAlerta.PENDIENTE)
                            .count();
                    countBadge.setText("[" + pendientes + "]");
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            AlertasPanel.this,
                            "No se pudieron cargar las alertas: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void registerShortcuts() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "atender");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "ignorar");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "ignorar");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "filaAnterior");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "filaSiguiente");
        getActionMap().put("atender", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    atenderSeleccionada();
                }
            }
        });
        getActionMap().put("ignorar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!isTextEditing()) {
                    ignorarSeleccionada();
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

    public void atenderSeleccionada() {
        updateSelected(AlertaAction.ATENDER);
    }

    public void ignorarSeleccionada() {
        updateSelected(AlertaAction.IGNORAR);
    }

    private void installTableActions() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int column = table.columnAtPoint(event.getPoint());
                if (row < 0 || column != 5) {
                    return;
                }
                table.setRowSelectionInterval(row, row);
                Alerta alerta = model.getAlertaAt(table.convertRowIndexToModel(row));
                updateAlerta(alerta, actionAt(event, row, column));
            }
        });
    }

    private AlertaAction actionAt(MouseEvent event, int row, int column) {
        int x = event.getX() - table.getCellRect(row, column, true).x;
        int segmentWidth = Math.max(1, table.getColumnModel().getColumn(column).getWidth() / 2);
        return x < segmentWidth ? AlertaAction.ATENDER : AlertaAction.IGNORAR;
    }

    private void updateSelected(AlertaAction action) {
        Alerta alerta = selectedAlerta();
        if (alerta == null) {
            return;
        }
        updateAlerta(alerta, action);
    }

    private void updateAlerta(Alerta alerta, AlertaAction action) {
        if (alerta.getEstado() != EstadoAlerta.PENDIENTE) {
            Dialogs.showMessage(
                    this,
                    "Esta alerta ya fue procesada.",
                    "Alerta sin acciones",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        int result = Dialogs.showConfirm(
                this,
                confirmMessage(alerta, action),
                "Confirmar " + action.label().toLowerCase(),
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (action == AlertaAction.ATENDER) {
                    alertaServicio.atenderAlerta(alerta.getIdAlerta(), SessionManager.getCurrentUser());
                } else {
                    alertaServicio.ignorarAlerta(alerta.getIdAlerta());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshAlerts();
                } catch (Exception exception) {
                    Dialogs.showMessage(
                            AlertasPanel.this,
                            "No se pudo actualizar la alerta: " + exception.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private Alerta selectedAlerta() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            Dialogs.showMessage(
                    this,
                    "Seleccione una alerta de la tabla.",
                    "Alerta requerida",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }
        return model.getAlertaAt(table.convertRowIndexToModel(selected));
    }

    private String confirmMessage(Alerta alerta, AlertaAction action) {
        String message = """
                %s

                Cuando usarla:
                %s

                Que pasara:
                %s

                Lote: %s
                Tipo: %s
                Dias para vencer: %d
                """;
        return message.formatted(
                action.label(),
                action.whenToUse(),
                action.resultText(),
                alerta.getLoteNumero(),
                alerta.getTipoAlerta(),
                alerta.getDiasParaVencer()
        );
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

    private enum AlertaAction {
        ATENDER(
                "Atender",
                "Use esta opcion cuando ya reviso el lote o tomo la accion operativa necesaria.",
                "La alerta se marcara como ATENDIDA y dejara de aparecer como pendiente."
        ),
        IGNORAR(
                "Ignorar",
                "Use esta opcion cuando la alerta no aplica, es duplicada o no requiere accion.",
                "La alerta se marcara como IGNORADA y dejara de aparecer como pendiente."
        );

        private final String label;
        private final String whenToUse;
        private final String resultText;

        AlertaAction(String label, String whenToUse, String resultText) {
            this.label = label;
            this.whenToUse = whenToUse;
            this.resultText = resultText;
        }

        private String label() {
            return label;
        }

        private String whenToUse() {
            return whenToUse;
        }

        private String resultText() {
            return resultText;
        }
    }
}
