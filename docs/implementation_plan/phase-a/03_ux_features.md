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

- [ ] En `App.java`, después de crear la `Scene`, registrar:
  ```java
  scene.addEventHandler(KeyEvent.KEY_PRESSED, KeyboardHandler::dispatch);
  ```

- [ ] Crear `util/KeyboardHandler.java` con método estático `dispatch(KeyEvent event)`:
  - Lee `SceneManager.getCurrentScreen()` para saber en qué pantalla está el usuario
  - Despacha al controlador correspondiente si el atajo aplica a esa pantalla

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

- [ ] `InventarioController` expone `openNuevoLote()`, `marcarVencido()`, `marcarRemate()`, `selectPrevRow()`, `selectNextRow()`
- [ ] `AlertasController` expone `atenderSeleccionada()`, `ignorarSeleccionada()`, `selectPrevRow()`, `selectNextRow()`
- [ ] `SceneManager` mantiene referencia al controlador activo actual; `KeyboardHandler` llama los métodos del controlador activo
- [ ] Los atajos de navegación (`Ctrl+*`) siempre funcionan, incluso desde modales — excepto si hay un campo de texto con foco (no robar eventos de escritura)
- [ ] Guardar la `compositeKey` como `new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)` usando la API de JavaFX en lugar de comparar strings

### Barra de atajos inferior

- [ ] Crear componente `HBox` fijo en la parte inferior de la ventana principal (fuera del `BorderPane` center)
- [ ] Muestra los atajos relevantes a la pantalla actual como chips: `[N] Nuevo` `[V] Vencido` `[R] Remate`
- [ ] Visible por defecto; se oculta con `?` (toggle en `SessionManager.showShortcuts: BooleanProperty`)
- [ ] Estilo: fondo `#0b0e11`, texto `#707a8a`, borde superior `#2b3139`, fuente `12px`

---

## UX 3: Dashboard de Riesgo en Tiempo Real

### GaugeCard (componente reutilizable)

- [ ] `component/GaugeCard.java` extiende `VBox`:

  ```
  VBox (.gauge-card)
  ├── Label (.gauge-title)        — "Lotes Vencidos"
  ├── HBox
  │   ├── Label (.gauge-value)    — "8" en JetBrains Mono, fuente grande
  │   └── Label (.gauge-trend)    — "↑ +2" o "↓ -1" (flecha de tendencia)
  ├── ProgressBar (.gauge-bar)    — porcentaje visual
  └── Label (.gauge-subtitle)     — "de 45 lotes activos"
  ```

- [ ] Método `setData(String titulo, int valor, int total, int tendencia)`:
  - Calcula `porcentaje = valor / total`
  - Formatea trend: `+2 → "↑ +2"`, `-1 → "↓ -1"`, `0 → "— sin cambio"`
  - Aplica la clase CSS de color correcta

- [ ] Lógica de color (aplicada dinámicamente, no hardcoded):
  ```java
  void applyColorClass(String metrica, double porcentaje) {
      getStyleClass().removeAll("safe", "warning", "danger");
      if (metrica.equals("VENCIDO") && porcentaje > 0) {
          getStyleClass().add("danger");
      } else if (metrica.equals("PROXIMO_VENCER") && porcentaje > 0.15) {
          getStyleClass().add("warning");
      } else {
          getStyleClass().add("safe");
      }
  }
  ```

### Auto-refresh

- [ ] En `DashboardController.initialize()`:
  ```java
  Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> refreshDashboard()));
  timeline.setCycleCount(Animation.INDEFINITE);
  timeline.play();
  ```

- [ ] `refreshDashboard()`:
  - En Phase A: recalcula desde `MockData.getLotes()` (mismos datos, se nota cuando se agrega un lote nuevo desde Inventario)
  - En Phase B: llama `inventarioServicio.consultarStock()` dentro de `Platform.runLater()`

### Snapshot de tendencia

- [ ] Al iniciar `DashboardController`, guardar el snapshot inicial:
  ```java
  private Map<String, Integer> snapshotAnterior = new HashMap<>();
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
