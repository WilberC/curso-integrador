# Sistema de Control de Productos Perecibles — Plaza Vea
## Guía de Trabajo para Desarrolladores

---

## Descripción del Proyecto

Aplicación de escritorio Swing + FlatLaf para el control de inventario de productos perecibles en tiendas Plaza Vea (Lima). Reemplaza el proceso manual de revisión física de fechas de vencimiento con un sistema que centraliza el registro de lotes, genera alertas automáticas y produce reportes de mermas. Meta: reducir pérdidas por productos vencidos en 30% durante el primer año en tiendas piloto.

Usuarios: **Operarios** (registro y movimientos) y **Supervisores** (todo lo anterior + reportes + configuración de alertas).

---

## Tech Stack

| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| Java | 25 | Lenguaje principal |
| Gradle | 9.5.0 | Build tool |
| Swing | (incluido en JDK) | UI framework — desktop nativo |
| FlatLaf | 3.6 | Look & Feel moderno oscuro (mismo L&F que IntelliJ IDEA) |
| FlatLaf Extras | 3.6 | Toast notifications, íconos SVG |
| MigLayout | 11.4.2 | Layout manager para formularios (Grid flexible) |
| Spring Data JPA | 3.x | ORM / repositorios |
| Hibernate | 6.x | Implementación JPA |
| PostgreSQL | 16 | Base de datos (via Docker) |
| dotenv-java | 3.x | Lectura de variables de entorno desde `.env` |
| Jackson | 2.x | Serialización JSON / exportación CSV |
| jpackage | (incluido en JDK 25) | Empaquetado de instalador nativo |
| Inter (font) | latest | Tipografía cuerpo / headings (archivo .ttf en resources) |
| JetBrains Mono (font) | latest | Tipografía numérica / estadísticas (archivo .ttf en resources) |

