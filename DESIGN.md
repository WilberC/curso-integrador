## Overview

The Plaza Vea Perecibles desktop app is a **dark-first, data-dense inventory tool** for store operatives. The visual language borrows the authority of financial trading UIs — near-black canvas, a single energetic accent, and monospace numerical type — and applies it to inventory management. The result should feel like a professional instrument, not a web portal ported to a window.

**Platform reality:** JavaFX on a 1080p or 1440p workstation monitor. No touch, no mobile, no responsive breakpoints. Mouse + keyboard is the primary input model; keyboard-first operation is a first-class goal (every critical action has a shortcut).

**Atmosphere:** Deep near-black surface (`-fx-canvas-dark` — `#0b0e11`) holding light-gray body text with a single accent — **Plaza Yellow** (`-fx-primary` — `#FCD535`). That yellow does all of the brand work: the primary action button, the wordmark, critical CTAs. Semantic colors handle stock status: green for safe stock, amber for near-expiry, red for expired. Nothing else competes for visual attention.

**Font split:**
- **Inter** — all labels, headings, body copy, button text.
- **JetBrains Mono** — every number: quantities, days-to-expiry counts, percentages, dates in table cells, gauge values. The mono font makes columns scannable at a glance, exactly as BinancePlex does on a trading platform.

---

## Design Tokens — JavaFX CSS

Declare all tokens as named properties in the `.root` rule of `styles.css`. Reference them with `-fx-*` properties everywhere else — never hardcode hex values outside this block.

```css
.root {
    /* ── Canvas ─────────────────────────────────────────── */
    -fx-canvas-dark:        #0b0e11;   /* window background floor */
    -fx-surface-card:       #1e2329;   /* sidebar, cards, table background */
    -fx-surface-elevated:   #2b3139;   /* toolbar, row hover, nested panels */
    -fx-surface-input:      #2b3139;   /* text field and combo box background */

    /* ── Accent ─────────────────────────────────────────── */
    -fx-primary:            #FCD535;   /* primary button bg, wordmark, active nav dot */
    -fx-primary-active:     #f0b90b;   /* button press / hover-darker */
    -fx-primary-disabled:   #3a3a1f;   /* disabled primary button */
    -fx-on-primary:         #181a20;   /* text ON yellow surfaces */

    /* ── Stock Semantics ────────────────────────────────── */
    -fx-safe:               #0ecb81;   /* DISPONIBLE — green */
    -fx-warning:            #f0a500;   /* PROXIMO_VENCER — amber */
    -fx-danger:             #f6465d;   /* VENCIDO — red */

    /* ── Text ───────────────────────────────────────────── */
    -fx-body:               #eaecef;   /* default running text on dark */
    -fx-muted:              #707a8a;   /* captions, column headers, secondary labels */
    -fx-muted-strong:       #929aa5;   /* slightly louder muted tier */
    -fx-ink:                #181a20;   /* text on light surfaces (dialogs) */
    -fx-on-dark:            #ffffff;   /* high-contrast headlines on canvas */

    /* ── Borders ─────────────────────────────────────────── */
    -fx-hairline-dark:      #2b3139;   /* dividers, table row separators on dark */
    -fx-hairline-light:     #eaecef;   /* borders on light dialog surfaces */
    -fx-focus-ring:         #FCD535;   /* keyboard focus outline */

    /* ── Border Radius ───────────────────────────────────── */
    -fx-radius-sm:          4px;       /* small badges, inline status chips */
    -fx-radius-md:          6px;       /* buttons, inputs */
    -fx-radius-lg:          8px;       /* cards, table container */
    -fx-radius-xl:          12px;      /* modal dialogs, gauge cards */

    /* ── Fonts ───────────────────────────────────────────── */
    -fx-font-family:        "Inter";
    -fx-font-mono:          "JetBrains Mono";

    /* ── Spacing scale (multiples of 4) ─────────────────── */
    /* Use as padding/gap values; not JavaFX tokens but record here for consistency */
    /* xxs=4 xs=8 sm=12 md=16 lg=24 xl=32 xxl=48 */
}
```

---

## Colors

