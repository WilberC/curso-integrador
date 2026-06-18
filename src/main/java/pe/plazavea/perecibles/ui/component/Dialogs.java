package pe.plazavea.perecibles.ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public final class Dialogs {

    private Dialogs() {
    }

    public static void showMessage(Component parent, Object message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = createCenteredDialog(optionPane, parent, title);
        dialog.setVisible(true);
    }

    public static int showConfirm(Component parent, Object message, String title, int optionType) {
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType);
        JDialog dialog = createCenteredDialog(optionPane, parent, title);
        dialog.setVisible(true);
        Object value = optionPane.getValue();
        if (value instanceof Integer option) {
            return option;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static String showInput(Component parent, Object message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION);
        optionPane.setWantsInput(true);
        JDialog dialog = createCenteredDialog(optionPane, parent, title);
        optionPane.selectInitialValue();
        dialog.setVisible(true);
        Object value = optionPane.getValue();
        if (!(value instanceof Integer option) || option != JOptionPane.OK_OPTION) {
            return null;
        }
        Object input = optionPane.getInputValue();
        if (input == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return input == null ? null : input.toString();
    }

    private static JDialog createCenteredDialog(JOptionPane optionPane, Component parent, String title) {
        Window owner = ownerWindow(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(optionPane);
        dialog.setLocationByPlatform(false);
        optionPane.addPropertyChangeListener(event -> {
            if (dialog.isVisible() && JOptionPane.VALUE_PROPERTY.equals(event.getPropertyName())) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.pack();
        center(dialog, owner);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                center(dialog, owner);
            }
        });
        return dialog;
    }

    private static Window ownerWindow(Component parent) {
        return parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
    }

    private static void center(JDialog dialog, Window owner) {
        dialog.setLocation(centerLocation(dialog.getSize(), owner));
    }

    private static Point centerLocation(Dimension dialogSize, Window owner) {
        GraphicsConfiguration configuration = owner == null
                ? GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                : owner.getGraphicsConfiguration();
        Rectangle bounds = configuration.getBounds();
        Rectangle target = owner != null && owner.isShowing() ? owner.getBounds() : bounds;
        int x = target.x + (target.width - dialogSize.width) / 2;
        int y = target.y + (target.height - dialogSize.height) / 2;
        int maxX = Math.max(bounds.x, bounds.x + bounds.width - dialogSize.width);
        int maxY = Math.max(bounds.y, bounds.y + bounds.height - dialogSize.height);
        return new Point(clamp(x, bounds.x, maxX), clamp(y, bounds.y, maxY));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
