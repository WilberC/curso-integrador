# Fase A — Características UX Avanzadas

Implementar los tres patrones del "Estado del Arte" con datos simulados. En Phase B solo cambia la fuente de datos, no la lógica de UX.

---

## UX 1: Parser de Fechas en Lenguaje Natural

**Clase:** `util/DateParser.java`

### Patrones soportados

| Entrada del usuario | Resultado |
|---------------------|-----------|
| `Hoy + 5` | `LocalDate.now().plusDays(5)` |
| `Hoy + 12 días` | `LocalDate.now().plusDays(12)` |
| `hoy + 1 semana` | `LocalDate.now().plusWeeks(1)` |
| `En 3 semanas` | `LocalDate.now().plusWeeks(3)` |
| `En 2 meses` | `LocalDate.now().plusMonths(2)` |
| `Próximo lunes` | siguiente lunes desde hoy |
| `Próximo viernes` | siguiente viernes |
| `15/06/2026` | `LocalDate.parse("15/06/2026", DD/MM/YYYY)` |
| `2026-06-15` | `LocalDate.parse("2026-06-15")` |

### Implementación

- [ ] Crear `util/DateParser.java` con método estático `Optional<LocalDate> parse(String input)`:

  ```java
  // Regex a implementar (en orden de prioridad):
  // 1. "Hoy + N días?"  →  plusDays(N)
  //    Pattern: (?i)hoy\s*\+\s*(\d+)(\s*d[ií]as?)?
  // 2. "En N semanas?"  →  plusWeeks(N)
  //    Pattern: (?i)en\s+(\d+)\s+semanas?
  // 3. "En N meses?"    →  plusMonths(N)
  //    Pattern: (?i)en\s+(\d+)\s+meses?
  // 4. "Próximo DIADELASEMANA"  →  nextWeekday(DayOfWeek)
  //    Pattern: (?i)pr[oó]ximo\s+(lunes|martes|miércoles|jueves|viernes|sábado|domingo)
  // 5. Fallback: DateTimeFormatter con patrones ["dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd"]
  ```

- [ ] Método privado `nextWeekday(DayOfWeek target)`:
  - Itera `LocalDate.now().plusDays(1)` hasta encontrar el primer día que coincida con `target`

- [ ] Si ningún patrón coincide, devolver `Optional.empty()`

### Integración en el formulario Nuevo Lote

- [ ] El `TextField` de fecha de vencimiento tiene un `ChangeListener` en su `textProperty()`
- [ ] Al cambiar el texto, llama `DateParser.parse(texto)` y actualiza el `Label` de preview:
  - Éxito: `"→ 15 de junio de 2026"` en color `#0ecb81`
  - Fallo: `"Fecha no reconocida"` en color `#f6465d`
- [ ] Al guardar el formulario, si el texto aún no está parseable → bloquear guardado y mostrar borde rojo
- [ ] Formatear la fecha resuelta en español usando `DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "PE"))`

---

## UX 2: Navegación Prioritaria con Teclado

### Registro de handlers globales

- [ ] En `MainFrame`, registrar un `KeyEventDispatcher` global una sola vez:
  ```java
  KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .addKeyEventDispatcher(event -> {
          if (event.getID() != KeyEvent.KEY_PRESSED) return false;
          // dispatchar según pantalla activa y tecla
          return handleGlobalKey(event);
      });
  ```

- [ ] Crear `util/KeyboardHandler.java` con método estático `handle(KeyEvent event, Navigator navigator, SessionManager session)`:
  - Lee `Navigator.getCurrentName()` para saber en qué pantalla está el usuario
  - Despacha al panel correspondiente si el atajo aplica a esa pantalla

### Tabla de atajos

| Tecla | Acción | Pantalla |
|-------|--------|----------|
| `N` | Abrir modal "Nuevo Lote" | Inventario |
| `V` | Marcar fila seleccionada como Vencido | Inventario, Alertas |
| `R` | Marcar fila para Remate / Donación | Inventario, Alertas |
| `↑` | Seleccionar fila anterior en tabla activa | Inventario, Alertas |
| `↓` | Seleccionar fila siguiente en tabla activa | Inventario, Alertas |
| `Enter` | Confirmar / guardar (en modales) | Nuevo Lote |
| `Esc` | Cerrar modal sin guardar | Cualquier modal |
| `F5` | Refrescar datos de la pantalla actual | Todas |
| `?` | Mostrar / ocultar barra de atajos inferior | Global |
| `Ctrl+G` | Navegar a Dashboard | Global |
| `Ctrl+I` | Navegar a Inventario | Global |
| `Ctrl+A` | Navegar a Alertas | Global |
| `Ctrl+R` | Navegar a Reportes (solo Supervisor) | Global |

### Implementación