### Canvas Layers

The app uses three elevation levels. Color alone — no drop shadows — creates the depth hierarchy.

| Token | Hex | Use |
|---|---|---|
| `-fx-canvas-dark` | `#0b0e11` | Window root background; the floor everything sits on |
| `-fx-surface-card` | `#1e2329` | Sidebar nav, content cards, TableView background |
| `-fx-surface-elevated` | `#2b3139` | Toolbar strip, hovered table rows, nested panels, input fields |

Never use a fourth canvas layer. If something needs more separation, use a hairline border, not another background color.

### Accent

**Plaza Yellow** (`-fx-primary` — `#FCD535`) is the single brand color. It appears on:
- The primary action `Button` (save, confirm, register)
- The active nav item indicator dot / left bar
- The wordmark in the title bar area
- `GaugeCard` threshold indicators when all stock is safe

It does **not** appear on:
- Table row backgrounds
- Section fill colors
- Secondary or destructive actions

### Stock Semantics

| Token | Hex | Estado | Use |
|---|---|---|---|
| `-fx-safe` | `#0ecb81` | `DISPONIBLE` | Text color on status chips and gauge bars |
| `-fx-warning` | `#f0a500` | `PROXIMO_VENCER` | Text and gauge bar color |
| `-fx-danger` | `#f6465d` | `VENCIDO` | Text, gauge bar, and row text tint |

Apply these as **text color or left-border accent on rows** — never as a full row background fill. A fully red row background destroys readability of the row's own text.

```css
/* Correct — text + left-border tint */
.row-danger {
    -fx-text-fill: -fx-danger;
    -fx-border-color: -fx-danger transparent transparent transparent;
    -fx-border-width: 0 0 0 3px;
}

/* Wrong — do not do this */
.row-danger-bad {
    -fx-background-color: -fx-danger; /* kills contrast */
}
```

### Text

| Token | Hex | Use |
|---|---|---|
| `-fx-body` | `#eaecef` | Default `Label` and `Text` on dark surfaces |
| `-fx-muted` | `#707a8a` | Column headers, captions, secondary metadata |
| `-fx-muted-strong` | `#929aa5` | Slightly louder secondary labels |
| `-fx-on-dark` | `#ffffff` | Section headings that need maximum contrast |
| `-fx-ink` | `#181a20` | Text inside light-surface dialog forms |
| `-fx-on-primary` | `#181a20` | Button label text on yellow background |

---

## Typography

### Font Assignment

```
Inter         → all Label, Button, Text, ComboBox, TextField labels
JetBrains Mono → all numeric columns in TableView, GaugeCard values,
                 date cells, quantity fields, percentage displays
```

In FXML or controller code, apply the mono family with a CSS class rather than inline styles:

```css
.mono {
    -fx-font-family: "JetBrains Mono";
}
```

### Size Scale

| Role | Size | Weight | Use |
|---|---|---|---|
| `screen-title` | 20px | 600 | Section heading at top of each content pane (e.g., "Inventario") |
| `card-title` | 15px | 600 | GaugeCard metric label, dialog section header |
| `body` | 13px | 400 | Default label text, form field labels, list items |
| `caption` | 11px | 500 | Column headers in TableView, timestamp metadata |
| `number-lg` | 32px | 700 | GaugeCard primary number (e.g., `142`) — JetBrains Mono |
| `number-md` | 14px | 500 | Table cell quantities, days-to-expiry — JetBrains Mono |
| `number-sm` | 12px | 500 | Inline percentage change labels — JetBrains Mono |
| `button-label` | 13px | 600 | Button text (primary and secondary) |
| `nav-label` | 13px | 500 | Sidebar navigation item labels |
| `shortcut-hint` | 11px | 400 | Shortcut bar hints at window bottom — muted color |

### Principles

- Numbers always render in JetBrains Mono so columns align without proportional-font jitter.
- Never drop `number-lg` below weight 600 — the gauge card value needs to read from across a room.
- Screen titles use weight 600 at 20px; they do not need 700 because they compete with data, not a hero background.
- All sizes are in `px` for JavaFX (`-fx-font-size: 13px`). JavaFX does not use `rem`.

