package pe.plazavea.perecibles.theme;

import com.formdev.flatlaf.FlatIntelliJLaf;
import java.awt.Color;
import javax.swing.UIManager;

public final class Theme {

    public static final Color CANVAS = hex("#ffffff");
    public static final Color SURFACE_SOFT = hex("#f8fafc");
    public static final Color SURFACE_STRONG = hex("#e0e2e6");

    public static final Color PRIMARY = hex("#181d26");
    public static final Color PRIMARY_ACTIVE = hex("#0d1218");
    public static final Color PRIMARY_DISABLED = hex("#c8cdd4");
    public static final Color ON_PRIMARY = hex("#ffffff");

    public static final Color SAFE = hex("#006400");
    public static final Color WARNING = hex("#b45309");
    public static final Color DANGER = hex("#aa2d00");

    public static final Color SAFE_TINT = hex("#dcfce7");
    public static final Color WARNING_TINT = hex("#fef3c7");
    public static final Color DANGER_TINT = hex("#fee2e2");

    public static final Color INK = hex("#181d26");
    public static final Color BODY = hex("#333840");
    public static final Color MUTED = hex("#41454d");
    public static final Color MUTED_STRONG = hex("#9297a0");
    public static final Color ON_DARK = hex("#ffffff");

    public static final Color HAIRLINE = hex("#dddddd");
    public static final Color BORDER_STRONG = hex("#9297a0");
    public static final Color FOCUS_RING = hex("#458fff");
    public static final Color TABLE_SELECTION = hex("#e8eaed");

    public static final int RADIUS_SM = 4;
    public static final int RADIUS_MD = 6;
    public static final int RADIUS_LG = 8;
    public static final int RADIUS_XL = 12;

    public static final int SP_XXS = 4;
    public static final int SP_XS = 8;
    public static final int SP_SM = 12;
    public static final int SP_MD = 16;
    public static final int SP_LG = 24;
    public static final int SP_XL = 32;

    private Theme() {
    }

    public static void apply() {
        FlatIntelliJLaf.setup();

        UIManager.put("Panel.background", CANVAS);
        UIManager.put("RootPane.background", CANVAS);
        UIManager.put("OptionPane.background", SURFACE_SOFT);

        UIManager.put("Table.background", SURFACE_SOFT);
        UIManager.put("Table.foreground", BODY);
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("Table.gridColor", HAIRLINE);
        UIManager.put("TableHeader.background", SURFACE_STRONG);
        UIManager.put("TableHeader.foreground", MUTED);

        UIManager.put("TextField.background", CANVAS);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.caretForeground", INK);
        UIManager.put("ComboBox.background", CANVAS);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("PasswordField.background", CANVAS);

        UIManager.put("ScrollPane.background", CANVAS);
        UIManager.put("ScrollBar.thumbColor", SURFACE_STRONG);
        UIManager.put("ScrollBar.trackColor", SURFACE_SOFT);

        UIManager.put("Component.arc", RADIUS_MD);
        UIManager.put("Button.arc", RADIUS_MD);
        UIManager.put("TextComponent.arc", RADIUS_MD);
        UIManager.put("Component.focusColor", FOCUS_RING);
        UIManager.put("Component.focusWidth", 2);
    }

    private static Color hex(String value) {
        return Color.decode(value);
    }
}
