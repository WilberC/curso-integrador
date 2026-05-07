# Fase B — Wiring: Reemplazar Mocks + Empaquetado

Conectar los controladores JavaFX de Phase A con los servicios reales de Phase B. La UI no cambia; solo cambia la fuente de datos.

---

## SessionManager (actualizar para Phase B)

- [ ] Actualizar `util/SessionManager.java`:
  - Agrega campo `private static Usuario currentUser`
  - Método `login(String email, String password)` llama `UsuarioServicio.login()` y guarda el resultado
  - Método `getCurrentUser()` devuelve el `Usuario` real (no el mock hardcoded)
  - Método `logout()` limpia `currentUser` y navega a `"login"`

---

## Bridge Spring ↔ JavaFX

JavaFX instancia sus controladores via FXMLLoader; Spring no los conoce por defecto. Solución: pasar el `ApplicationContext` al loader.

- [ ] En `App.java`, inicializar Spring antes de JavaFX:
  ```java
  @Override
  public void start(Stage stage) throws Exception {
      ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
      SpringContext.init(ctx);
      // ... resto del start
  }
  ```

- [ ] Crear `config/AppConfig.java` con `@Configuration @ComponentScan("pe.plazavea.perecibles")`

- [ ] En `SceneManager.navigate()`, usar un `FXMLLoader` que obtiene controladores del contexto Spring:
  ```java
  FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + name + ".fxml"));
  loader.setControllerFactory(SpringContext::getBean);
  ```

- [ ] Anotar todos los controladores con `@Component` para que Spring los gestione

---

## Reemplazar MockData en Controladores

Para cada controlador, reemplazar la llamada a MockData por la llamada al servicio correspondiente. Los tipos devueltos son los mismos (Lote, Alerta, etc.) por lo que el binding con TableView no cambia.

### LoginController

- [ ] Reemplazar hardcoded credentials:
  ```java
  // Antes (Phase A):
  if (email.equals("operario@plazavea.com") && password.equals("admin")) { ... }

  // Después (Phase B):
  try {
      Usuario usuario = usuarioServicio.login(emailField.getText(), passwordField.getText());
      SessionManager.setCurrentUser(usuario);
      SceneManager.navigate("dashboard");
  } catch (RuntimeException e) {
      errorLabel.setText(e.getMessage());
      errorLabel.setVisible(true);
  }
  ```

- [ ] Inyectar `UsuarioServicio` via campo con `@Autowired` o constructor

### DashboardController

- [ ] Reemplazar `MockData.getLotes()` con `inventarioServicio.consultarStock()`
- [ ] `refreshDashboard()` ahora corre `inventarioServicio.consultarStock()` en un `Task<List<Lote>>` y actualiza la UI en `Platform.runLater()` para no bloquear el hilo de JavaFX:
  ```java
  Task<List<Lote>> task = new Task<>() {
      @Override protected List<Lote> call() { return inventarioServicio.consultarStock(); }
  };
  task.setOnSucceeded(e -> updateGauges(task.getValue()));
  new Thread(task).start();
  ```

### InventarioController

- [ ] Reemplazar MockData con `inventarioServicio.consultarStock()`
- [ ] Al guardar desde `NuevoLoteController`, llamar `inventarioServicio.registrarIngreso(lote, SessionManager.getCurrentUser())`
- [ ] Atajos `V` y `R` ahora llaman `inventarioServicio.registrarRetiro(...)` con el tipo correspondiente

### AlertasController

- [ ] Reemplazar `MockData.getAlertas()` con `alertaServicio.obtenerPendientes()`
- [ ] Atender / Ignorar llaman `alertaServicio.atenderAlerta()` / `alertaServicio.ignorarAlerta()`

### ReportesController

- [ ] Habilitar botón `Exportar CSV` (estaba deshabilitado en Phase A)
- [ ] Al generar: llamar el método correspondiente de `ReporteServicio` según el `TipoReporte` seleccionado
- [ ] Al exportar: llamar `reporteServicio.exportarCSV(reporte)`

---

## Arranque de AlertaServicio

- [ ] En `App.start()`, después de inicializar Spring:
  ```java
  AlertaServicio alertaServicio = SpringContext.getBean(AlertaServicio.class);
  alertaServicio.iniciarScheduler();
  ```

- [ ] En `App.stop()` (llamado al cerrar la ventana):
  ```java
  @Override
  public void stop() {
      SpringContext.getBean(AlertaServicio.class).getScheduler().shutdown();
  }
  ```

---

## Manejo de Error de Conexión a DB

- [ ] Si el `ApplicationContext` no puede conectar a PostgreSQL al iniciar, mostrar un `Alert` de error antes de abrir la ventana principal:
  ```java
  try {
      ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
      SpringContext.init(ctx);
  } catch (Exception e) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error de conexión");
      alert.setContentText("No se pudo conectar a la base de datos. Verifique que PostgreSQL esté corriendo.");
      alert.showAndWait();
      Platform.exit();
      return;
  }
  ```

---

## Semilla de Datos Inicial (Data Seeder)

- [ ] Crear `config/DataSeeder.java` con `@Component` que implementa `ApplicationListener<ContextRefreshedEvent>`:
  - Verifica si la tabla `usuario` está vacía
  - Si es la primera ejecución, inserta:
    - 1 supervisor: `supervisor@plazavea.com` / `admin`
    - 1 operario: `operario@plazavea.com` / `admin`
    - 4 categorías: Lácteos, Carnes, Embutidos, Panadería
    - Configuración de alerta default: diasAmarilla=7, diasRoja=2

---

## Cambiar DDL a `validate` para Producción

- [ ] Cambiar `.env` para producción:
  ```
  DB_DDL_AUTO=validate
  ```
  (En desarrollo mantener `create-drop` o `update`)

---

## Empaquetado con jpackage

- [ ] Agregar configuración en `build.gradle`:
  ```groovy
  jpackage {
      appName = 'Plaza Vea — Perecibles'
      vendor = 'Supermercados Peruanos S.A.'
      appVersion = project.version
      mainJar = jar.archiveFileName.get()
      mainClass = application.mainClass.get()

      // Windows
      winMenu = true
      winShortcut = true
      type = 'MSI'

      // macOS (descomentar para build en Mac)
      // type = 'DMG'

      jvmArgs = ['-Xmx512m']
      appContent = ['.env']  // incluir .env en el instalador (preconfigurado por el admin de TI)
  }
  ```

- [ ] Verificar que el instalador incluye el JRE embebido (jpackage lo hace por defecto)
- [ ] Probar que la app abre sin necesitar Java instalado en el equipo destino

---

## Verificación Phase B — Wiring

- [ ] `docker-compose up -d` + `./gradlew run` → app abre con datos reales
- [ ] Login con `operario@plazavea.com` / `admin` → autentica contra DB
- [ ] Registrar un nuevo Lote → persiste y aparece después de reiniciar la app
- [ ] `AlertaServicio` genera alertas al iniciar (las primeras se crean en el ciclo inicial)
- [ ] Dashboard actualiza datos desde la DB cada 60 segundos sin congelar la UI
- [ ] Exportar reporte genera archivo CSV en la carpeta temporal del OS
- [ ] Desconectar PostgreSQL mientras la app está abierta → dialog de error amigable al intentar una operación
- [ ] `./gradlew jpackage` genera el instalador sin errores
- [ ] Instalador ejecutado en una máquina sin Java → app abre correctamente