---

## Window Structure

```
┌─────────────────────────────────────────────────────────────────┐
│  Title bar (OS-native)  — window controls handled by OS         │
├──────────┬──────────────────────────────────────────────────────┤
│          │  Toolbar strip  (40px tall, surface-elevated)         │
│ Sidebar  ├──────────────────────────────────────────────────────┤
│  nav     │                                                        │
│ (200px)  │          Content pane  (fills remaining space)        │
│          │                                                        │
│          │                                                        │
├──────────┴──────────────────────────────────────────────────────┤
│  Shortcut bar  (28px tall, surface-elevated, muted text)         │
└─────────────────────────────────────────────────────────────────┘
```

- **Sidebar:** 200px fixed width. Background `-fx-surface-card`. Holds nav items and the wordmark at top.
- **Toolbar strip:** 40px tall. Background `-fx-surface-elevated`. Contains contextual action buttons for the active screen and a search field.
- **Content pane:** Fills the remaining area. Background `-fx-canvas-dark`. Each screen's root node lives here.
- **Shortcut bar:** 28px tall strip at the window bottom. Background `-fx-surface-elevated`. Shows active keyboard shortcuts. Toggleable with `?`.

---

## Layout & Spacing

### Base Unit

4px. All paddings, gaps, and margins are multiples of 4.

| Name | Value | Typical use |
|---|---|---|
| `xxs` | 4px | Icon-to-label gap, badge internal padding |
| `xs` | 8px | Table row vertical padding, input internal padding |
| `sm` | 12px | Card internal padding (compact) |
| `md` | 16px | Standard card padding, toolbar button padding |
| `lg` | 24px | GaugeCard padding, section content padding |
| `xl` | 32px | Modal dialog padding |

### Content Pane Layout Patterns

**Dashboard** — `FlowPane` or `GridPane` of 4 `GaugeCard` instances (2×2 at 1080p). Below the cards, a summary `TableView` showing the 10 most urgent lotes.

**Inventario / Alertas** — Full-height `TableView` consuming the content pane. A filter bar sits above it (inside the toolbar strip). No extra chrome.

**Nuevo Lote modal** — `DialogPane` or custom `Stage` with `VBox` layout. Width 480px, centered on parent window. Padding `xl` (32px).

**Reportes** — Left: filter/options `VBox` (240px). Right: report preview or chart (fills remaining).

### TableView Rows

Standard row height: 40px. This gives number-md text (14px) comfortable vertical breathing room and makes rows keyboard-navigable without accidental mis-clicks.

---

## Elevation & Depth

Depth comes from background-color steps, never from drop shadows.

| Level | Treatment | JavaFX context |
|---|---|---|
| Floor | `-fx-canvas-dark` (`#0b0e11`) | Window root, pane backgrounds |
| Card | `-fx-surface-card` (`#1e2329`) | Sidebar, TableView, GaugeCard background |
| Elevated | `-fx-surface-elevated` (`#2b3139`) | Toolbar, hovered rows, focused inputs |
| Focus ring | 2px outline `-fx-primary` | Focused `TextField`, `Button`, `TableView` |
| Modal overlay | Semi-transparent `#0b0e11` at 60% opacity behind dialog | Blocks interaction with parent window |

No `dropshadow` effects on cards. If a `GaugeCard` needs to stand out from the canvas, it's because its background is `-fx-surface-card` while the canvas is `-fx-canvas-dark` — that 12-step lightness jump is sufficient.

The one exception: modal dialogs (`DialogPane` / custom `Stage`) receive a single subtle drop shadow to separate them from the dimmed background overlay:

```css
.modal-root {
    -fx-effect: dropshadow(gaussian, #000000, 24, 0.4, 0, 4);
}
```

---

## Shapes

### Border Radius

| Token | Value | Use |
|---|---|---|
| `-fx-radius-sm` | 4px | Status chips, inline badges (`VENCIDO`, `DISPONIBLE`) |
| `-fx-radius-md` | 6px | Buttons, text inputs, combo boxes |
| `-fx-radius-lg` | 8px | GaugeCard container, filter bar |
| `-fx-radius-xl` | 12px | Modal dialogs, report preview pane |

