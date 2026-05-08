# Fase A — Pantallas Swing

Construir las 6 pantallas en orden. Cada una se conecta a `MockData`; ninguna toca servicios reales.

Estructura Swing de referencia para pantallas con sidebar (ya manejado por `MainFrame`):

```
MainFrame (JFrame)
├── WEST:   SidebarPanel (JPanel, 200px)
├── CENTER: CardLayout content area
│           ├── "login"      → LoginPanel
│           ├── "dashboard"  → DashboardPanel
│           ├── "inventario" → InventarioPanel
│           ├── "alertas"    → AlertasPanel
│           └── "reportes"   → ReportesPanel
└── SOUTH:  ShortcutBar (JPanel, 28px)
```

> `LoginPanel` se muestra antes de agregar el sidebar al frame. Al autenticar, `MainFrame` construye la vista con sidebar y navega a "dashboard".

---

## Pantalla 1: Login (`ui/LoginPanel.java`)

- [ ] Layout: `JPanel` con `GridBagLayout` para centrar una tarjeta
- [ ] Fondo del panel: `Theme.CANVAS_DARK`
- [ ] Tarjeta centrada (`JPanel`, fondo `Theme.SURFACE_CARD`, radio `Theme.RADIUS_XL`, ancho ~400px):
  - Wordmark / logo (`JLabel`, fuente `Fonts.inter(BOLD, 22f)`, color `Theme.PRIMARY`)
  - Campo `Usuario` (`JTextField`, estilo del `DESIGN.md`)
  - Campo `Contraseña` (`JPasswordField`)
  - Botón `Iniciar Sesión` (`Buttons.primaryButton(...)`, ancho completo)
  - `JLabel` de error (invisible por defecto, `Theme.DANGER` al fallar)
- [ ] `LoginPanel` lógica:
  - Credenciales aceptadas en Phase A:
    - `operario@plazavea.com` / `admin` → navega como OPERARIO
    - `supervisor@plazavea.com` / `admin` → navega como SUPERVISOR
  - Cualquier otra combinación muestra el label de error
  - Al éxito: guardar usuario en `SessionManager`, llamar `Navigator.show("dashboard")`
- [ ] Teclado: `Enter` en cualquier campo del formulario dispara el login (via `InputMap`/`ActionMap` sobre el `getRootPane()`)
- [ ] La tarjeta usa `paintComponent` custom con `fillRoundRect` para el radio de esquina

---

## Pantalla 2: Dashboard (`ui/DashboardPanel.java`)

- [ ] Layout: `BorderLayout`. NORTH: toolbar (timestamp + refresh). CENTER: contenido del dashboard
- [ ] Contenido: `JPanel` con `GridLayout(2, 2, Theme.SP_LG, Theme.SP_LG)` con 4 `GaugeCard`
- [ ] Debajo de los gauges (en un `JSplitPane` o segundo `JPanel`): `JTable` con los 10 lotes más urgentes
- [ ] `DashboardPanel` inicialización:
  - Llama `MockData.getLotes()` y calcula:
    - Total lotes activos (todos excepto RETIRADO)
    - Cantidad y % de PROXIMO_VENCER
    - Cantidad y % de VENCIDO
    - Mermas del día (hardcoded `3` en mock)
  - Pasa los valores a los 4 `GaugeCard.setData()`
  - Llama `GaugeCard.applyState(GaugeState)` según umbrales:
    - `% VENCIDO > 0` → `DANGER`
    - `% PROXIMO_VENCER > 15%` → `WARNING`
    - Resto → `SAFE`
- [ ] Crear `component/GaugeCard.java` (ver `DESIGN.md` para implementación completa):
  - Extiende `JPanel`, pinta fondo redondeado en `paintComponent`
  - Contiene: `JLabel titulo`, `JLabel valorGrande` (mono 32px), `JProgressBar`, `JLabel tendencia`
  - Método `setData(String titulo, int valor, int total, int trend)`
  - Método `applyState(GaugeState)` — cambia `setForeground` del valor y `setForeground` de la barra
- [ ] Auto-refresh: `javax.swing.Timer` cada 60 segundos llama `refreshDashboard()`:
  ```java
  Timer timer = new Timer(60_000, e -> refreshDashboard());
  timer.setRepeats(true);
  timer.start();
  ```
  Detener el timer cuando el panel se oculte (override `setVisible` o listener en `MainFrame`)

