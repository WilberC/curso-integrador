# Sistema de Control de Productos Perecibles — Plaza Vea
## Guía de Trabajo para Desarrolladores

---

## Descripción del Proyecto

Aplicación de escritorio JavaFX para el control de inventario de productos perecibles en tiendas Plaza Vea (Lima). Reemplaza el proceso manual de revisión física de fechas de vencimiento con un sistema que centraliza el registro de lotes, genera alertas automáticas y produce reportes de mermas. Meta: reducir pérdidas por productos vencidos en 30% durante el primer año en tiendas piloto.

Usuarios: **Operarios** (registro y movimientos) y **Supervisores** (todo lo anterior + reportes + configuración de alertas).

---

## Tech Stack

| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| Java | 25 | Lenguaje principal |
| Gradle | 9.5.0 | Build tool |
| JavaFX | 25 | UI framework (desktop only) |
| ControlsFX | 11.2.1 | Componentes avanzados de JavaFX |
| FontAwesomeFX | 4.7.0-9.1.2 | Íconos vectoriales |
| Spring Data JPA | 3.x | ORM / repositorios |
| Hibernate | 6.x | Implementación JPA |
| PostgreSQL | 16 | Base de datos (via Docker) |
| dotenv-java | 3.x | Lectura de variables de entorno desde `.env` |
| Jackson | 2.x | Serialización JSON / exportación CSV |
| jpackage | (incluido en JDK 25) | Empaquetado de instalador nativo |
| Inter (font) | latest | Tipografía cuerpo / headings |
| JetBrains Mono (font) | latest | Tipografía numérica / estadísticas |

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

---

## Estructura de Paquetes

```
src/main/java/pe/plazavea/perecibles/
├── App.java                        # JavaFX Application entry point
│
├── model/                          # Entidades JPA (Phase B)
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
├── controller/                     # Controladores JavaFX (uno por pantalla)
│   ├── LoginController.java
│   ├── DashboardController.java
│   ├── InventarioController.java
│   ├── NuevoLoteController.java
│   ├── AlertasController.java
│   └── ReportesController.java
│
├── component/                      # Componentes JavaFX reutilizables
│   └── GaugeCard.java              # Indicador de riesgo del dashboard
│
├── util/                           # Utilidades transversales
│   ├── DateParser.java             # Parser de fechas en lenguaje natural
│   └── SessionManager.java         # Singleton — usuario autenticado en sesión
│
└── config/                         # Configuración JPA / Spring (Phase B)
    ├── JpaConfig.java
    └── SpringContext.java          # Bridge entre Spring y JavaFX

src/main/resources/
├── fxml/
│   ├── login.fxml
│   ├── dashboard.fxml
│   ├── inventario.fxml
│   ├── nuevo-lote.fxml
│   ├── alertas.fxml
│   └── reportes.fxml
├── css/
│   └── styles.css                  # Design tokens + estilos globales
└── images/
    └── (íconos PNG/SVG)
```

---

## Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clases entidad | `PascalCase`, singular | `ProductoPerecible` |
| Servicios | `PascalCase` + sufijo `Servicio` | `InventarioServicio` |
| Repositorios | `PascalCase` + sufijo `Repository` | `LoteRepository` |
| Controladores JavaFX | `PascalCase` + sufijo `Controller` | `DashboardController` |
| FXML | `kebab-case` | `nuevo-lote.fxml` |
| CSS classes | `kebab-case` | `.gauge-card`, `.row-danger` |
| Variables | `camelCase` | `fechaVencimiento` |
| Constantes | `UPPER_SNAKE_CASE` | `DIAS_ALERTA_ROJA` |
| Enums | `PascalCase` clase, `UPPER_SNAKE_CASE` valores | `EstadoLote.PROXIMO_VENCER` |
| Columnas DB | `snake_case` | `fecha_vencimiento` |

**Idioma del código:** inglés para nombres técnicos (métodos, variables locales temporales). Español para nombres de dominio que el cliente usa (entidades, campos, mensajes UI).

---

## Reglas UI/UX

### Design Tokens — JavaFX CSS

Declarar en `:root` (o en la regla `.root`) de `styles.css`:

```css
.root {
    /* Canvas */
    -fx-canvas-dark:      #0b0e11;
    -fx-surface-card:     #1e2329;
    -fx-surface-elevated: #2b3139;

    /* Accent — solo para CTAs primarios y el wordmark */
    -fx-primary:          #FCD535;
    -fx-primary-active:   #f0b90b;
    -fx-on-primary:       #181a20;   /* texto negro sobre amarillo */

    /* Semántica de stock */
    -fx-safe:             #0ecb81;   /* DISPONIBLE */
    -fx-warning:          #f0a500;   /* PROXIMO_VENCER */
    -fx-danger:           #f6465d;   /* VENCIDO */

    /* Texto */
    -fx-body:             #eaecef;
    -fx-muted:            #707a8a;
    -fx-ink:              #181a20;   /* texto sobre superficies claras */

    /* Bordes */
    -fx-hairline-dark:    #2b3139;
    -fx-hairline-light:   #eaecef;

    /* Radio */
    -fx-radius-sm:        4px;
    -fx-radius-md:        6px;
    -fx-radius-lg:        8px;
    -fx-radius-xl:        12px;

    /* Fuentes */
    -fx-font-family:      "Inter";
    -fx-font-mono:        "JetBrains Mono";
}
```

**Reglas de uso:**
- `-fx-primary` (amarillo) **solo** para botones de acción principal y el logo. Nunca para texto de cuerpo ni fondos de sección.
- Colores de stock (`safe` / `warning` / `danger`) van como color de texto o borde de fila, nunca como fondo completo de tarjeta.
- Fondo base siempre `#0b0e11`. Tarjetas elevadas `#1e2329`. Elementos sobre tarjeta `#2b3139`.
- Texto numérico (cantidades, días, porcentajes) en `JetBrains Mono` para legibilidad tabular.

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

En la parte inferior de la ventana hay una barra de atajos visible (toggleable con `?`).

#### 3. Indicadores de Riesgo en Tiempo Real (inspirado en Grafana Gauges)

El Dashboard muestra 4 tarjetas de métrica (`GaugeCard`) con:
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
