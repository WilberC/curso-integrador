## Overview

The Plaza Vea Perecibles desktop app is a **light-first, editorial inventory tool** built with **Java Swing + FlatLaf**. The visual language borrows the clarity of professional print interfaces — white canvas, ink-black type, generous whitespace, and a near-black CTA — applied to inventory management. The result should feel like a precise, legible instrument: easy to read at a glance, calm under pressure, and authoritative without decoration.

**Why Swing + FlatLaf over JavaFX:** JavaFX's styling model is CSS-driven (designed for rich internet apps), which naturally produces web-like results. Swing renders through Java2D against the OS painting model — window chrome, scrollbars, focus rings, and system dialogs are all OS-native. FlatLaf (used by IntelliJ IDEA, DataGrip, and other JetBrains tools) layers a modern flat light theme on top while keeping that native character.

**Platform reality:** 1080p or 1440p workstation monitor. No touch, no responsive breakpoints. Mouse + keyboard is the primary input model; keyboard-first operation is a first-class goal.

**Atmosphere:** White editorial canvas (`#ffffff`) with ink-black type (`#181d26`) and generous whitespace. Visual emphasis comes from color contrast and status semantics — not from dark surfaces, saturated gradients, or background decoration.

**Font split:**
- **Inter** — all labels, headings, body copy, button text.
- **JetBrains Mono** — every number: quantities, days-to-expiry, percentages, dates in table cells, gauge values. Mono makes columns scannable at a glance.

---

## Color Constants — `theme/Theme.java`

Define all colors as `java.awt.Color` constants in one class. Never hardcode hex values anywhere else in the codebase.

```java
package pe.plazavea.perecibles.theme;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import java.awt.*;

public final class Theme {

    // ── Canvas (light surfaces) ─────────────────────────────────
    public static final Color CANVAS          = hex("#ffffff"); // window background floor
    public static final Color SURFACE_SOFT    = hex("#f8fafc"); // sidebar, panels, table bg
    public static final Color SURFACE_STRONG  = hex("#e0e2e6"); // toolbar, section headers, dividers

    // ── Brand / Primary ─────────────────────────────────────────
    public static final Color PRIMARY         = hex("#181d26"); // near-black — CTA button, active nav bar
    public static final Color PRIMARY_ACTIVE  = hex("#0d1218"); // button press state
    public static final Color PRIMARY_DISABLED= hex("#c8cdd4"); // disabled button bg
    public static final Color ON_PRIMARY      = hex("#ffffff"); // text ON dark buttons and surfaces

    // ── Stock Semantics ─────────────────────────────────────────
    public static final Color SAFE            = hex("#006400"); // DISPONIBLE
    public static final Color WARNING         = hex("#b45309"); // PROXIMO_VENCER
    public static final Color DANGER          = hex("#aa2d00"); // VENCIDO

    // ── Semantic tints (light bg chip) ───────────────────────────
    public static final Color SAFE_TINT       = hex("#dcfce7"); // chip bg for DISPONIBLE
    public static final Color WARNING_TINT    = hex("#fef3c7"); // chip bg for PROXIMO_VENCER
    public static final Color DANGER_TINT     = hex("#fee2e2"); // chip bg for VENCIDO

    // ── Text ─────────────────────────────────────────────────────
    public static final Color INK             = hex("#181d26"); // strongest text — headings, primary buttons
    public static final Color BODY            = hex("#333840"); // default running text
    public static final Color MUTED           = hex("#41454d"); // captions, secondary labels
    public static final Color MUTED_STRONG    = hex("#9297a0"); // column headers, hints
    public static final Color ON_DARK         = hex("#ffffff"); // text on dark surfaces (PRIMARY bg)

    // ── Borders ──────────────────────────────────────────────────
    public static final Color HAIRLINE        = hex("#dddddd"); // dividers, input outlines
    public static final Color BORDER_STRONG   = hex("#9297a0"); // strong borders, disabled secondary outlines
    public static final Color FOCUS_RING      = hex("#458fff"); // keyboard focus outline (blue, visible on white)

    // ── Radius (use in paintComponent / setBorder) ───────────────
    public static final int RADIUS_SM = 4;
    public static final int RADIUS_MD = 6;
    public static final int RADIUS_LG = 8;
    public static final int RADIUS_XL = 12;

    // ── Spacing ─────────────────────────────────────────────────
    public static final int SP_XXS = 4;
    public static final int SP_XS  = 8;
    public static final int SP_SM  = 12;
    public static final int SP_MD  = 16;
    public static final int SP_LG  = 24;
    public static final int SP_XL  = 32;

    private Theme() {}

    private static Color hex(String h) {
        return Color.decode(h);
    }

    /** Call once at JVM start — before any Swing component is created. */
    public static void apply() {
        FlatIntelliJLaf.setup();

        UIManager.put("Panel.background",               CANVAS);
        UIManager.put("RootPane.background",            CANVAS);
        UIManager.put("OptionPane.background",          SURFACE_SOFT);

        UIManager.put("Table.background",               SURFACE_SOFT);
        UIManager.put("Table.foreground",               BODY);
        UIManager.put("Table.selectionBackground",      new Color(0xe8eaed));
        UIManager.put("Table.selectionForeground",      INK);
        UIManager.put("Table.gridColor",                HAIRLINE);
        UIManager.put("TableHeader.background",         SURFACE_STRONG);
        UIManager.put("TableHeader.foreground",         MUTED);

        UIManager.put("TextField.background",           CANVAS);
        UIManager.put("TextField.foreground",           INK);
        UIManager.put("TextField.caretForeground",      INK);
        UIManager.put("ComboBox.background",            CANVAS);
        UIManager.put("ComboBox.foreground",            INK);
        UIManager.put("PasswordField.background",       CANVAS);

        UIManager.put("ScrollPane.background",          CANVAS);
        UIManager.put("ScrollBar.thumbColor",           SURFACE_STRONG);
        UIManager.put("ScrollBar.trackColor",           SURFACE_SOFT);

        UIManager.put("Component.arc",                  RADIUS_MD);
        UIManager.put("Button.arc",                     RADIUS_MD);
        UIManager.put("TextComponent.arc",              RADIUS_MD);

        UIManager.put("Component.focusColor",           FOCUS_RING);
        UIManager.put("Component.focusWidth",           2);
    }
}
```

