package pe.plazavea.perecibles.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import javax.swing.UIManager;

public final class Theme {

    public static final Color CANVAS_DARK = hex("#0b0e11");
    public static final Color SURFACE_CARD = hex("#1e2329");
    public static final Color SURFACE_ELEVATED = hex("#2b3139");

    public static final Color PRIMARY = hex("#FCD535");
    public static final Color PRIMARY_ACTIVE = hex("#f0b90b");
    public static final Color PRIMARY_DISABLED = hex("#3a3a1f");
    public static final Color ON_PRIMARY = hex("#181a20");

    public static final Color SAFE = hex("#0ecb81");
    public static final Color WARNING = hex("#f0a500");
    public static final Color DANGER = hex("#f6465d");

    public static final Color SAFE_TINT = hex("#0a2e20");
    public static final Color WARNING_TINT = hex("#2e1f00");
    public static final Color DANGER_TINT = hex("#2e0a10");

    public static final Color BODY = hex("#eaecef");
    public static final Color MUTED = hex("#707a8a");
    public static final Color MUTED_STRONG = hex("#929aa5");
    public static final Color ON_DARK = hex("#ffffff");
    public static final Color INK = hex("#181a20");

    public static final Color HAIRLINE_DARK = hex("#2b3139");
    public static final Color HAIRLINE_LIGHT = hex("#eaecef");
    public static final Color FOCUS_RING = PRIMARY;

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
        FlatDarkLaf.setup();

        UIManager.put("Panel.background", CANVAS_DARK);
        UIManager.put("RootPane.background", CANVAS_DARK);
        UIManager.put("OptionPane.background", SURFACE_CARD);

        UIManager.put("Table.background", SURFACE_CARD);
        UIManager.put("Table.foreground", BODY);
        UIManager.put("Table.selectionBackground", PRIMARY_DISABLED);
        UIManager.put("Table.selectionForeground", BODY);
        UIManager.put("Table.gridColor", HAIRLINE_DARK);
        UIManager.put("TableHeader.background", SURFACE_ELEVATED);
        UIManager.put("TableHeader.foreground", MUTED);

        UIManager.put("TextField.background", SURFACE_ELEVATED);
        UIManager.put("TextField.foreground", BODY);
        UIManager.put("TextField.caretForeground", BODY);
        UIManager.put("ComboBox.background", SURFACE_ELEVATED);
        UIManager.put("ComboBox.foreground", BODY);
        UIManager.put("PasswordField.background", SURFACE_ELEVATED);

        UIManager.put("ScrollPane.background", CANVAS_DARK);
        UIManager.put("ScrollBar.thumbColor", SURFACE_ELEVATED);
        UIManager.put("ScrollBar.trackColor", SURFACE_CARD);

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
