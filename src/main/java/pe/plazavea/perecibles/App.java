package pe.plazavea.perecibles;

import javax.swing.SwingUtilities;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.MainFrame;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Fonts.load();
        Theme.apply();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
