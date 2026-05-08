package pe.plazavea.perecibles.theme;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public final class Fonts {

    private static Font inter = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static Font jetBrainsMono = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private Fonts() {
    }

    public static void load() {
        inter = loadFont("/fonts/Inter-Regular.ttf", Font.SANS_SERIF);
        jetBrainsMono = loadFont("/fonts/JetBrainsMono-Regular.ttf", Font.MONOSPACED);

        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphics.registerFont(inter);
        graphics.registerFont(jetBrainsMono);
    }

    public static Font inter(int style, float size) {
        return inter.deriveFont(style, size);
    }

    public static Font mono(int style, float size) {
        return jetBrainsMono.deriveFont(style, size);
    }

    private static Font loadFont(String path, String fallbackFamily) {
        try (InputStream stream = Fonts.class.getResourceAsStream(path)) {
            if (stream == null) {
                return new Font(fallbackFamily, Font.PLAIN, 13);
            }
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (FontFormatException | IOException exception) {
            return new Font(fallbackFamily, Font.PLAIN, 13);
        }
    }
}