- [ ] `InventarioPanel` expone `openNuevoLote()`, `marcarVencido()`, `marcarRemate()`
- [ ] `AlertasPanel` expone `atenderSeleccionada()`, `ignorarSeleccionada()`
- [ ] Screen-local shortcuts registrados via `InputMap`/`ActionMap` en cada panel:
  ```java
  InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
  im.put(KeyStroke.getKeyStroke('N'), "nuevoLote");
  panel.getActionMap().put("nuevoLote", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { openNuevoLote(); }
  });
  ```
- [ ] Los atajos de navegación global (`Ctrl+*`) se manejan en el `KeyEventDispatcher` del `MainFrame` — no en los paneles individuales
- [ ] Nunca interceptar teclas cuando el foco está en un `JTextField` o `JTextArea`:
  ```java
  Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
  if (focused instanceof JTextComponent) return false; // no robar el evento
  ```

### Barra de atajos inferior (`ShortcutBar`)

- [ ] `ShortcutBar` vive en el SOUTH del `MainFrame`, siempre presente
- [ ] `Navigator` llama `shortcutBar.setHints(hintsForScreen(name))` al navegar
- [ ] Muestra los atajos relevantes a la pantalla actual: `[N] Nuevo` `[V] Vencido` `[R] Remate`
- [ ] Visible por defecto; `toggle()` alterna visibilidad con `setVisible(!isVisible())`
- [ ] Estilo: fondo `Theme.SURFACE_ELEVATED`, texto `Theme.MUTED` para descripciones, `Theme.PRIMARY` para teclas, fuente 11px mono para la tecla

---

## UX 3: Dashboard de Riesgo en Tiempo Real

### GaugeCard (componente reutilizable)

- [ ] `component/GaugeCard.java` extiende `JPanel` (ver implementación completa en `DESIGN.md`):

  ```
  JPanel (paintComponent → fillRoundRect con SURFACE_CARD)
  ├── JLabel titulo        — "Lotes Vencidos", inter 14px bold, MUTED
  ├── JLabel valorGrande   — "8" en JetBrains Mono 32px bold, color semántico
  ├── JProgressBar         — 6px de alto, color semántico via setForeground()
  └── JLabel tendencia     — "↑ +2" o "↓ -1", inter 11px, MUTED
  ```

- [ ] Método `setData(String titulo, int valor, int total, int trend)`:
  - Calcula `porcentaje = valor / total`
  - Formatea trend: `+2 → "↑ +2"`, `-1 → "↓ -1"`, `0 → "— sin cambio"`
  - Llama `applyState(GaugeState)` según la lógica de umbral

- [ ] Lógica de color (dinámicamente, sin CSS):
  ```java
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
  ```

### Auto-refresh

- [ ] En `DashboardPanel` (al construir el panel):
  ```java
  Timer timer = new Timer(60_000, e -> refreshDashboard());
  timer.setRepeats(true);
  timer.start();
  // Detener en windowClosed o cuando el panel se oculte
  ```

- [ ] `refreshDashboard()` corre en el EDT (Timer de Swing ya corre en EDT):
  - En Phase A: recalcula desde `MockData.getLotes()` (se nota cuando se agrega un lote nuevo desde Inventario)
  - En Phase B: delegar a `SwingWorker` que llama `inventarioServicio.consultarStock()` y llama `updateGauges()` en `done()`

### Snapshot de tendencia

- [ ] Al inicializar `DashboardPanel`, guardar el snapshot inicial:
  ```java
  private final Map<String, Integer> snapshotAnterior = new HashMap<>();
  ```
- [ ] Cada ciclo de refresh compara el valor actual contra el snapshot y calcula la diferencia para la flecha de tendencia
- [ ] Actualizar snapshot después de calcular la tendencia

---

## Verificación Phase A — UX Features

- [ ] Escribir `"Hoy + 7"` en el campo de fecha → preview muestra la fecha exacta en verde
- [ ] Escribir `"texto inválido"` → preview muestra "Fecha no reconocida" en rojo
- [ ] Escribir `"Próximo viernes"` → preview muestra el viernes más próximo
- [ ] Presionar `N` en la pantalla de Inventario → se abre el modal Nuevo Lote
- [ ] Presionar `V` sobre un lote en la tabla → su estado cambia a VENCIDO
- [ ] Presionar `Ctrl+G` desde Alertas → navega a Dashboard
- [ ] Presionar `↑` / `↓` → selección de fila se mueve
- [ ] Presionar `?` → barra de atajos inferiores aparece/desaparece
- [ ] Dashboard muestra la tarjeta de VENCIDO en rojo (hay 1 lote vencido en MockData)
- [ ] Dashboard muestra la tarjeta de PROXIMO_VENCER en naranja (2 de 5 = 40% > 15%)
- [ ] Agregar un lote desde Inventario → el total del Dashboard sube 60 segundos después (o al forzar F5)