> **Herramientas de desarrollo:** Las versiones exactas de Java y Gradle están fijadas en `mise.toml`. Instalar [mise](https://mise.jdx.dev/) y ejecutar `mise install` para activarlas.

---

## Dev Setup

```bash
# 1. Activar versiones de Java/Gradle
mise install

# 2. Levantar base de datos (solo en Phase B)
cp .env.example .env          # editar contraseñas si se desea
docker-compose up -d

# 3. Compilar y ejecutar
./gradlew run

# 4. Ejecutar tests
./gradlew test

# 5. Generar instalador nativo
./gradlew jpackage
```

> **Nota Swing:** No se requiere ningún plugin adicional de Gradle para Swing — está incluido en el JDK. Solo se necesitan las dependencias de FlatLaf y MigLayout en `build.gradle`.

---

## Estructura de Paquetes

```
src/main/java/pe/plazavea/perecibles/
├── App.java                        # main() — inicia FlatLaf, carga fuentes, abre MainFrame
│
├── ui/                             # Pantallas Swing (JPanel / JDialog por pantalla)
│   ├── MainFrame.java              # JFrame raíz: sidebar + CardLayout + shortcut bar
│   ├── LoginPanel.java
│   ├── DashboardPanel.java
│   ├── InventarioPanel.java
│   ├── AlertasPanel.java
│   ├── ReportesPanel.java
│   └── NuevoLoteDialog.java        # JDialog modal para registrar lotes
│
├── model/                          # Entidades JPA (Phase B) / POJOs (Phase A)
│   ├── Usuario.java
│   ├── Categoria.java
│   ├── ProductoPerecible.java
│   ├── Lote.java
│   ├── MovimientoInventario.java
│   ├── Merma.java
│   ├── Alerta.java
│   ├── Reporte.java
│   └── ConfiguracionAlerta.java
│
├── enums/                          # 6 enumeraciones del dominio
│   ├── RolUsuario.java             # OPERARIO, SUPERVISOR
│   ├── EstadoLote.java             # DISPONIBLE, PROXIMO_VENCER, VENCIDO, RETIRADO
│   ├── TipoMovimiento.java         # INGRESO, RETIRO, AJUSTE, REMATE, DONACION
│   ├── TipoAlerta.java             # PROXIMO_VENCER, VENCIDO
│   ├── EstadoAlerta.java           # PENDIENTE, ATENDIDA, IGNORADA
│   └── TipoReporte.java            # STOCK, VENCIDOS, PROXIMOS_VENCER, MERMAS
│
├── mock/                           # Datos simulados para Phase A (eliminar en Phase B)
│   └── MockData.java
│
├── repository/                     # Spring Data JPA (Phase B)
│   ├── UsuarioRepository.java
│   ├── CategoriaRepository.java
│   ├── ProductoPerecibleRepository.java
│   ├── LoteRepository.java
│   ├── MovimientoInventarioRepository.java
│   ├── AlertaRepository.java
│   ├── ReporteRepository.java
│   └── ConfiguracionAlertaRepository.java
│
├── service/                        # Lógica de negocio (Phase B)
│   ├── UsuarioServicio.java
│   ├── InventarioServicio.java
│   ├── AlertaServicio.java
│   └── ReporteServicio.java
│
├── component/                      # Componentes Swing reutilizables
│   ├── GaugeCard.java              # extends JPanel — indicador de riesgo del dashboard
│   ├── SidebarPanel.java           # nav lateral con NavItem
│   ├── ShortcutBar.java            # barra inferior de atajos
│   └── StatusChip.java             # badge de color para EstadoLote
│
├── theme/                          # Tokens visuales y carga de fuentes
│   ├── Theme.java                  # Color constants + UIManager setup (FlatLaf)
│   └── Fonts.java                  # Carga Inter + JetBrains Mono desde resources/fonts/
│
├── util/                           # Utilidades transversales
│   ├── DateParser.java             # Parser de fechas en lenguaje natural
│   ├── Navigator.java              # CardLayout wrapper — navegar entre pantallas
│   └── SessionManager.java         # Singleton — usuario autenticado en sesión
│
└── config/                         # Configuración JPA / Spring (Phase B)
    ├── JpaConfig.java
    └── SpringContext.java          # Bridge entre Spring y Swing

src/main/resources/
├── fonts/
│   ├── Inter-Regular.ttf
│   ├── Inter-Bold.ttf
│   ├── JetBrainsMono-Regular.ttf
│   └── JetBrainsMono-Bold.ttf
└── images/
    └── (íconos PNG/SVG)
```

> **Sin FXML ni CSS:** Swing no usa archivos FXML ni CSS. La estructura visual está definida en código Java y tokens en `theme/Theme.java`. El estilo global se aplica via FlatLaf + `UIManager.put()`.

---

## Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clases entidad | `PascalCase`, singular | `ProductoPerecible` |
| Servicios | `PascalCase` + sufijo `Servicio` | `InventarioServicio` |
| Repositorios | `PascalCase` + sufijo `Repository` | `LoteRepository` |
| Paneles Swing | `PascalCase` + sufijo `Panel` | `DashboardPanel` |
| Diálogos Swing | `PascalCase` + sufijo `Dialog` | `NuevoLoteDialog` |
| Tokens de color/fuente | Constantes `UPPER_SNAKE_CASE` en `Theme.java` / `Fonts.java` | `Theme.SURFACE_CARD` |
| Variables | `camelCase` | `fechaVencimiento` |
| Constantes | `UPPER_SNAKE_CASE` | `DIAS_ALERTA_ROJA` |
| Enums | `PascalCase` clase, `UPPER_SNAKE_CASE` valores | `EstadoLote.PROXIMO_VENCER` |
| Columnas DB | `snake_case` | `fecha_vencimiento` |

**Idioma del código:** inglés para nombres técnicos (métodos, variables locales temporales). Español para nombres de dominio que el cliente usa (entidades, campos, mensajes UI).

---

## Reglas UI/UX

### Design Tokens — `theme/Theme.java`

Todos los colores y radios están definidos como constantes `public static final Color` en `Theme.java`. No hardcodear hex en ningún otro archivo.

```java
// Canvas
Theme.CANVAS_DARK      = #0b0e11   // fondo de ventana
Theme.SURFACE_CARD     = #1e2329   // sidebar, paneles, fondo de tabla
Theme.SURFACE_ELEVATED = #2b3139   // toolbar, hover, inputs

// Accent
Theme.PRIMARY          = #FCD535   // solo CTAs primarios y wordmark
Theme.ON_PRIMARY       = #181a20   // texto sobre amarillo

// Semántica de stock
Theme.SAFE             = #0ecb81   // DISPONIBLE
Theme.WARNING          = #f0a500   // PROXIMO_VENCER
Theme.DANGER           = #f6465d   // VENCIDO

// Texto
Theme.BODY             = #eaecef
Theme.MUTED            = #707a8a
```

Llamar `Theme.apply()` como **primera línea de `main()`** antes de crear cualquier componente Swing.

**Reglas de uso:**
- `Theme.PRIMARY` (amarillo) **solo** para botón de acción principal y el wordmark. Nunca para texto de cuerpo ni fondos de sección.
- Colores de stock van como `setForeground()` en celdas de tabla o como borde izquierdo de 3px en la primera celda de la fila. Nunca como fondo completo de fila.
- Fondo base siempre `CANVAS_DARK`. Tarjetas elevadas `SURFACE_CARD`. Elementos sobre tarjeta `SURFACE_ELEVATED`.
- Texto numérico (cantidades, días, porcentajes) en `JetBrains Mono` cargado con `Fonts.mono()`.

### Estado del Arte — Patrones UX a Implementar

#### 1. Lenguaje Natural para Fechas (inspirado en Soulver)
El campo de fecha de vencimiento acepta texto libre. El parser lo convierte en tiempo real.

Ejemplos soportados:
- `"Hoy + 5"` → `LocalDate.now().plusDays(5)`
- `"Hoy + 12 días"` → `LocalDate.now().plusDays(12)`
- `"En 3 semanas"` → `LocalDate.now().plusWeeks(3)`
- `"Próximo viernes"` → siguiente viernes
- `"15/06/2026"` → parseo estándar como fallback

Debajo del campo aparece siempre un `Label` con la fecha resuelta en formato largo (`15 de junio de 2026`) o un mensaje de error en rojo si no se reconoce el texto.

#### 2. Interfaz Prioritaria con Teclado (inspirado en Godspeed)

| Tecla | Acción | Pantalla |
|-------|--------|----------|
| `N` | Abrir modal "Nuevo Lote" | Inventario |
| `V` | Marcar lote seleccionado como Vencido | Inventario, Alertas |
| `R` | Marcar lote para Remate / Donación | Inventario, Alertas |
| `↑` / `↓` | Navegar filas de la tabla activa | Cualquier tabla |
| `Enter` | Confirmar acción / guardar formulario | Diálogos |
| `Esc` | Cerrar modal / cancelar | Modales |
| `F5` | Refrescar datos de la pantalla actual | Todas |
| `Ctrl+G` | Ir a Dashboard | Global |
| `Ctrl+I` | Ir a Inventario | Global |
| `Ctrl+A` | Ir a Alertas | Global |
| `Ctrl+R` | Ir a Reportes (solo Supervisor) | Global |

En la parte inferior de la ventana hay una `ShortcutBar` visible (toggleable con `?`).

Los atajos se registran via `InputMap`/`ActionMap` en cada panel y via `KeyboardFocusManager` para los globales. No usar `KeyListener`.

#### 3. Indicadores de Riesgo en Tiempo Real (inspirado en Grafana Gauges)

El Dashboard muestra 4 tarjetas de métrica (`GaugeCard extends JPanel`) con:
- **Número grande** en `JetBrains Mono` mostrando el valor o porcentaje
- **Barra de progreso** con color dinámico
- **Flecha de tendencia** comparando contra el snapshot anterior de la sesión
- **Cambio automático de color** según umbrales:
  - Verde (`-fx-safe`) → sin lotes vencidos y < 15% próximos a vencer
  - Naranja (`-fx-warning`) → ≥ 15% próximos a vencer
  - Rojo (`-fx-danger`) → cualquier lote vencido presente

Las 4 métricas: Total lotes activos · % Próximos a vencer · % Vencidos · Mermas del día.

---

## Enfoque de Desarrollo en Dos Fases

Ver `docs/implementation_plan/` para las tareas detalladas con checkboxes.

```
docs/implementation_plan/
├── 00_overview.md              ← léer primero
├── phase-a/                    ← FASE A: UI con datos simulados
│   ├── 01_setup.md
│   ├── 02_screens.md
│   └── 03_ux_features.md
└── phase-b/                    ← FASE B: lógica real + base de datos
    ├── 04_data_model.md
    ├── 05_repositories.md
    ├── 06_services.md
    └── 07_wiring.md
```

**Fase A primero:** Construir todas las pantallas con `MockData` (listas estáticas en memoria). Validar flujos, atajos de teclado y diseño visual antes de tocar JPA o PostgreSQL.

**Fase B después:** Reemplazar `MockData` por servicios reales. Los controladores no cambian su firma; solo cambia de dónde obtienen los datos.

---

## Comandos de Referencia

```bash
# Levantar DB
docker-compose up -d
docker-compose down

# Build
./gradlew build
./gradlew run
./gradlew test
./gradlew clean build

# Empaquetar
./gradlew jpackage          # genera instalador en build/jpackage/

# Formatear código
./gradlew spotlessApply     # si se configura Spotless
```