---

## Typography — Font Loading

Load Inter and JetBrains Mono from the classpath at startup. Both font files ship in `src/main/resources/fonts/`.

```java
public final class Fonts {
    public static Font INTER;
    public static Font JETBRAINS_MONO;

    public static void load() {
        INTER         = load("/fonts/Inter-Regular.ttf");
        JETBRAINS_MONO = load("/fonts/JetBrainsMono-Regular.ttf");
        // Register so Swing can find them by name
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(INTER);
        ge.registerFont(JETBRAINS_MONO);
    }

    public static Font inter(int style, float size) {
        return INTER.deriveFont(style, size);
    }

    public static Font mono(int style, float size) {
        return JETBRAINS_MONO.deriveFont(style, size);
    }

    private static Font load(String path) {
        try (var is = Fonts.class.getResourceAsStream(path)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, 13);
        }
    }
}
```

### Size Scale

| Role | Size | Weight | Java constant |
|---|---|---|---|
| `screen-title` | 20f | BOLD | `inter(Font.BOLD, 20f)` |
| `card-title` | 15f | BOLD | `inter(Font.BOLD, 15f)` |
| `body` | 13f | PLAIN | `inter(Font.PLAIN, 13f)` |
| `caption` | 11f | PLAIN | `inter(Font.PLAIN, 11f)` |
| `number-lg` | 32f | BOLD | `mono(Font.BOLD, 32f)` |
| `number-md` | 14f | PLAIN | `mono(Font.PLAIN, 14f)` |
| `number-sm` | 12f | PLAIN | `mono(Font.PLAIN, 12f)` |
| `button-label` | 13f | BOLD | `inter(Font.BOLD, 13f)` |
| `nav-label` | 13f | PLAIN | `inter(Font.PLAIN, 13f)` |
| `shortcut-hint` | 11f | PLAIN | `inter(Font.PLAIN, 11f)` |

**Rules:**
- Numbers always render in `JETBRAINS_MONO`. This is non-negotiable — proportional fonts make table columns shift.
- Never use font sizes below 11f on a 96dpi display.
- FlatLaf handles HiDPI scaling automatically; no manual scaling needed.

---

## Window Structure

```
┌─────────────────────────────────────────────────────────────────┐
│  JFrame title bar (OS-native window chrome)                      │
├──────────┬──────────────────────────────────────────────────────┤
│          │  Toolbar JPanel  (40px, SURFACE_STRONG)               │
│ Sidebar  ├──────────────────────────────────────────────────────┤
│ JPanel   │                                                        │
│ (200px)  │     Content JPanel — CardLayout (fills rest)          │
│          │                                                        │
│          │                                                        │
├──────────┴──────────────────────────────────────────────────────┤
│  ShortcutBar JPanel  (28px, SURFACE_STRONG)                      │
└─────────────────────────────────────────────────────────────────┘
```

