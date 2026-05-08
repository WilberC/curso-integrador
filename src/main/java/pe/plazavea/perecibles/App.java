package pe.plazavea.perecibles;

import javax.swing.SwingUtilities;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pe.plazavea.perecibles.config.JpaConfig;
import pe.plazavea.perecibles.config.SpringContext;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.MainFrame;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Theme.apply();
        SpringContext.init(new AnnotationConfigApplicationContext(JpaConfig.class));
        Fonts.load();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
