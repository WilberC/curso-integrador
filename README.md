# Sistema de Control de Productos Perecibles - Plaza Vea

Aplicacion de escritorio JavaFX para controlar inventario de productos perecibles en tiendas Plaza Vea. El sistema centraliza el registro de lotes, seguimiento de fechas de vencimiento, alertas automaticas y reportes de mermas.

El objetivo del proyecto es reemplazar el control manual de vencimientos por una herramienta local de tienda que ayude a reducir perdidas por productos vencidos durante la operacion diaria.

## Usuarios

- Operario: registra lotes, consulta inventario y atiende movimientos.
- Supervisor: realiza las acciones del operario y accede a reportes y configuracion de alertas.

## Estado Actual

El proyecto esta iniciando la Fase A: una version UI-first con datos simulados antes de integrar base de datos.

- [x] Configuracion Gradle creada
- [x] JavaFX configurado
- [x] Estructura base de paquetes creada
- [x] Pantalla inicial de login creada
- [x] Enums de dominio creados
- [x] POJOs minimos de Fase A creados
- [x] `MockData` creado
- [x] CSS base con tokens visuales creado
- [x] Compilacion verificada
- [ ] Pantallas principales de la Fase A
- [ ] Atajos de teclado
- [ ] Parser de fechas en lenguaje natural
- [ ] Dashboard con gauges
- [ ] Persistencia real con PostgreSQL

El checklist detallado esta en `docs/implementation_plan/phase-a/01_setup.md`.

## Stack

| Herramienta | Version | Uso |
|-------------|---------|-----|
| Java | 25 | Lenguaje principal |
| Gradle | 9.5.0 | Build y ejecucion |
| JavaFX | 25 | Interfaz de escritorio |
| ControlsFX | 11.2.1 | Componentes JavaFX avanzados |
| FontAwesomeFX | 4.7.0-9.1.2 | Iconos |
| PostgreSQL | 16 | Persistencia en Fase B |

Las versiones de Java y Gradle estan fijadas en `mise.toml`.

## Requisitos

- macOS, Linux o Windows con entorno grafico disponible
- `mise` instalado
- Conexion a internet en la primera ejecucion para descargar Gradle y dependencias

Instalar herramientas del proyecto:

```bash
mise install
```

## Como Ejecutar

Compilar:

```bash
mise exec -- ./gradlew compileJava processResources
```

Ejecutar la aplicacion:

```bash
mise exec -- ./gradlew run
```

Ejecutar tests cuando existan:

```bash
mise exec -- ./gradlew test
```

En sistemas donde Java 25 ya este disponible en el entorno, tambien se puede usar directamente:

```bash
./gradlew run
```

## Estructura del Proyecto

```text
src/main/java/pe/plazavea/perecibles/
├── App.java
├── controller/
├── component/
├── enums/
├── mock/
├── model/
└── util/

src/main/resources/
├── css/
├── fxml/
└── images/
```

Carpetas principales:

- `controller/`: controladores JavaFX.
- `component/`: componentes reutilizables de UI.
- `enums/`: enumeraciones del dominio.
- `mock/`: datos simulados usados durante Fase A.
- `model/`: POJOs de dominio para Fase A.
- `util/`: utilidades transversales como navegacion de escenas.
- `resources/fxml/`: vistas JavaFX.
- `resources/css/`: estilos globales y tokens visuales.

## Flujo de Implementacion

El plan completo vive en `docs/implementation_plan/`:

- `00_overview.md`: vision general, fases y criterios de exito.
- `phase-a/01_setup.md`: configuracion inicial del proyecto.
- `phase-a/02_screens.md`: pantallas JavaFX con datos simulados.
- `phase-a/03_ux_features.md`: parser de fechas, teclado y gauges.
- `phase-b/04_data_model.md`: entidades JPA.
- `phase-b/05_repositories.md`: repositorios Spring Data.
- `phase-b/06_services.md`: logica de negocio.
- `phase-b/07_wiring.md`: reemplazo de mocks, Docker y empaquetado.

## Convenciones

- Clases en `PascalCase`.
- Variables y metodos en `camelCase`.
- Constantes en `UPPER_SNAKE_CASE`.
- FXML en `kebab-case`.
- CSS classes en `kebab-case`.
- Nombres tecnicos en ingles; nombres del dominio y textos de UI en espanol.

## Autor

wilberC