Root layout in `MainFrame extends JFrame`:

```java
JPanel root = new JPanel(new BorderLayout());
root.setBackground(Theme.CANVAS);

SidebarPanel sidebar = new SidebarPanel();   // 200px fixed
JPanel center = new JPanel(new BorderLayout());
  ToolbarPanel toolbar = new ToolbarPanel(); // 40px
  JPanel cards = new JPanel(cardLayout);     // CardLayout content area
  center.add(toolbar, BorderLayout.NORTH);
  center.add(cards,   BorderLayout.CENTER);

ShortcutBar shortcutBar = new ShortcutBar(); // 28px

root.add(sidebar,      BorderLayout.WEST);
root.add(center,       BorderLayout.CENTER);
root.add(shortcutBar,  BorderLayout.SOUTH);
```

- **Sidebar:** 200px fixed (`setPreferredSize(new Dimension(200, 0))`). Background `SURFACE_SOFT`.
- **Toolbar:** 40px. Background `SURFACE_STRONG`. Bottom border hairline `HAIRLINE`.
- **Content area:** `CardLayout`. Background `CANVAS`. Panels swap in/out without full rebuilds.
- **Shortcut bar:** 28px. Background `SURFACE_STRONG`. Top border hairline `HAIRLINE`. Toggle with `?`.

---

## Layout & Spacing

### Base Unit

4px. All paddings, gaps, and insets are multiples of 4. Use `Theme.SP_*` constants.

| Constant | Value | Typical use |
|---|---|---|
| `SP_XXS` | 4px | Icon-to-label gap, badge internal padding |
| `SP_XS` | 8px | Table row vertical padding, input internal padding |
| `SP_SM` | 12px | Compact card internal padding |
| `SP_MD` | 16px | Standard panel padding, toolbar button padding |
| `SP_LG` | 24px | GaugeCard padding, section padding |
| `SP_XL` | 32px | Dialog padding |

### Layout Managers by Context

| Screen | Layout | Notes |
|---|---|---|
| Sidebar | `BoxLayout Y_AXIS` | Items stack vertically with `Box.createVerticalGlue()` between nav and user footer |
| Dashboard | `GridLayout(2, 2, SP_LG, SP_LG)` | 4 GaugeCard instances at equal size |
| Inventario / Alertas | `BorderLayout` | Toolbar (NORTH) + `JScrollPane(JTable)` (CENTER) |
| Nuevo Lote dialog | `MigLayout "insets 32, gapy 12"` | Aligns labels left, fields right in a 2-column form |
| Reportes | `JSplitPane` | Left filter panel (240px) + right preview pane |
| GaugeCard | `BoxLayout Y_AXIS` | Title → value → progress bar → trend label |

### JTable Row Height

Standard row height: **40px** (`table.setRowHeight(40)`). This gives 14px mono text comfortable breathing room and makes rows click-accurate.

---

## Elevation & Depth

Depth comes from background-color steps, never from drop shadows.

| Level | Color | Use |
|---|---|---|
| Floor | `CANVAS` (`#ffffff`) | JFrame root, default JPanel background |
| Card | `SURFACE_SOFT` (`#f8fafc`) | Sidebar, JTable background, GaugeCard |
| Elevated | `SURFACE_STRONG` (`#e0e2e6`) | Toolbar, section headers, dividers |
| Focus ring | `FOCUS_RING` (`#458fff`) | FlatLaf handles via `Component.focusColor` |
| Modal overlay | `new Color(0, 0, 0, 80)` painted on glass pane | Semi-transparent dim behind JDialog |

No custom drop shadow painting on cards. The contrast between `CANVAS` and `SURFACE_SOFT` is sufficient visual separation without shadows or dark backgrounds.

The one exception: `NuevoLoteDialog` draws a subtle shadow using FlatLaf's built-in window decoration:

```java
dialog.getRootPane().putClientProperty("FlatLaf.fullWindowContent", false);
// FlatLaf applies platform-appropriate shadow to undecorated dialogs automatically
```

---

## Shapes & Borders

### Border Radius

FlatLaf arcs are set globally via UIManager. For custom-painted panels (GaugeCard, status chips), use:

```java
// Rounded fill utility
private void fillRounded(Graphics2D g, Color color, int x, int y, int w, int h, int arc) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(color);
    g.fillRoundRect(x, y, w, h, arc, arc);
}
```

