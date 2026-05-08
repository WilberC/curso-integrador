package pe.plazavea.perecibles.util;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import pe.plazavea.perecibles.ui.Navigator;

public final class KeyboardHandler {

    private KeyboardHandler() {
    }

    public static boolean handle(KeyEvent event, Navigator navigator) {
        return handle(event, navigator, null);
    }

    public static boolean handle(KeyEvent event, Navigator navigator, SessionManager session) {
        if (event.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }

        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused instanceof JTextComponent) {
            return false;
        }

        boolean ctrl = (event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
        if (ctrl) {
            return handleNavigation(event, navigator);
        }

        if (event.getKeyCode() == KeyEvent.VK_F5) {
            navigator.refreshCurrentScreen();
            return true;
        }
        if (event.getKeyCode() == KeyEvent.VK_SLASH && event.isShiftDown()) {
            navigator.toggleShortcutBar();
            return true;
        }

        return false;
    }

    private static boolean handleNavigation(KeyEvent event, Navigator navigator) {
        return switch (event.getKeyCode()) {
            case KeyEvent.VK_G -> {
                navigator.show("dashboard");
                yield true;
            }
            case KeyEvent.VK_I -> {
                navigator.show("inventario");
                yield true;
            }
            case KeyEvent.VK_A -> {
                navigator.show("alertas");
                yield true;
            }
            case KeyEvent.VK_R -> {
                navigator.show("reportes");
                yield true;
            }
            default -> false;
        };
    }
}
