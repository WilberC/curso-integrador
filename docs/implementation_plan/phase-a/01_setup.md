# Fase A — Setup del Proyecto

Objetivo: tener un proyecto Swing + FlatLaf corriendo con MockData antes de escribir cualquier lógica real.

---

## Gradle

- [x] Crear `settings.gradle`:
  ```groovy
  rootProject.name = 'plazavea-perecibles'
  ```

- [x] Crear `build.gradle` con solo las dependencias de Fase A (sin JPA ni PostgreSQL):
  ```groovy
  plugins {
      id 'application'
      // No se necesita plugin de JavaFX — Swing viene incluido en el JDK
  }

  group = 'pe.plazavea'
  version = '1.0.0'

  java {
      toolchain { languageVersion = JavaLanguageVersion.of(25) }
  }

  application {
      mainClass = 'pe.plazavea.perecibles.App'
  }

  repositories { mavenCentral() }

  dependencies {
      implementation 'com.formdev:flatlaf:3.6'
      implementation 'com.formdev:flatlaf-extras:3.6'
      implementation 'com.miglayout:miglayout-swing:11.4.2'
  }
  ```

- [x] Verificar que `./gradlew run` compila sin errores

---

## Estructura de Directorios

- [x] Crear árbol de carpetas vacías:
  ```
  src/
  └── main/
      ├── java/pe/plazavea/perecibles/
      │   ├── ui/           ← JPanel + JDialog por pantalla
      │   ├── component/    ← GaugeCard, SidebarPanel, ShortcutBar, StatusChip
      │   ├── theme/        ← Theme.java, Fonts.java
      │   ├── mock/
      │   ├── enums/
      │   ├── model/
      │   └── util/
      └── resources/
          ├── fonts/        ← Inter-Regular.ttf, Inter-Bold.ttf,
          │                    JetBrainsMono-Regular.ttf, JetBrainsMono-Bold.ttf
          └── images/
  ```

> **Sin `fxml/` ni `css/`:** Swing no los usa. La UI se construye en código Java. Los estilos se aplican vía `Theme.java` + FlatLaf UIManager.

---

## App Entry Point

- [x] Crear `App.java`:
  ```java
  package pe.plazavea.perecibles;

  import pe.plazavea.perecibles.theme.Fonts;
  import pe.plazavea.perecibles.theme.Theme;
  import pe.plazavea.perecibles.ui.MainFrame;
  import javax.swing.SwingUtilities;

  public class App {

      public static void main(String[] args) {
          // MUST be called before any Swing component is created
          Theme.apply();
          Fonts.load();

          SwingUtilities.invokeLater(() -> {
              MainFrame frame = new MainFrame();
              frame.setTitle("Plaza Vea — Control de Perecibles");
              frame.setMinimumSize(new java.awt.Dimension(1024, 700));
              frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
              frame.setVisible(true);
          });
      }
  }
  ```

- [x] Crear `theme/Theme.java` con:
  - Todas las constantes de color (`CANVAS_DARK`, `SURFACE_CARD`, etc.)
  - Método `apply()` que llama `FlatDarkLaf.setup()` y sobreescribe los UIManager defaults relevantes (ver `DESIGN.md`)

- [x] Crear `theme/Fonts.java` con:
  - Carga de `Inter` y `JetBrains Mono` desde `/fonts/*.ttf`
  - Métodos helper: `inter(int style, float size)`, `mono(int style, float size)`

- [x] Crear `util/Navigator.java` — wrapper de `CardLayout`:
  - Recibe el `JPanel` con `CardLayout` en `init(JPanel cards, CardLayout layout)`
  - Método `show(String name)` que llama `layout.show(cards, name)`
  - Método `getCurrentName()` para que el shortcut bar sepa qué pantalla está activa

- [x] Crear `util/SessionManager.java` — singleton:
  - `setCurrentUser(Usuario)` y `getCurrentUser()`
  - En Phase A devuelve usuario mock hardcoded según última navegación de login

---

## Enums (crear ahora, usados por MockData)

- [x] Crear `enums/RolUsuario.java` — `OPERARIO, SUPERVISOR`
- [x] Crear `enums/EstadoLote.java` — `DISPONIBLE, PROXIMO_VENCER, VENCIDO, RETIRADO`
- [x] Crear `enums/TipoMovimiento.java` — `INGRESO, RETIRO, AJUSTE, REMATE, DONACION`
- [x] Crear `enums/TipoAlerta.java` — `PROXIMO_VENCER, VENCIDO`
- [x] Crear `enums/EstadoAlerta.java` — `PENDIENTE, ATENDIDA, IGNORADA`
- [x] Crear `enums/TipoReporte.java` — `STOCK, VENCIDOS, PROXIMOS_VENCER, MERMAS`

---

## POJOs de Dominio (sin JPA — para Phase A)

En Phase A las entidades son POJOs simples (sin `@Entity`). Se reemplazarán con las entidades JPA en Phase B sin cambiar la interfaz pública.