| Constant | Value | Use |
|---|---|---|
| `RADIUS_SM` | 4 | Status chips, inline badges |
| `RADIUS_MD` | 6 | Buttons, text inputs (set via UIManager globally) |
| `RADIUS_LG` | 8 | GaugeCard outer container |
| `RADIUS_XL` | 12 | Modal dialog border |

### Status Chips

Rendered by a custom `TableCellRenderer` or standalone via `StatusChip extends JLabel`:

```java
public class StatusChip extends JLabel {
    public StatusChip(EstadoLote estado) {
        setText(estado.name());
        setOpaque(false); // we paint in paintComponent
        setFont(Fonts.inter(Font.BOLD, 11f));
        switch (estado) {
            case DISPONIBLE    -> { setForeground(Theme.SAFE);    putClientProperty("bg", Theme.SAFE_TINT); }
            case PROXIMO_VENCER-> { setForeground(Theme.WARNING); putClientProperty("bg", Theme.WARNING_TINT); }
            case VENCIDO       -> { setForeground(Theme.DANGER);  putClientProperty("bg", Theme.DANGER_TINT); }
        }
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Color bg = (Color) getClientProperty("bg");
        if (bg != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_SM * 2, Theme.RADIUS_SM * 2);
        }
        super.paintComponent(g);
    }
}
```

---

## Components

### Sidebar Navigation (`SidebarPanel extends JPanel`)

- `setPreferredSize(new Dimension(200, 0))`
- Background `SURFACE_SOFT`
- Layout: `BoxLayout Y_AXIS`
- Children:
  - Wordmark area: `JPanel` 56px tall, horizontally centered `JLabel`
  - `JSeparator` hairline
  - Nav items (one per screen)
  - `Box.createVerticalGlue()`
  - User info area: name, role badge, logout button

**Nav item painting:**

```java
public class NavItem extends JPanel {
    private boolean active = false;

    public NavItem(String label, Icon icon) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(200, 48));
        setMaximumSize(new Dimension(200, 48));
        setBackground(Theme.SURFACE_SOFT);
        setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD));
        add(new JLabel(icon));
        add(Box.createHorizontalStrut(Theme.SP_SM));
        JLabel lbl = new JLabel(label);
        lbl.setFont(Fonts.inter(Font.PLAIN, 13f));
        lbl.setForeground(Theme.MUTED);
        add(lbl);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (active) {
            // 3px ink left-bar signals active screen
            g.setColor(Theme.PRIMARY);
            g.fillRect(0, 0, 3, getHeight());
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        setBackground(active ? Theme.SURFACE_STRONG : Theme.SURFACE_SOFT);
        // update label foreground
        repaint();
    }
}
```

Add hover via `MouseAdapter`:
```java
navItem.addMouseListener(new MouseAdapter() {
    public void mouseEntered(MouseEvent e) { navItem.setBackground(Theme.SURFACE_STRONG); navItem.repaint(); }
    public void mouseExited(MouseEvent e)  { if (!navItem.isActive()) { navItem.setBackground(Theme.SURFACE_SOFT); navItem.repaint(); } }
});
```

### Toolbar (`ToolbarPanel extends JPanel`)

```java
ToolbarPanel panel = new ToolbarPanel();
// setPreferredSize sets height; width is managed by BorderLayout
panel.setPreferredSize(new Dimension(0, 40));
panel.setBackground(Theme.SURFACE_STRONG);
panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE));
panel.setLayout(new BorderLayout());
// Left: action buttons
// Right: search + filter
```

### Button — Primary

FlatLaf handles the button painting. Set background and foreground colors directly:

```java
public static JButton primaryButton(String text) {
    JButton btn = new JButton(text);
    btn.setFont(Fonts.inter(Font.BOLD, 13f));
    btn.setBackground(Theme.PRIMARY);
    btn.setForeground(Theme.ON_PRIMARY);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    // FlatLaf picks up arc from UIManager Component.arc
    btn.putClientProperty("JButton.buttonType", "roundRect");
    return btn;
}
```

Hover/press states are handled by FlatLaf automatically using color derivation from `btn.getBackground()`.

### Button — Secondary

```java
public static JButton secondaryButton(String text) {
    JButton btn = new JButton(text);
    btn.setFont(Fonts.inter(Font.BOLD, 13f));
    btn.setBackground(Theme.CANVAS);
    btn.setForeground(Theme.INK);
    btn.setBorder(BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true));
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
}
```

### Button — Destructive

```java
public static JButton dangerButton(String text) {
    JButton btn = new JButton(text);
    btn.setFont(Fonts.inter(Font.BOLD, 13f));
    btn.setBackground(Theme.DANGER_TINT);
    btn.setForeground(Theme.DANGER);
    btn.setBorder(BorderFactory.createLineBorder(Theme.DANGER, 1, true));
    btn.setFocusPainted(false);
    return btn;
}
```

