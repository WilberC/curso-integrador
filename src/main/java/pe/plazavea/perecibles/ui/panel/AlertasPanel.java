package pe.plazavea.perecibles.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
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
        add(buildToolbar(), BorderLayout.NORTH);
        add(TableFactory.scrollPane(table), BorderLayout.CENTER);
        registerShortcuts();
        refreshAlerts();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.CANVAS);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SP_MD, 0));

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
        updateSelected(true);
    }

    public void ignorarSeleccionada() {
        updateSelected(false);
    }

    private void updateSelected(boolean atender) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            return;
        }
        Alerta alerta = model.getAlertaAt(table.convertRowIndexToModel(selected));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (atender) {
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
}