### Status Chips

Status chips are small `Label` nodes with background color derived from stock semantics. They use `-fx-radius-sm` and padding `4px 8px`.

```css
.chip-safe    { -fx-background-color: derive(-fx-safe, -70%);    -fx-text-fill: -fx-safe; }
.chip-warning { -fx-background-color: derive(-fx-warning, -70%); -fx-text-fill: -fx-warning; }
.chip-danger  { -fx-background-color: derive(-fx-danger, -70%);  -fx-text-fill: -fx-danger; }
```

`derive()` at −70% darkens the semantic color to create a low-opacity background tint without losing legibility.

---

## Components

### Sidebar Navigation

**Structure:** `VBox` with 200px `prefWidth`, `-fx-surface-card` background. Children: wordmark `HBox` (56px tall) at top, then nav item rows, then spacer, then user info row at bottom.

**Nav item — default:**
- `HBox` with 48px height, 16px horizontal padding
- FontAwesome icon (16px) + `Label` (nav-label size) with 12px gap
- Background transparent; text `-fx-muted`

**Nav item — active:**
- Background `-fx-surface-elevated`
- Left border: 3px solid `-fx-primary`
- Text `-fx-on-dark` weight 600
- Small yellow dot or the left-bar treatment — pick one, not both

**Nav item — hover:**
- Background `-fx-surface-elevated`; text `-fx-body`

```css
.nav-item { -fx-padding: 0 16px; -fx-pref-height: 48px; -fx-cursor: hand; }
.nav-item:hover { -fx-background-color: -fx-surface-elevated; }
.nav-item.active {
    -fx-background-color: -fx-surface-elevated;
    -fx-border-color: -fx-primary transparent transparent transparent;
    -fx-border-width: 0 0 0 3px;
}
.nav-item.active .nav-label { -fx-text-fill: -fx-on-dark; -fx-font-weight: 600; }
```

### Toolbar Strip

40px tall `HBox`, `-fx-surface-elevated` background, `md` (16px) horizontal padding, hairline bottom border (`-fx-hairline-dark`).

Left side: contextual `Button` group (e.g., "Nuevo Lote `N`").
Right side: search `TextField`, optional filter `ComboBox`.

### Button — Primary

The yellow action button. Used for "Guardar", "Registrar Lote", "Confirmar".

```css
.button-primary {
    -fx-background-color: -fx-primary;
    -fx-text-fill: -fx-on-primary;
    -fx-font-size: 13px;
    -fx-font-weight: 600;
    -fx-padding: 8px 20px;
    -fx-background-radius: -fx-radius-md;
    -fx-cursor: hand;
}
.button-primary:hover    { -fx-background-color: -fx-primary-active; }
.button-primary:pressed  { -fx-background-color: -fx-primary-active; -fx-scale-y: 0.98; }
.button-primary:disabled { -fx-background-color: -fx-primary-disabled; -fx-opacity: 0.6; }
.button-primary:focused  { -fx-border-color: -fx-focus-ring; -fx-border-width: 2px; -fx-border-radius: -fx-radius-md; }
```

### Button — Secondary

Used for "Cancelar", "Filtrar", less-critical actions.

```css
.button-secondary {
    -fx-background-color: -fx-surface-elevated;
    -fx-text-fill: -fx-body;
    -fx-font-size: 13px;
    -fx-font-weight: 600;
    -fx-padding: 8px 20px;
    -fx-background-radius: -fx-radius-md;
    -fx-border-color: -fx-hairline-dark;
    -fx-border-width: 1px;
    -fx-border-radius: -fx-radius-md;
    -fx-cursor: hand;
}
.button-secondary:hover   { -fx-background-color: derive(-fx-surface-elevated, 10%); }
.button-secondary:focused { -fx-border-color: -fx-focus-ring; }
```

### Button — Destructive

Used for "Marcar Vencido", "Retirar Lote" — actions that cannot be undone.