### JTextField & JComboBox

UIManager defaults from `Theme.apply()` handle background and foreground. Apply consistent insets with:

```java
field.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(Theme.HAIRLINE, 1, true),
    BorderFactory.createEmptyBorder(
        Theme.SP_XS, Theme.SP_SM, Theme.SP_XS, Theme.SP_SM)
));
field.setPreferredSize(new Dimension(0, 36));
```

FlatLaf shows the blue `FOCUS_RING` automatically on focus.

For validation error state:
```java
// On validation fail:
field.setBorder(BorderFactory.createLineBorder(Theme.DANGER, 1, true));
// On correction:
field.setBorder(defaultBorder);
```

### Date Input with Natural Language Preview

The `fechaVencimiento` field in `NuevoLoteDialog` has two parts:

1. **`JTextField`** — user types `"Hoy + 15"` or `"En 2 semanas"`.
2. **Preview `JLabel`** below — shows the resolved date in long format or an error.

```java
JTextField fechaField = new JTextField();
JLabel previewLabel = new JLabel(" ");
previewLabel.setFont(Fonts.mono(Font.PLAIN, 12f));

fechaField.getDocument().addDocumentListener(new DocumentListener() {
    void update(DocumentEvent e) {
        String text = fechaField.getText().trim();
        DateParser.parse(text).ifPresentOrElse(
            date -> {
                previewLabel.setText("→ " + formatLong(date));
                previewLabel.setForeground(Theme.SAFE);
            },
            () -> {
                previewLabel.setText("Fecha no reconocida");
                previewLabel.setForeground(Theme.DANGER);
            }
        );
    }
    public void insertUpdate(DocumentEvent e) { update(e); }
    public void removeUpdate(DocumentEvent e) { update(e); }
    public void changedUpdate(DocumentEvent e) { update(e); }
});
```

### JTable

The primary data component. Each content panel contains a `JTable` inside a `JScrollPane`.

```java
JTable table = new JTable(model);
table.setRowHeight(40);
table.setShowGrid(false);
table.setIntercellSpacing(new Dimension(0, 0));
table.setBackground(Theme.SURFACE_SOFT);
table.setForeground(Theme.BODY);
table.setFont(Fonts.inter(Font.PLAIN, 13f));
table.setSelectionBackground(new Color(0xe8eaed));
table.setSelectionForeground(Theme.INK);
table.getTableHeader().setBackground(Theme.SURFACE_STRONG);
table.getTableHeader().setForeground(Theme.MUTED);
table.getTableHeader().setFont(Fonts.inter(Font.PLAIN, 11f));
table.getTableHeader().setPreferredSize(new Dimension(0, 32));

// Hairline row separator
table.setShowHorizontalLines(true);
table.setGridColor(Theme.HAIRLINE);

// Wrap in scroll pane
JScrollPane scroll = new JScrollPane(table);
scroll.setBorder(BorderFactory.createEmptyBorder());
scroll.getViewport().setBackground(Theme.SURFACE_SOFT);
```

**Row coloring** via a custom renderer at the row level — override `prepareRenderer`:

```java
table = new JTable(model) {
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        Lote lote = model.getLoteAt(row);
        if (!isRowSelected(row)) {
            if (lote.getEstado() == EstadoLote.VENCIDO) {
                c.setBackground(SURFACE_SOFT);
                c.setForeground(Theme.DANGER);
            } else if (lote.getEstado() == EstadoLote.PROXIMO_VENCER) {
                c.setBackground(SURFACE_SOFT);
                c.setForeground(Theme.WARNING);
            } else {
                c.setBackground(SURFACE_SOFT);
                c.setForeground(Theme.BODY);
            }
        }
        return c;
    }
};
```

For the 3px left-border row accent, paint in a custom `TableCellRenderer` on the first column:

```java
@Override
public Component getTableCellRendererComponent(...) {
    JLabel label = (JLabel) super.getTableCellRendererComponent(...);
    label.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 3, 0, 0, statusColor(lote.getEstado())),
        BorderFactory.createEmptyBorder(0, 8, 0, 8)
    ));
    return label;
}
```

**Numeric cells** — apply mono font in the column's renderer:

```java
TableColumn cantidadCol = table.getColumnModel().getColumn(COL_CANTIDAD);
cantidadCol.setCellRenderer((tbl, val, sel, foc, row, col) -> {
    JLabel lbl = new JLabel(val.toString(), SwingConstants.RIGHT);
    lbl.setFont(Fonts.mono(Font.PLAIN, 14f));
    lbl.setForeground(sel ? Theme.INK : Theme.MUTED_STRONG);
    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
    return lbl;
});
```

