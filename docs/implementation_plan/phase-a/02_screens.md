# Fase A — Pantallas JavaFX

Construir las 6 pantallas en orden. Cada una se conecta a `MockData`; ninguna toca servicios reales.

Estructura FXML de referencia para todas las pantallas que tienen sidebar:
```
BorderPane (root)
├── top: TopBar (HBox con logo + nombre usuario + logout)
├── left: Sidebar (VBox con nav items)
└── center: contenido de la pantalla
```

---

## Pantalla 1: Login

**Archivo:** `fxml/login.fxml` + `controller/LoginController.java`

- [ ] Layout: `StackPane` con fondo `#0b0e11` ocupando toda la ventana
- [ ] Tarjeta centrada (`VBox`, clase CSS `.card`, ancho ~400px):
  - Logo / nombre de la app (`Label` grande, fuente `Inter`, color `#eaecef`)
  - Campo `Email` (`TextField`)
  - Campo `Contraseña` (`PasswordField`)
  - Botón `Iniciar Sesión` (clase `.btn-primary`, ancho completo)
  - Label de error (oculto por defecto, aparece en rojo si login falla)
- [ ] LoginController:
  - Credenciales aceptadas en Phase A:
    - `operario@plazavea.com` / `admin` → redirige como OPERARIO
    - `supervisor@plazavea.com` / `admin` → redirige como SUPERVISOR
  - Cualquier otra combinación muestra el label de error
  - Al éxito: guardar el usuario en `SessionManager` y navegar a `"dashboard"`
- [ ] Teclado: `Enter` en el campo de contraseña dispara el login

---

## Pantalla 2: Dashboard

**Archivo:** `fxml/dashboard.fxml` + `controller/DashboardController.java`

- [ ] Layout principal: `BorderPane` con sidebar a la izquierda
- [ ] Área central: `GridPane` 2×2 con 4 `GaugeCard` (ver component abajo)
- [ ] `DashboardController.initialize()`:
  - Llama `MockData.getLotes()` y calcula:
    - Total lotes activos (todos excepto RETIRADO)
    - Cantidad y % de PROXIMO_VENCER
    - Cantidad y % de VENCIDO
    - Mermas del día (hardcoded `3` en mock)
  - Pasa los valores a los 4 `GaugeCard`
  - Aplica clase CSS de color según umbral:
    - `% VENCIDO > 0%` → clase `.danger` en la tarjeta VENCIDO
    - `% PROXIMO_VENCER > 15%` → clase `.warning` en esa tarjeta
    - Resto → clase `.safe`
- [ ] Crear `component/GaugeCard.java` (custom control):
  - Extiende `VBox`
  - Contiene: `Label titulo`, `Label valorGrande` (JetBrains Mono), `ProgressBar`, `Label tendencia`
  - Método `setData(String titulo, double valor, double porcentaje, String tendencia)`
  - Método `setColorClass(String cssClass)` — agrega/quita `.safe` / `.warning` / `.danger`
- [ ] Auto-refresh stub: `Timeline` cada 60 segundos llama `refreshDashboard()` (recalcula desde MockData — sin cambios visibles en mock pero el mecanismo queda listo)

---

## Pantalla 3: Inventario

**Archivo:** `fxml/inventario.fxml` + `controller/InventarioController.java`

- [ ] Layout: `BorderPane` con sidebar; área central tiene:
  - Toolbar superior: `TextField` de búsqueda + botón `Nuevo Lote` (clase `.btn-primary`)
  - `TableView<Lote>` que ocupa el resto del espacio
- [ ] Columnas de la tabla:
  | Columna | Campo | Ancho |
  |---------|-------|-------|
  | Producto | `nombreProducto` | flex |
  | N° Lote | `numeroLote` | 100px |
  | Categoría | `categoria` | 120px |
  | Cantidad | `cantidadActual` (JetBrains Mono) | 90px |
  | Vencimiento | `fechaVencimiento` (formato `dd/MM/yyyy`) | 120px |
  | Días | `getDiasParaVencer()` (JetBrains Mono, color por valor) | 80px |
  | Estado | `estado` (badge con color CSS) | 130px |
  | Acciones | botones `V` `R` | 90px |
- [ ] Color de fila: `rowFactory` aplica clase `.row-safe`, `.row-warning` o `.row-danger` según `estado`
- [ ] Búsqueda: `FilteredList` que filtra `nombreProducto` y `numeroLote` con `StringProperty` del TextField
- [ ] `InventarioController.initialize()`: carga `MockData.getLotes()` en la `FilteredList`
- [ ] Botón `Nuevo Lote`: abre `nuevo-lote.fxml` como modal (`Stage` con `Modality.APPLICATION_MODAL`)
- [ ] Al cerrar el modal: refrescar la tabla (el mock actualiza la lista observable)

---

## Pantalla 4: Nuevo Lote (modal)

**Archivo:** `fxml/nuevo-lote.fxml` + `controller/NuevoLoteController.java`