```css
.button-danger {
    -fx-background-color: derive(-fx-danger, -60%);
    -fx-text-fill: -fx-danger;
    -fx-border-color: -fx-danger;
    -fx-border-width: 1px;
    -fx-border-radius: -fx-radius-md;
    -fx-background-radius: -fx-radius-md;
    -fx-padding: 8px 20px;
    -fx-font-size: 13px;
    -fx-font-weight: 600;
}
.button-danger:hover { -fx-background-color: derive(-fx-danger, -50%); }
```

### TextField & ComboBox

```css
.text-field, .combo-box {
    -fx-background-color: -fx-surface-input;
    -fx-text-fill: -fx-body;
    -fx-prompt-text-fill: -fx-muted;
    -fx-background-radius: -fx-radius-md;
    -fx-border-color: -fx-hairline-dark;
    -fx-border-width: 1px;
    -fx-border-radius: -fx-radius-md;
    -fx-padding: 8px 12px;
    -fx-font-size: 13px;
    -fx-pref-height: 36px;
}
.text-field:focused, .combo-box:focused {
    -fx-border-color: -fx-primary;
}
.text-field.error {
    -fx-border-color: -fx-danger;
}
```

### Date Input with Natural Language Preview

The `fechaVencimiento` field in `NuevoLoteController` has two parts:

1. **`TextField`** — user types `"Hoy + 15"` or `"En 2 semanas"`. Standard text-field style above.
2. **Preview `Label`** below the field — shows the resolved date in long format (`15 de junio de 2026`) or an error in `-fx-danger` color.

```css
.date-preview {
    -fx-font-family: "JetBrains Mono";
    -fx-font-size: 12px;
    -fx-text-fill: -fx-safe;        /* resolved successfully */
    -fx-padding: 4px 0 0 0;
}
.date-preview.error {
    -fx-text-fill: -fx-danger;
}
```

### TableView

The primary data component. Full-height in most content panes.

```css
.table-view {
    -fx-background-color: -fx-surface-card;
    -fx-border-color: transparent;
    -fx-table-cell-border-color: -fx-hairline-dark;
}
.table-view .column-header {
    -fx-background-color: -fx-surface-elevated;
    -fx-text-fill: -fx-muted;
    -fx-font-size: 11px;
    -fx-font-weight: 500;
    -fx-padding: 0 12px;
    -fx-pref-height: 32px;
}
.table-row-cell {
    -fx-background-color: -fx-surface-card;
    -fx-pref-height: 40px;
    -fx-border-color: transparent transparent -fx-hairline-dark transparent;
    -fx-border-width: 0 0 1px 0;
}
.table-row-cell:hover        { -fx-background-color: -fx-surface-elevated; }
.table-row-cell:selected     { -fx-background-color: derive(-fx-primary, -75%); }
.table-row-cell.row-warning  { -fx-border-color: -fx-warning transparent transparent transparent; -fx-border-width: 0 0 0 3px; }
.table-row-cell.row-danger   { -fx-border-color: -fx-danger transparent transparent transparent; -fx-border-width: 0 0 0 3px; }

/* Numeric cells */
.table-cell.mono-cell {
    -fx-font-family: "JetBrains Mono";
    -fx-font-size: 13px;
    -fx-alignment: center-right;
}
```

Apply `.row-warning` or `.row-danger` CSS class via a `TableRow` factory in the controller:

```java
tableView.setRowFactory(tv -> new TableRow<>() {
    @Override
    protected void updateItem(Lote item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("row-warning", "row-danger");
        if (item != null) {
            if (item.getEstado() == EstadoLote.VENCIDO)          getStyleClass().add("row-danger");
            else if (item.getEstado() == EstadoLote.PROXIMO_VENCER) getStyleClass().add("row-warning");
        }
    }
});
```

### GaugeCard

`VBox` with `-fx-surface-card` background, `-fx-radius-xl` corners, `lg` (24px) padding.

Internal layout:

```
┌──────────────────────────────────┐
│  Icon   LABEL (card-title, muted)  │  ← HBox, top
│                                    │
│  142                               │  ← number-lg, JetBrains Mono, color=semantic
│                                    │
│  [████████████░░░░░░░░░]  68%      │  ← ProgressBar + Label
│                                    │
│  ▲ +12 desde inicio de sesión      │  ← trend HBox, muted text, caption size
└──────────────────────────────────┘
```