### GaugeCard (`GaugeCard extends JPanel`)

```java
public class GaugeCard extends JPanel {

    private final JLabel titleLabel  = new JLabel();
    private final JLabel valueLabel  = new JLabel();
    private final JProgressBar bar   = new JProgressBar(0, 100);
    private final JLabel trendLabel  = new JLabel();

    public GaugeCard() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SURFACE_SOFT);
        setBorder(BorderFactory.createEmptyBorder(
            Theme.SP_LG, Theme.SP_LG, Theme.SP_LG, Theme.SP_LG));

        titleLabel.setFont(Fonts.inter(Font.BOLD, 14f));
        titleLabel.setForeground(Theme.MUTED);

        valueLabel.setFont(Fonts.mono(Font.BOLD, 32f));
        valueLabel.setForeground(Theme.INK);

        bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 6));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        bar.setString("");
        bar.setStringPainted(false);
        bar.setBorderPainted(false);
        bar.setBackground(Theme.SURFACE_STRONG);

        trendLabel.setFont(Fonts.inter(Font.PLAIN, 11f));
        trendLabel.setForeground(Theme.MUTED);

        add(titleLabel);
        add(Box.createVerticalStrut(Theme.SP_SM));
        add(valueLabel);
        add(Box.createVerticalStrut(Theme.SP_SM));
        add(bar);
        add(Box.createVerticalStrut(Theme.SP_XS));
        add(trendLabel);
    }

    public void setData(String titulo, int valor, int total, int trend) {
        titleLabel.setText(titulo);
        valueLabel.setText(String.valueOf(valor));
        int pct = total > 0 ? (int) ((valor * 100.0) / total) : 0;
        bar.setValue(pct);
        trendLabel.setText(trend > 0 ? "▲ +" + trend : trend < 0 ? "▼ " + trend : "— sin cambio");
    }

    public void applyState(GaugeState state) {
        Color c = switch (state) {
            case DANGER  -> Theme.DANGER;
            case WARNING -> Theme.WARNING;
            case SAFE    -> Theme.SAFE;
        };
        valueLabel.setForeground(c);
        bar.setForeground(c);
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_XL * 2, Theme.RADIUS_XL * 2);
    }
}
```

**Color thresholds:**
- `SAFE` → 0 vencidos AND < 15% próximos a vencer
- `WARNING` → ≥ 15% próximos a vencer
- `DANGER` → any `VENCIDO` lote present

### Modal Dialog (`NuevoLoteDialog extends JDialog`)

```java
public class NuevoLoteDialog extends JDialog {

    public NuevoLoteDialog(JFrame parent) {
        super(parent, "Nuevo Lote", true); // APPLICATION_MODAL
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(Theme.SURFACE_SOFT);
        getRootPane().putClientProperty("FlatLaf.style", "background: #f8fafc");
        buildLayout();
        registerEscapeKey();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE_SOFT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE_SOFT);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE),
            BorderFactory.createEmptyBorder(20, Theme.SP_LG, Theme.SP_MD, Theme.SP_LG)
        ));
        JLabel title = new JLabel("Nuevo Lote");
        title.setFont(Fonts.inter(Font.BOLD, 18f));
        title.setForeground(Theme.INK);
        header.add(title, BorderLayout.WEST);

        // Body — use MigLayout for clean form alignment
        JPanel body = new JPanel(new MigLayout("insets 24, gapy 12", "[right][grow, fill]"));
        body.setBackground(Theme.SURFACE_SOFT);
        addFormRow(body, "Producto",        new JComboBox<>());
        addFormRow(body, "N° Lote",         new JTextField());
        addFormRow(body, "Cantidad",        new JTextField());
        addFormRow(body, "Ubicación",       new JTextField());
        addFormRow(body, "Vencimiento",     buildDateRow());

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SP_XS, Theme.SP_MD));
        footer.setBackground(Theme.SURFACE_SOFT);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE));
        footer.add(Buttons.secondaryButton("Cancelar"));
        footer.add(Buttons.primaryButton("Registrar"));

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void registerEscapeKey() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }
}
```

### Shortcut Bar (`ShortcutBar extends JPanel`)