- [ ] Layout: `VBox` con padding `24px`, fondo `#1e2329`
- [ ] Campos del formulario (en orden de tab):
  1. `Producto` — `ComboBox` con nombres de productos de MockData
  2. `N° Lote` — `TextField`
  3. `Cantidad inicial` — `TextField` (solo números)
  4. `Ubicación` — `TextField`
  5. `Fecha de vencimiento` — `TextField` + `Label` de preview debajo (ver `03_ux_features.md`)
- [ ] Botones: `Guardar` (`.btn-primary`) y `Cancelar` (`.btn-secondary`)
- [ ] `NuevoLoteController`:
  - Validación básica: campos vacíos muestran borde rojo en el campo correspondiente
  - Al guardar: crear nuevo `Lote` POJO, agregarlo a `MockData.getLotes()` (la lista es `ObservableList`, la tabla se actualiza automáticamente), cerrar modal
  - `Esc` cierra el modal (sin guardar)
- [ ] Tab order correcto: `Tab` avanza de campo en campo sin usar el mouse

---

## Pantalla 5: Alertas

**Archivo:** `fxml/alertas.fxml` + `controller/AlertasController.java`

- [ ] Layout: `BorderPane` con sidebar; área central:
  - Header: título `Alertas Pendientes` + badge con conteo
  - `TableView<Alerta>` o `ListView` con tarjetas de alerta
- [ ] Columnas / info por alerta:
  | Columna | Descripción |
  |---------|-------------|
  | Tipo | Badge `VENCIDO` (rojo) o `PRÓXIMO A VENCER` (naranja) |
  | Producto / Lote | Nombre descriptivo del lote |
  | Días para vencer | Número destacado en JetBrains Mono |
  | Estado | `PENDIENTE` / `ATENDIDA` / `IGNORADA` |
  | Acciones | Botones `Atender` y `Ignorar` |
- [ ] `AlertasController.initialize()`: carga `MockData.getAlertas()`, filtra solo `PENDIENTE` por defecto
- [ ] Toggle para mostrar también las atendidas/ignoradas
- [ ] Al `Atender`: cambia el estado de la alerta a `ATENDIDA` en la lista observable
- [ ] Al `Ignorar`: cambia el estado a `IGNORADA`
- [ ] Los atajos `V` e `I` (Ignorar) sobre la fila seleccionada deben funcionar aquí (ver `03_ux_features.md`)

---

## Pantalla 6: Reportes (solo Supervisor)

**Archivo:** `fxml/reportes.fxml` + `controller/ReportesController.java`

- [ ] El ítem de Reportes en el sidebar solo es visible cuando `SessionManager.getCurrentUser().getRol() == SUPERVISOR`
- [ ] Layout: `BorderPane` con sidebar; área central:
  - Selector de tipo de reporte: `ComboBox<TipoReporte>`
  - Rango de fechas: dos `DatePicker` (Desde / Hasta)
  - Botón `Generar Reporte` (`.btn-primary`)
  - Área de resultados: `TableView` o `TextArea` con datos de mock
- [ ] `ReportesController`:
  - Al generar: mostrar datos ficticios que varíen según el tipo seleccionado (hardcoded en MockData)
  - Botón `Exportar CSV` (deshabilitado en Phase A, habilitado en Phase B)
  - Mostrar mensaje `"Esta función estará disponible en la versión completa"` si operario intenta acceder directamente (navegando manualmente)

---

## Sidebar y Navegación

- [ ] Crear componente de sidebar como `VBox` reutilizable (incluido en cada FXML o extraído como include)
- [ ] Nav items:
  ```
  [G] Dashboard      (Ctrl+G)
  [I] Inventario     (Ctrl+I)
  [A] Alertas        (Ctrl+A)
  [R] Reportes       (Ctrl+R) — oculto para OPERARIO
  ```
- [ ] El ítem activo tiene clase `.nav-item.active` (texto blanco en lugar de muted)
- [ ] Footer del sidebar: nombre del usuario + rol + botón `Cerrar Sesión`
- [ ] `SceneManager.navigate()` actualiza el estado activo del sidebar

---

## Verificación Phase A — Pantallas

- [ ] Login con `operario@plazavea.com / admin` → Dashboard visible, sin tab Reportes en sidebar
- [ ] Login con `supervisor@plazavea.com / admin` → Dashboard con tab Reportes visible
- [ ] Login con credenciales incorrectas → Label de error visible, no navega
- [ ] Dashboard muestra 4 gauges con colores correctos (al menos 1 naranja y 1 rojo con los datos mock)
- [ ] Inventario carga 6 lotes con colores de fila correctos
- [ ] Búsqueda en inventario filtra por nombre de producto
- [ ] Modal "Nuevo Lote" abre y cierra; al guardar aparece en la tabla
- [ ] Alertas muestra 3 alertas pendientes; Atender/Ignorar cambia el estado
- [ ] Reportes visibles para Supervisor; ComboBox de tipo funciona
- [ ] Navegación con sidebar funciona entre todas las pantallas
