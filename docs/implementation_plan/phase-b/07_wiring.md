# Fase B — Wiring: Reemplazar Mocks + Empaquetado

Conectar los paneles Swing de Phase A con los servicios reales de Phase B. La UI no cambia; solo cambia la fuente de datos.

---

## SessionManager (actualizar para Phase B)

- [x] Actualizar `util/SessionManager.java`:
  - Agrega campo `private static Usuario currentUser`
  - Método `login(String email, String password)` llama `UsuarioServicio.login()` y guarda el resultado
  - Método `getCurrentUser()` devuelve el `Usuario` real (no el mock hardcoded)
  - Método `logout()` limpia `currentUser` y llama `Navigator.show("login")`

---

## Bridge Spring ↔ Swing

Swing no usa FXMLLoader — los paneles son instanciados directamente en Java. Spring puede gestionar los paneles anotándolos con `@Component` y obteniendo instancias del `ApplicationContext`.

- [x] En `App.main()`, inicializar Spring antes de `SwingUtilities.invokeLater`:
  ```java
  public static void main(String[] args) {
      Theme.apply();
      Fonts.load();
      ApplicationContext ctx;
      try {
          ctx = new AnnotationConfigApplicationContext(AppConfig.class);
          SpringContext.init(ctx);
      } catch (Exception e) {
          SwingUtilities.invokeLater(() -> {
              JOptionPane.showMessageDialog(null,
                  "No se pudo conectar a la base de datos.\nVerifique que PostgreSQL esté corriendo.",
                  "Error de conexión", JOptionPane.ERROR_MESSAGE);
              System.exit(1);
          });
          return;
      }
      SwingUtilities.invokeLater(() -> {
          MainFrame frame = SpringContext.getBean(MainFrame.class);
          frame.setVisible(true);
      });
  }
  ```

- [x] Crear `config/AppConfig.java` con `@Configuration @ComponentScan("pe.plazavea.perecibles")`

- [x] Anotar `MainFrame`, `DashboardPanel`, `InventarioPanel`, `AlertasPanel`, `ReportesPanel` con `@Component` para que Spring los gestione e inyecte los servicios via `@Autowired`

- [x] `SpringContext.java` sigue el mismo patrón — guarda el `ApplicationContext` y expone `getBean(Class<T>)`

---

## Reemplazar MockData en Controladores

Para cada controlador, reemplazar la llamada a MockData por la llamada al servicio correspondiente. Los tipos devueltos son los mismos (Lote, Alerta, etc.) por lo que el binding con TableView no cambia.

### LoginPanel

- [x] Reemplazar credenciales hardcoded:
  ```java
  // Antes (Phase A):
  if (email.equals("operario@plazavea.com") && password.equals("admin")) { ... }

  // Después (Phase B):
  new SwingWorker<Usuario, Void>() {
      @Override protected Usuario doInBackground() throws Exception {
          return usuarioServicio.login(emailField.getText(), new String(passwordField.getPassword()));
      }
      @Override protected void done() {
          try {
              Usuario usuario = get();
              SessionManager.setCurrentUser(usuario);
              Navigator.show("dashboard");
          } catch (Exception e) {
              errorLabel.setText(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
              errorLabel.setVisible(true);
          }
      }
  }.execute();
  ```

- [x] Inyectar `UsuarioServicio` via `@Autowired`

### DashboardPanel

- [x] Reemplazar `MockData.getLotes()` con `inventarioServicio.consultarStock()`
- [x] `refreshDashboard()` delega a `SwingWorker` para no bloquear el EDT:
  ```java
  new SwingWorker<List<Lote>, Void>() {
      @Override protected List<Lote> doInBackground() {
          return inventarioServicio.consultarStock();
      }
      @Override protected void done() {
          try { updateGauges(get()); }
          catch (Exception ex) { showErrorToast(ex.getMessage()); }
      }
  }.execute();
  ```

### InventarioPanel

- [x] Reemplazar `MockData.getLotes()` con `SwingWorker` que llama `inventarioServicio.consultarStock()`
- [x] Al guardar desde `NuevoLoteDialog`, llamar `inventarioServicio.registrarIngreso(lote, SessionManager.getCurrentUser())`
- [x] Atajos `V` y `R` ahora llaman `inventarioServicio.registrarRetiro(...)` con el tipo correspondiente

### AlertasPanel

- [x] Reemplazar `MockData.getAlertas()` con `alertaServicio.obtenerPendientes()`
- [x] Atender / Ignorar llaman `alertaServicio.atenderAlerta()` / `alertaServicio.ignorarAlerta()`

### ReportesPanel

- [x] Habilitar botón `Exportar CSV` (estaba deshabilitado en Phase A)
- [x] Al generar: llamar `ReporteServicio` via `SwingWorker` según el `TipoReporte` seleccionado
- [x] Al exportar: llamar `reporteServicio.exportarCSV(reporte)`

---

## Arranque de AlertaServicio

- [x] En `App.main()`, después de inicializar Spring y antes de `SwingUtilities.invokeLater`:
  ```java
  AlertaServicio alertaServicio = SpringContext.getBean(AlertaServicio.class);
  alertaServicio.iniciarScheduler();
  ```

- [x] Registrar shutdown hook en `MainFrame.addWindowListener`:
  ```java
  frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosed(WindowEvent e) {
          SpringContext.getBean(AlertaServicio.class).getScheduler().shutdown();
      }
  });
  ```

---

## Manejo de Error de Conexión a DB

- [x] Si el `ApplicationContext` no puede conectar a PostgreSQL al iniciar, mostrar un `JOptionPane` antes de abrir la ventana principal (ya incluido en el bloque `App.main()` de arriba):
  ```java
  JOptionPane.showMessageDialog(null,
      "No se pudo conectar a la base de datos.\nVerifique que PostgreSQL esté corriendo.",
      "Error de conexión", JOptionPane.ERROR_MESSAGE);
  System.exit(1);
  ```

---

## Semilla de Datos Inicial (Data Seeder)

- [x] Crear `config/DataSeeder.java` con `@Component` que implementa `ApplicationListener<ContextRefreshedEvent>`:
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

- [x] Agregar configuración en `build.gradle`:
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

- [x] Verificar que el instalador incluye el JRE embebido (jpackage lo hace por defecto)
- [ ] Probar que la app abre sin necesitar Java instalado en el equipo destino

---

## Verificación Phase B — Wiring

- [ ] `docker-compose up -d` + `./gradlew run` → app abre con datos reales
- [x] Login con `operario@plazavea.com` / `admin` → autentica contra DB
- [ ] Registrar un nuevo Lote → persiste y aparece después de reiniciar la app
- [x] `AlertaServicio` genera alertas al iniciar (las primeras se crean en el ciclo inicial)
- [ ] Dashboard actualiza datos desde la DB cada 60 segundos sin congelar la UI
- [ ] Exportar reporte genera archivo CSV en la carpeta temporal del OS
- [ ] Desconectar PostgreSQL mientras la app está abierta → dialog de error amigable al intentar una operación
- [x] `./gradlew jpackage` genera el instalador sin errores
- [ ] Instalador ejecutado en una máquina sin Java → app abre correctamente