```css
.gauge-card {
    -fx-background-color: -fx-surface-card;
    -fx-background-radius: -fx-radius-xl;
    -fx-padding: 24px;
    -fx-pref-width: 220px;
    -fx-pref-height: 160px;
}
.gauge-value {
    -fx-font-family: "JetBrains Mono";
    -fx-font-size: 32px;
    -fx-font-weight: 700;
}
.gauge-value.safe    { -fx-text-fill: -fx-safe; }
.gauge-value.warning { -fx-text-fill: -fx-warning; }
.gauge-value.danger  { -fx-text-fill: -fx-danger; }

.gauge-bar > .track { -fx-background-color: -fx-surface-elevated; -fx-background-radius: 99px; }
.gauge-bar.safe  > .bar { -fx-background-color: -fx-safe; }
.gauge-bar.warning > .bar { -fx-background-color: -fx-warning; }
.gauge-bar.danger > .bar { -fx-background-color: -fx-danger; }
```

**Color thresholds (from AGENT.md):**
- Green (`safe`) → 0 vencidos AND < 15% próximos a vencer
- Amber (`warning`) → ≥ 15% próximos a vencer
- Red (`danger`) → any `VENCIDO` lote present

### Modal Dialog

Custom `Stage` (or `DialogPane`) for Nuevo Lote, confirmations, detail views.

- Width: 480px. Height: auto (min 360px).
- Background: `-fx-surface-card`; corner radius `-fx-radius-xl`.
- Parent window dimmed with semi-transparent overlay pane.
- Header `HBox`: title label (screen-title size, `-fx-on-dark`) + `×` close button (secondary style).
- Footer `HBox`: action buttons right-aligned. Primary button on far right, secondary/cancel to its left. Gap `xs` (8px).
- Body: `VBox` with form fields, `lg` (24px) padding.

```css
.modal-root {
    -fx-background-color: -fx-surface-card;
    -fx-background-radius: -fx-radius-xl;
    -fx-effect: dropshadow(gaussian, black, 24, 0.4, 0, 4);
}
.modal-header {
    -fx-padding: 20px 24px 16px 24px;
    -fx-border-color: transparent transparent -fx-hairline-dark transparent;
    -fx-border-width: 0 0 1px 0;
}
.modal-body   { -fx-padding: 24px; }
.modal-footer {
    -fx-padding: 16px 24px;
    -fx-border-color: -fx-hairline-dark transparent transparent transparent;
    -fx-border-width: 1px 0 0 0;
}
```

### Shortcut Bar

`HBox` at bottom of root layout, 28px `prefHeight`, `-fx-surface-elevated` background, `md` horizontal padding. Toggleable with `?`.

Each hint: `[KEY]` label in `-fx-primary` mono font + description label in `-fx-muted` body font. Gap between hints: `lg` (24px).

```css
.shortcut-bar { -fx-background-color: -fx-surface-elevated; -fx-pref-height: 28px; -fx-padding: 0 16px; }
.shortcut-key { -fx-font-family: "JetBrains Mono"; -fx-font-size: 11px; -fx-text-fill: -fx-primary; }
.shortcut-desc { -fx-font-size: 11px; -fx-text-fill: -fx-muted; -fx-padding: 0 0 0 4px; }
```

### Alert / Notification Toast

Non-blocking `Popup` or ControlsFX `Notifications` that appears in the top-right of the window for brief system feedback ("Lote registrado", "Alerta atendida").

- Width: 280px, padding `md` (16px), `-fx-radius-lg` corners.
- Background: `-fx-surface-elevated` with a 3px left border in the semantic color (safe/warning/danger).
- Auto-dismiss after 3 seconds.
- No sound, no modal blocking.

---

## Keyboard Navigation

Every `TableView` and form `TextField` must be fully keyboard-operable. Defined shortcuts wire to `scene.addEventFilter(KeyEvent.KEY_PRESSED, ...)` in the main controller.