---

## Pantalla 3: Inventario (`ui/InventarioPanel.java`)

- [ ] Layout: `BorderLayout`
  - NORTH: toolbar (`JPanel`, `BorderLayout`):
    - WEST: `Buttons.primaryButton("＋  Nuevo Lote  [N]")`
    - CENTER/EAST: `JTextField` búsqueda + `JComboBox` categoría + `JComboBox` estado
  - CENTER: `JScrollPane` que envuelve el `JTable`
- [ ] Crear `LoteTableModel extends AbstractTableModel`:
  - Almacena `List<Lote>` internamente
  - Columnas: Producto · N° Lote · Categoría · Cantidad · Vencimiento · Días · Estado · Acciones
  - `setData(List<Lote>)` + `fireTableDataChanged()`
  - `getLoteAt(int row)` para acceso en renderers y shortcuts

  | Columna | Campo | Ancho | Renderer |
  |---------|-------|-------|----------|
  | Producto | `nombreProducto` | flex | default (con borde izquierdo de color) |
  | N° Lote | `numeroLote` | 100px | default |
  | Categoría | `categoria` | 120px | default |
  | Cantidad | `cantidadActual` | 90px | `MonoCellRenderer` (right-aligned) |
  | Vencimiento | `fechaVencimiento` (`dd/MM/yyyy`) | 120px | `MonoCellRenderer` |
  | Días | `getDiasParaVencer()` | 80px | `MonoCellRenderer` con color semántico |
  | Estado | `estado` | 130px | `StatusChipRenderer` |
  | Acciones | — | 90px | `ActionButtonRenderer` (botones V · R) |

- [ ] Row coloring via `prepareRenderer` override en la instancia de `JTable`:
  - VENCIDO → `setForeground(Theme.DANGER)`
  - PROXIMO_VENCER → `setForeground(Theme.WARNING)`
  - Resto → `setForeground(Theme.BODY)`
  - Primera celda de cada fila con `MatteBorder(0, 3, 0, 0, statusColor)` para el acento izquierdo

- [ ] Búsqueda: `DocumentListener` en el `JTextField` que llama `model.setData(filtered)` donde `filtered` filtra por `nombreProducto.contains(query)` y `numeroLote.contains(query)`

- [ ] Botón "Nuevo Lote": abre `new NuevoLoteDialog(parentFrame)`, luego `dialog.setVisible(true)`. Al retornar (diálogo es modal), `model.setData(MockData.getLotes())` para refrescar

- [ ] Shortcuts locales via `InputMap`/`ActionMap` en el panel:
  - `N` → abrir `NuevoLoteDialog`
  - `V` → `marcarVencido()` en la fila seleccionada
  - `R` → `marcarRemate()` en la fila seleccionada

---

## Pantalla 4: Nuevo Lote (`ui/NuevoLoteDialog.java`)

- [ ] Extiende `JDialog`, modal (`ModalityType.APPLICATION_MODAL`), `setResizable(false)`
- [ ] Tamaño: 480×520px, centrado en el parent frame
- [ ] Fondo: `Theme.SURFACE_CARD`
- [ ] Layout del contenido (ver `DESIGN.md` para el patrón completo):
  - Header (`JPanel`, borde inferior hairline): título "Nuevo Lote" + botón `×`
  - Body (`JPanel` con `MigLayout`):
    1. Producto — `JComboBox` con nombres de `MockData.getLotes()` únicos
    2. N° Lote — `JTextField`
    3. Cantidad inicial — `JTextField` (solo acepta dígitos via `DocumentFilter`)
    4. Ubicación — `JTextField`
    5. Fecha de vencimiento — `JTextField` + `JLabel` preview debajo (ver `03_ux_features.md`)
  - Footer (`JPanel`, borde superior hairline): `Cancelar` secondary + `Registrar` primary (alineados a la derecha)
- [ ] `NuevoLoteDialog` lógica:
  - Validación básica: campos vacíos muestran borde `Theme.DANGER` en el campo
  - Al guardar: crear `Lote` POJO, agregar a `MockData.getLotes()`, llamar `dispose()`
  - Tab order correcto: `Tab` avanza campo a campo (Swing maneja esto automáticamente con `setFocusCycleRoot`)