```java
public class ShortcutBar extends JPanel {

    public ShortcutBar() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Theme.SURFACE_STRONG);
        setPreferredSize(new Dimension(0, 28));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE),
            BorderFactory.createEmptyBorder(0, Theme.SP_MD, 0, Theme.SP_MD)
        ));
    }

    public void setHints(List<ShortcutHint> hints) {
        removeAll();
        for (ShortcutHint hint : hints) {
            JLabel key  = new JLabel("[" + hint.key() + "]");
            key.setFont(Fonts.mono(Font.PLAIN, 11f));
            key.setForeground(Theme.INK);

            JLabel desc = new JLabel(hint.description());
            desc.setFont(Fonts.inter(Font.PLAIN, 11f));
            desc.setForeground(Theme.MUTED);

            add(key);
            add(Box.createHorizontalStrut(4));
            add(desc);
            add(Box.createHorizontalStrut(Theme.SP_LG));
        }
        revalidate();
        repaint();
    }
}
```

### Toast Notification

Use `FlatLaf Extras` `Notifications` class:

```java
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.components.FlatLabel;
// or use ControlsFX-equivalent for Swing: custom popup

// Simple approach with FlatLaf Extras:
Notifications.getInstance()
    .withTitle("Lote registrado")
    .withText("L-007 — Leche Gloria 1L agregado al inventario")
    .withDelay(3000)
    .show();
```

---

## Keyboard Navigation

Register shortcuts via `InputMap`/`ActionMap` on the main panel and via `KeyboardFocusManager` for global shortcuts. Do **not** use `KeyListener` — it requires focus and misses events when child components are focused.

```java
// Global shortcuts via KeyboardFocusManager (registered once on startup)
KeyboardFocusManager.getCurrentKeyboardFocusManager()
    .addKeyEventDispatcher(event -> {
        if (event.getID() != KeyEvent.KEY_PRESSED) return false;
        boolean ctrl = (event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
        switch (event.getKeyCode()) {
            case KeyEvent.VK_G -> { if (ctrl) { navigator.show("dashboard"); return true; } }
            case KeyEvent.VK_I -> { if (ctrl) { navigator.show("inventario"); return true; } }
            case KeyEvent.VK_A -> { if (ctrl) { navigator.show("alertas"); return true; } }
            case KeyEvent.VK_R -> { if (ctrl && supervisor) { navigator.show("reportes"); return true; } }
            case KeyEvent.VK_SLASH -> { shortcutBar.toggle(); return true; } // '?'
            case KeyEvent.VK_F5   -> { navigator.getCurrentPanel().refresh(); return true; }
        }
        return false;
    });

// Screen-local shortcuts via InputMap on the panel
InputMap im = inventarioPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
ActionMap am = inventarioPanel.getActionMap();
im.put(KeyStroke.getKeyStroke('N'), "nuevoLote");
im.put(KeyStroke.getKeyStroke('V'), "marcarVencido");
im.put(KeyStroke.getKeyStroke('R'), "marcarRemate");
am.put("nuevoLote",    new AbstractAction() { public void actionPerformed(ActionEvent e) { openNuevoLote(); } });
am.put("marcarVencido", new AbstractAction() { public void actionPerformed(ActionEvent e) { marcarVencido(); } });
```

| Key | Action | Scope |
|---|---|---|
| `N` | Open "Nuevo Lote" dialog | Inventario |
| `V` | Mark selected row as Vencido | Inventario, Alertas |
| `R` | Mark selected row for Remate/Donación | Inventario, Alertas |
| `↑` / `↓` | Navigate JTable rows | Any JTable |
| `Enter` | Confirm / save focused form or dialog button | Dialogs, tables |
| `Esc` | Close dialog / cancel | All dialogs |
| `F5` | Refresh current screen | All screens |
| `Ctrl+G` | Navigate to Dashboard | Global |
| `Ctrl+I` | Navigate to Inventario | Global |
| `Ctrl+A` | Navigate to Alertas | Global |
| `Ctrl+R` | Navigate to Reportes (Supervisor only) | Global |
| `?` | Toggle shortcut bar | Global |

Focus indicators must always be visible. Never call `setFocusPainted(false)` on a button that can receive keyboard focus.

---

## Background Tasks — `SwingWorker`

Never perform DB operations on the Event Dispatch Thread. Use `SwingWorker`:

```java
new SwingWorker<List<Lote>, Void>() {
    @Override
    protected List<Lote> doInBackground() {
        return inventarioServicio.consultarStock(); // runs off-EDT
    }
    @Override
    protected void done() {
        try {
            List<Lote> lotes = get();
            tableModel.setData(lotes);            // safe — done() runs on EDT
        } catch (Exception ex) {
            showErrorDialog(ex.getMessage());
        }
    }
}.execute();
```