| Key | Action | Scope |
|---|---|---|
| `N` | Open "Nuevo Lote" modal | Inventario screen |
| `V` | Mark selected row as Vencido | Inventario, Alertas |
| `R` | Mark selected row for Remate/Donación | Inventario, Alertas |
| `↑` / `↓` | Navigate TableView rows | Any TableView |
| `Enter` | Confirm / save focused form or selected action | Dialogs, TableView |
| `Esc` | Close modal / cancel | Modals |
| `F5` | Refresh current screen data | All screens |
| `Ctrl+G` | Navigate to Dashboard | Global |
| `Ctrl+I` | Navigate to Inventario | Global |
| `Ctrl+A` | Navigate to Alertas | Global |
| `Ctrl+R` | Navigate to Reportes (Supervisor only) | Global |
| `?` | Toggle shortcut bar visibility | Global |

Focus indicators must always be visible. Never remove the focus ring with `-fx-focus-color: transparent`.

---

## Screens at a Glance

### Login
Full-window centered `VBox`. Background `-fx-canvas-dark`. Wordmark at top (`-fx-primary`). Username + password `TextField` pair, primary "Ingresar" button. No sidebar, no toolbar.

### Dashboard
4 `GaugeCard` instances in a `GridPane` (2×2). Below: `TableView` of top-10 urgent lotes (sorted by days-to-expiry ascending). Toolbar shows current timestamp and a refresh button.

### Inventario
Full-height `TableView`. Toolbar contains `[N] Nuevo Lote` primary button, category `ComboBox` filter, estado `ComboBox` filter, and search `TextField`. Row coloring via `.row-warning` / `.row-danger` CSS classes.

### Nuevo Lote (Modal)
480px modal. Fields: Producto (ComboBox), Categoría (ComboBox), Cantidad (TextField, mono), Fecha Vencimiento (TextField with date-preview label), Notas (TextArea optional). Footer: "Cancelar" secondary + "Registrar" primary.

### Alertas
`TableView` filtered to `PROXIMO_VENCER` and `VENCIDO` lotes. `V` and `R` shortcuts active. Quick-action buttons in each row or via toolbar.

### Reportes (Supervisor only)
Split `SplitPane`: left filter panel (240px, surface-card background), right preview pane (canvas-dark). Export button (secondary style) in toolbar.

---

## Do's and Don'ts

### Do
- Use `-fx-primary` (yellow) exclusively for primary CTAs and the wordmark. Every other interactive element uses secondary or semantic colors.
- Apply stock semantic colors as **text fill or left-border accent** on rows and chips, never as full background fills.
- Use JetBrains Mono for every number that lives in a column — days, quantities, percentages, dates. This is non-negotiable for scannability.
- Show keyboard shortcut hints in the shortcut bar at all times (unless hidden by user). The app is keyboard-first.
- Keep modal dialogs at exactly 480px width. Do not create multiple width variants.
- Maintain the three-layer elevation system (canvas → card → elevated) without adding a fourth level.

### Don't
- Don't add drop shadows to cards or the sidebar. Elevation is handled by background color alone.
- Don't use `#FF0000` or fully-saturated reds/greens. The semantic tokens (`-fx-safe`, `-fx-danger`) are calibrated for the dark canvas — substituting pure colors will blow out contrast.
- Don't use yellow (`-fx-primary`) for error states or stock warnings. Yellow is a brand accent, not a semantic color.
- Don't make table rows fully opaque red/green on their backgrounds. Use the 3px left-border treatment.
- Don't use responsive layout tricks (percentage widths, fluid grids). This is a fixed desktop window, not a web page. Use `prefWidth` and `HBox.setHgrow(node, Priority.ALWAYS)` for intentional expansion only.
- Don't style `TableView` scroll bars aggressively. The OS-native or JavaFX default scrollbar is acceptable — invisible scrollbars hurt discoverability on a mouse-driven app.
- Don't remove focus rings. Keyboard users rely on them.
- Don't use font sizes below 11px. Even on 1440p displays, sub-11px labels read as decorative noise, not information.
