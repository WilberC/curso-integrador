package pe.plazavea.perecibles;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pe.plazavea.perecibles.config.AppConfig;
import pe.plazavea.perecibles.config.SpringContext;
import pe.plazavea.perecibles.service.AlertaServicio;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.Dialogs;
import pe.plazavea.perecibles.ui.MainFrame;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Theme.apply();
        Fonts.load();
        try {
            SpringContext.init(new AnnotationConfigApplicationContext(AppConfig.class));
            SpringContext.getBean(AlertaServicio.class).iniciarScheduler();
        } catch (Exception exception) {
            SwingUtilities.invokeLater(() -> {
                Dialogs.showMessage(
                        null,
                        "No se pudo conectar a la base de datos.\nVerifique que PostgreSQL este corriendo.",
                        "Error de conexion",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = SpringContext.getBean(MainFrame.class);
            frame.setVisible(true);
        });
    }
}