Auto-refresh via `javax.swing.Timer` (fires on EDT, safe to update UI directly):

```java
Timer timer = new Timer(60_000, e -> refreshDashboard());
timer.setRepeats(true);
timer.start();
// Stop in windowClosed listener
```

---

## Screens at a Glance

### Login (`LoginPanel extends JPanel`)
Full-window `StackPane`-equivalent: `JPanel` with `GridBagLayout` to center a `JPanel` card (400px wide). Background `CANVAS`. Wordmark `JLabel` in `INK`. `JTextField` + `JPasswordField` + primary "Iniciar Sesión" button. `JLabel` error (initially invisible). No sidebar, no toolbar.

### Dashboard (`DashboardPanel extends JPanel`)
`GridLayout(2, 2, SP_LG, SP_LG)` of 4 `GaugeCard` instances in CENTER of content area. Below (or in a split using JSplitPane): `JTable` of top-10 urgent lotes. Toolbar: current timestamp label + refresh button.

### Inventario (`InventarioPanel extends JPanel`)
`BorderLayout`. Toolbar (NORTH): `[N] Nuevo Lote` primary button, categoria `JComboBox`, estado `JComboBox`, search `JTextField`. CENTER: `JScrollPane(JTable)`. Row coloring via `prepareRenderer` override.

### Nuevo Lote (`NuevoLoteDialog extends JDialog`)
480×520 APPLICATION_MODAL dialog. MigLayout form: Producto, N° Lote, Cantidad, Ubicación, Fecha Vencimiento (with preview label). Footer: "Cancelar" secondary + "Registrar" primary.

### Alertas (`AlertasPanel extends JPanel`)
`JTable` filtered to `PROXIMO_VENCER` and `VENCIDO` rows. `V` and `I` shortcuts active. Action column with "Atender" / "Ignorar" buttons rendered via `ButtonCellRenderer`.

### Reportes (`ReportesPanel extends JPanel`)
`JSplitPane(HORIZONTAL)`: left filter `JPanel` (240px divider location), right `JScrollPane` preview. Export `JButton` in toolbar.

---

## Do's and Don'ts

### Do
- Call `Theme.apply()` and `Fonts.load()` as the very first lines of `main()`, before any `new JFrame()`.
- Use `SwingWorker` for every DB call. The EDT is for painting and event dispatch only.
- Define all colors in `Theme.java`. Never hardcode `new Color(...)` in component files.
- Apply JetBrains Mono to every numeric column via a dedicated `TableCellRenderer`.
- Use `InputMap`/`ActionMap` for keyboard shortcuts. Never use raw `KeyListener`.
- Keep `NuevoLoteDialog` at exactly 480px wide. Use `setResizable(false)` on all dialogs.
- Let FlatLaf handle button hover/press states. Only set `setBackground()` for the resting color.
- Trust whitespace and surface-step contrast (`CANVAS` → `SURFACE_SOFT` → `SURFACE_STRONG`) for depth. No drop shadows needed.
- Keep semantic status colors (`SAFE`, `WARNING`, `DANGER`) in foreground text and chip tints only — never as full panel backgrounds.

### Don't
- Don't call `setOpaque(false)` on panels unless you are custom-painting them — it causes repaint artifacts in Swing.
- Don't use `null` layout. Every panel must have an explicit `LayoutManager`.
- Don't use `GridBagLayout` for forms — use `MigLayout` instead. GridBagLayout constraints are fragile.
- Don't do DB work in `ActionListener` callbacks directly. Always delegate to a `SwingWorker`.
- Don't use fully-saturated colors. The semantic tokens (`SAFE`, `WARNING`, `DANGER`) are calibrated for legibility on a white canvas — don't substitute with raw `#ff0000` or `#00ff00`.
- Don't flip to dark surfaces (`#0b0e11` or similar) for emphasis. Emphasis comes from ink-black type, status color, and surface-step contrast — not from dark backgrounds.
- Don't make table rows fully opaque red/green. Use foreground-color + 3px left-border treatment via `prepareRenderer` / cell renderer.
- Don't call `repaint()` from a background thread. Use `SwingUtilities.invokeLater(() -> repaint())` if you must trigger it from outside the EDT.
- Don't use `JOptionPane` for routine feedback. Use the `Notifications` toast for non-blocking feedback; reserve `JOptionPane` for destructive confirmations only.
- Don't use `FlatDarkLaf`. The theme is `FlatIntelliJLaf` (light). Switching to dark breaks all `CANVAS` / `SURFACE_SOFT` / `SURFACE_STRONG` assumptions.