- [x] Crear clase POJO `model/Lote.java` con campos: `id`, `numeroLote`, `producto` (String), `categoria` (String), `cantidadInicial`, `cantidadActual`, `fechaIngreso`, `fechaVencimiento`, `ubicacion`, `estado (EstadoLote)` — y métodos `getDiasParaVencer()`, `estaVencido()`, `estaProximoAVencer(int)`
- [x] Crear clase POJO `model/Alerta.java` con campos: `id`, `tipoAlerta`, `diasParaVencer`, `fechaGeneracion`, `estado`, `loteNumero` (String)
- [x] Crear clase POJO `model/Usuario.java` con campos: `id`, `nombre`, `apellido`, `rol (RolUsuario)`

---

## MockData

- [x] Crear `mock/MockData.java` con datos estáticos en `java.util.List` (Swing usa listas Java estándar, no `ObservableList`):

  ```java
  package pe.plazavea.perecibles.mock;

  import pe.plazavea.perecibles.enums.*;
  import pe.plazavea.perecibles.model.*;
  import java.time.LocalDate;
  import java.util.ArrayList;
  import java.util.List;

  public class MockData {

      // Mutable list so NuevoLoteDialog can add to it; panels listen via model refresh
      private static final List<Lote> lotes = new ArrayList<>(List.of(
          // 3 DISPONIBLE
          new Lote(1, "L-001", "Leche Gloria 1L", "Lácteos", 100, 100, LocalDate.now(), LocalDate.now().plusDays(20), "Anaquel A1", EstadoLote.DISPONIBLE),
          new Lote(2, "L-002", "Yogur Fresa 500g", "Lácteos", 60, 48, LocalDate.now().minusDays(3), LocalDate.now().plusDays(12), "Cámara B2", EstadoLote.DISPONIBLE),
          new Lote(3, "L-003", "Pollo Entero 1.8kg", "Carnes", 30, 22, LocalDate.now().minusDays(1), LocalDate.now().plusDays(9), "Cámara C1", EstadoLote.DISPONIBLE),
          // 2 PROXIMO_VENCER
          new Lote(4, "L-004", "Jamón del País 200g", "Embutidos", 40, 15, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), "Anaquel A3", EstadoLote.PROXIMO_VENCER),
          new Lote(5, "L-005", "Pan de Molde Bimbo", "Panadería", 24, 10, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3), "Anaquel D1", EstadoLote.PROXIMO_VENCER),
          // 1 VENCIDO
          new Lote(6, "L-006", "Queso Fresco 250g", "Lácteos", 20, 8, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), "Anaquel A2", EstadoLote.VENCIDO)
      ));

      private static final List<Alerta> alertas = new ArrayList<>(List.of(
          new Alerta(1, TipoAlerta.PROXIMO_VENCER, 5, "L-004 — Jamón del País", EstadoAlerta.PENDIENTE),
          new Alerta(2, TipoAlerta.PROXIMO_VENCER, 3, "L-005 — Pan de Molde Bimbo", EstadoAlerta.PENDIENTE),
          new Alerta(3, TipoAlerta.VENCIDO, 0, "L-006 — Queso Fresco 250g", EstadoAlerta.PENDIENTE)
      ));

      public static List<Lote> getLotes()     { return lotes; }
      public static List<Alerta> getAlertas() { return alertas; }

      public static Usuario getOperario()   { return new Usuario(1, "Carlos", "Quispe", RolUsuario.OPERARIO); }
      public static Usuario getSupervisor() { return new Usuario(2, "Ana", "Torres", RolUsuario.SUPERVISOR); }
  }
  ```

  > `List` estándar — cuando `NuevoLoteDialog` agrega un lote, los paneles llaman `tableModel.fireTableDataChanged()` tras cerrar el diálogo.

---

## MainFrame y Navegación Base

- [x] Crear `ui/MainFrame.java`:
  - Extiende `JFrame`
  - Layout raíz: `JPanel` con `BorderLayout`
  - WEST: `SidebarPanel` (200px)
  - CENTER: `JPanel` con `CardLayout` (las pantallas)
  - SOUTH: `ShortcutBar` (28px)
  - Registra los shortcuts globales via `KeyboardFocusManager`

- [x] Crear `component/SidebarPanel.java`:
  - Fondo `Theme.SURFACE_CARD`, layout `BoxLayout Y_AXIS`
  - Wordmark label en `Theme.PRIMARY` (56px de alto)
  - Nav items: Dashboard, Inventario, Alertas, Reportes
  - `NavItem` con efecto activo (barra izquierda amarilla de 3px)
  - Footer: nombre usuario + rol + botón "Cerrar Sesión"

- [x] Crear `component/ShortcutBar.java`:
  - Fondo `Theme.SURFACE_ELEVATED`, alto `28px`
  - Borde superior `Theme.HAIRLINE_DARK`
  - Método `setHints(List<ShortcutHint>)` que reemplaza los chips de la barra
  - Toggle de visibilidad con atajo `?`

---

## Verificación

- [x] `./gradlew run` abre la ventana con tema oscuro (FlatLaf activo)
- [x] No hay errores de compilación
- [x] Los 6 enums existen y compilan
- [x] `MockData.getLotes()` devuelve 6 lotes sin excepción
- [x] `Theme.CANVAS_DARK` es el color de fondo de la ventana principal