- [ ] `Esc` cierra el diálogo (registrar en `getRootPane().getInputMap()`)
- [ ] `Enter` en el último campo o cuando el botón "Registrar" tiene foco → guarda

---

## Pantalla 5: Alertas (`ui/AlertasPanel.java`)

- [ ] Layout: `BorderLayout`
  - NORTH: toolbar con título "Alertas Pendientes" + badge `JLabel` con conteo + toggle "Mostrar todas"
  - CENTER: `JScrollPane(JTable)`
- [ ] Crear `AlertaTableModel extends AbstractTableModel`

  | Columna | Descripción |
  |---------|-------------|
  | Tipo | `StatusChipRenderer` — VENCIDO (rojo) o PRÓXIMO A VENCER (naranja) |
  | Producto / Lote | Texto descriptivo |
  | Días para vencer | `MonoCellRenderer`, color semántico |
  | Estado | PENDIENTE / ATENDIDA / IGNORADA |
  | Acciones | `ActionButtonRenderer` — "Atender" + "Ignorar" |

- [ ] `AlertasPanel` inicialización: carga `MockData.getAlertas()`, filtra solo `PENDIENTE` por defecto
- [ ] Toggle "Mostrar todas": `JCheckBox` o `JToggleButton` en el toolbar, al cambiar llama `model.setData(filtered)`
- [ ] Al "Atender": `alerta.setEstado(ATENDIDA)`, `model.fireTableRowsUpdated(row, row)`
- [ ] Al "Ignorar": `alerta.setEstado(IGNORADA)`, `model.fireTableRowsUpdated(row, row)`
- [ ] Shortcuts locales:
  - `V` → atender la fila seleccionada
  - `I` → ignorar la fila seleccionada

---

## Pantalla 6: Reportes (`ui/ReportesPanel.java`) — solo Supervisor

- [ ] El nav item de Reportes en `SidebarPanel` se oculta (`setVisible(false)`) cuando `SessionManager.getCurrentUser().getRol() != SUPERVISOR`
- [ ] Layout: `JSplitPane(HORIZONTAL_SPLIT)`:
  - Left (`JPanel`, `BoxLayout Y_AXIS`, 240px): filtros
    - `JLabel` "Tipo de Reporte"
    - `JComboBox<TipoReporte>`
    - `JLabel` "Desde" + `JFormattedTextField` (fecha)
    - `JLabel` "Hasta" + `JFormattedTextField` (fecha)
    - `Buttons.primaryButton("Generar Reporte")`
    - `Buttons.secondaryButton("Exportar CSV")` (deshabilitado en Phase A)
  - Right (`JScrollPane` wrapping `JTable` o `JTextArea`): área de resultados
- [ ] Al generar: mostrar datos ficticios hardcoded en `MockData` que varíen según `TipoReporte`
- [ ] Botón CSV deshabilitado en Phase A; al clic mostrar `JOptionPane.showMessageDialog` con "Esta función estará disponible en la versión completa"
- [ ] Si un Operario navega directamente a este panel: mostrar mensaje de acceso denegado en lugar del contenido

---

## Verificación Phase A — Pantallas

- [ ] Login con `operario@plazavea.com / admin` → Dashboard visible, sin tab Reportes en sidebar
- [ ] Login con `supervisor@plazavea.com / admin` → Dashboard con tab Reportes visible
- [ ] Login con credenciales incorrectas → label de error visible, no navega
- [ ] Dashboard muestra 4 gauges con colores correctos (al menos 1 naranja y 1 rojo con los datos mock)
- [ ] Inventario carga 6 lotes con colores de fila correctos
- [ ] Búsqueda en inventario filtra por nombre de producto
- [ ] Modal "Nuevo Lote" abre y cierra; al guardar aparece en la tabla
- [ ] Alertas muestra 3 alertas pendientes; Atender/Ignorar cambia el estado
- [ ] Reportes visibles para Supervisor; `JComboBox` de tipo funciona
- [ ] Navegación con sidebar funciona entre todas las pantallas
- [ ] `Esc` en cualquier diálogo lo cierra sin guardar
