# Fase A — Setup del Proyecto

Objetivo: tener un proyecto JavaFX corriendo con MockData antes de escribir cualquier lógica real.

---

## Gradle

- [x] Crear `settings.gradle`
  ```groovy
  rootProject.name = 'plazavea-perecibles'
  ```

- [x] Crear `build.gradle` con solo las dependencias de Fase A (sin JPA ni PostgreSQL):
  ```groovy
  plugins {
      id 'application'
      id 'org.openjfx.javafxplugin' version '0.1.0'
  }

  group = 'pe.plazavea'
  version = '1.0.0'

  java {
      toolchain { languageVersion = JavaLanguageVersion.of(25) }
  }

  javafx {
      version = '25'
      modules = ['javafx.controls', 'javafx.fxml']
  }

  application {
      mainClass = 'pe.plazavea.perecibles.App'
  }

  repositories { mavenCentral() }

  dependencies {
      implementation 'org.controlsfx:controlsfx:11.2.1'
      implementation 'de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2'
  }
  ```

- [x] Verificar que `./gradlew run` compila sin errores (aunque la clase App no exista aún, el build debe configurarse correctamente)

---

## Estructura de Directorios

- [x] Crear árbol de carpetas vacías:
  ```
  src/
  └── main/
      ├── java/pe/plazavea/perecibles/
      │   ├── controller/
      │   ├── component/
      │   ├── mock/
      │   ├── enums/
      │   └── util/
      └── resources/
          ├── fxml/
          ├── css/
          └── images/
  ```

---

## App Entry Point

- [x] Crear `App.java`:
  ```java
  package pe.plazavea.perecibles;

  import javafx.application.Application;
  import javafx.stage.Stage;

  public class App extends Application {

      @Override
      public void start(Stage primaryStage) throws Exception {
          // Phase A: carga directa del login
          SceneManager.init(primaryStage);
          SceneManager.navigate("login");
          primaryStage.setTitle("Plaza Vea — Control de Perecibles");
          primaryStage.setMinWidth(1024);
          primaryStage.setMinHeight(700);
          primaryStage.show();
      }

      public static void main(String[] args) {
          launch(args);
      }
  }
  ```

- [x] Crear `util/SceneManager.java` — singleton que:
  - Recibe la `Stage` principal en `init(Stage)`
  - Carga FXML desde `resources/fxml/{nombre}.fxml` en `navigate(String name)`
  - Aplica `styles.css` a cada `Scene` nueva
  - Expone `getCurrentUser()` en Phase A (devuelve mock hardcoded)

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

> Nota: No crear todas las 9 entidades JPA completas en Phase A. Solo los POJOs mínimos que los controladores necesitan.

---

## MockData

- [x] Crear `mock/MockData.java` con datos estáticos en listas `ObservableList` (para enlace con TableView):

  ```java
  package pe.plazavea.perecibles.mock;

  import javafx.collections.FXCollections;
  import javafx.collections.ObservableList;
  import pe.plazavea.perecibles.enums.*;
  import pe.plazavea.perecibles.model.*;
  import java.time.LocalDate;

  public class MockData {

      public static ObservableList<Lote> getLotes() {
          return FXCollections.observableArrayList(
              // 3 DISPONIBLE (verde)
              new Lote(1, "L-001", "Leche Gloria 1L", "Lácteos", 100, 100, LocalDate.now(), LocalDate.now().plusDays(20), "Anaquel A1", EstadoLote.DISPONIBLE),
              new Lote(2, "L-002", "Yogur Fresa 500g", "Lácteos", 60, 48, LocalDate.now().minusDays(3), LocalDate.now().plusDays(12), "Cámara B2", EstadoLote.DISPONIBLE),
              new Lote(3, "L-003", "Pollo Entero 1.8kg", "Carnes", 30, 22, LocalDate.now().minusDays(1), LocalDate.now().plusDays(9), "Cámara C1", EstadoLote.DISPONIBLE),
              // 2 PROXIMO_VENCER (naranja)
              new Lote(4, "L-004", "Jamón del País 200g", "Embutidos", 40, 15, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), "Anaquel A3", EstadoLote.PROXIMO_VENCER),
              new Lote(5, "L-005", "Pan de Molde Bimbo", "Panadería", 24, 10, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3), "Anaquel D1", EstadoLote.PROXIMO_VENCER),
              // 1 VENCIDO (rojo)
              new Lote(6, "L-006", "Queso Fresco 250g", "Lácteos", 20, 8, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), "Anaquel A2", EstadoLote.VENCIDO)
          );
      }

      public static ObservableList<Alerta> getAlertas() {
          return FXCollections.observableArrayList(
              new Alerta(1, TipoAlerta.PROXIMO_VENCER, 5, "L-004 — Jamón del País", EstadoAlerta.PENDIENTE),
              new Alerta(2, TipoAlerta.PROXIMO_VENCER, 3, "L-005 — Pan de Molde Bimbo", EstadoAlerta.PENDIENTE),
              new Alerta(3, TipoAlerta.VENCIDO, 0, "L-006 — Queso Fresco 250g", EstadoAlerta.PENDIENTE)
          );
      }

      public static Usuario getOperario() {
          return new Usuario(1, "Carlos", "Quispe", RolUsuario.OPERARIO);
      }

      public static Usuario getSupervisor() {
          return new Usuario(2, "Ana", "Torres", RolUsuario.SUPERVISOR);
      }
  }
  ```

---

## CSS Base

- [x] Crear `resources/css/styles.css` con los design tokens del AGENT.md y estilos base:
  - `.root` con todos los tokens de color y fuente
  - `.btn-primary` — fondo `#FCD535`, texto `#181a20`, radius `6px`
  - `.btn-secondary` — fondo `#1e2329`, texto `#eaecef`, borde `#2b3139`
  - `.card` — fondo `#1e2329`, radius `12px`, padding `24px`
  - `.row-safe` — texto `#0ecb81`
  - `.row-warning` — texto `#f0a500`
  - `.row-danger` — texto `#f6465d`
  - `.sidebar` — fondo `#0b0e11`, borde derecho `#2b3139`
  - `.nav-item` — texto `#707a8a`; `.nav-item.active` — texto `#eaecef`
  - `.muted` — texto `#707a8a`, tamaño `12px`

---

## Verificación

- [x] `./gradlew run` abre la ventana de Login (pantalla negra con tarjeta centrada)
- [x] No hay errores de compilación
- [x] Los 6 enums existen y compilan
- [x] MockData.getLotes() devuelve 6 lotes sin excepción
