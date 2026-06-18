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
        JDialog dialog = new JDialog(ownerWindow(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
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
        center(dialog);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                center(dialog);
            }
        });
        return dialog;
    }

    private static Window ownerWindow(Component parent) {
        return parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
    }

    private static void center(JDialog dialog) {
        dialog.setLocation(centerLocation(dialog.getSize()));
    }

    private static Point centerLocation(Dimension dialogSize) {
        GraphicsConfiguration configuration = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        Rectangle bounds = configuration.getBounds();
        int x = bounds.x + (bounds.width - dialogSize.width) / 2;
        int y = bounds.y + (bounds.height - dialogSize.height) / 2;
        return new Point(Math.max(bounds.x, x), Math.max(bounds.y, y));
    }
}
