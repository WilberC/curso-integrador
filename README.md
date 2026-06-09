# Sistema de Control de Productos Perecibles - Plaza Vea

Aplicacion de escritorio Swing + FlatLaf para controlar inventario de productos perecibles en tiendas Plaza Vea. El sistema centraliza el registro de lotes, seguimiento de fechas de vencimiento, alertas automaticas y reportes de mermas.

El objetivo del proyecto es reemplazar el control manual de vencimientos por una herramienta local de tienda que ayude a reducir perdidas por productos vencidos durante la operacion diaria.

## Usuarios

- Operario: registra lotes, consulta inventario y atiende movimientos.
- Supervisor: realiza las acciones del operario y accede a reportes y configuracion de alertas.

## Estado Actual

El proyecto esta iniciando la Fase A: una version UI-first con datos simulados antes de integrar base de datos.

- [x] Configuracion Gradle creada
- [x] FlatLaf y MigLayout configurados
- [x] Estructura base de paquetes creada
- [x] Pantalla inicial de login creada
- [x] Enums de dominio creados
- [x] POJOs minimos de Fase A creados
- [x] `MockData` creado
- [x] Tokens visuales en `Theme.java` creados
- [x] Pantallas principales de la Fase A
- [x] Atajos de teclado base
- [x] Compilacion verificada
- [ ] Parser de fechas en lenguaje natural
- [x] Dashboard con gauges
- [x] Persistencia real con PostgreSQL

El checklist detallado esta en `docs/implementation_plan/phase-a/01_setup.md`.

## Stack

| Herramienta | Version | Uso |
|-------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Gradle | 9.5.0 | Build y ejecucion |
| Swing | (incluido en JDK) | Interfaz de escritorio nativa |
| FlatLaf | 3.6 | Look & Feel moderno oscuro |
| MigLayout | 11.4.2 | Layout para formularios |
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

Ejecutar la aplicacion con el script corto:

```bash
./scripts/run.sh
```

`gradlew run` ya compila las clases y procesa recursos antes de abrir la aplicacion, asi que no es necesario correr `compileJava processResources` manualmente cada vez.

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

En sistemas donde Java 21 ya este disponible en el entorno, tambien se puede usar directamente:

```bash
./gradlew run
```

## Estructura del Proyecto

```text
src/main/java/pe/plazavea/perecibles/
├── App.java
├── enums/
├── mock/
├── model/
├── theme/
└── ui/
```

Carpetas principales:

- `ui/`: paneles y dialogos Swing (uno por pantalla).
- `ui/component/`: componentes reutilizables (GaugeCard, StatusChip, Buttons).
- `ui/panel/`: pantallas y regiones principales (Login, Dashboard, Sidebar, Toolbar).
- `ui/table/`: modelos y renderers para `JTable`.
- `theme/`: tokens de color/fuente (`Theme.java`, `Fonts.java`).
- `enums/`: enumeraciones del dominio.
- `mock/`: datos simulados usados durante Fase A.
- `model/`: POJOs de dominio para Fase A.
- `resources/fonts/`: archivos .ttf de Inter y JetBrains Mono cuando se agreguen; si no existen, `Fonts.java` usa fuentes del sistema.

## Flujo de Implementacion

El plan completo vive en `docs/implementation_plan/`:

- `00_overview.md`: vision general, fases y criterios de exito.
- `phase-a/01_setup.md`: configuracion inicial del proyecto.
- `phase-a/02_screens.md`: pantallas Swing con datos simulados.
- `phase-a/03_ux_features.md`: parser de fechas, teclado y gauges.
- `phase-b/04_data_model.md`: entidades JPA.
- `phase-b/05_repositories.md`: repositorios Spring Data.
- `phase-b/06_services.md`: logica de negocio.
- `phase-b/07_wiring.md`: reemplazo de mocks, Docker y empaquetado.

## Convenciones

- Clases en `PascalCase`.
- Variables y metodos en `camelCase`.
- Constantes en `UPPER_SNAKE_CASE`.
- No usar FXML ni CSS; la UI se construye con Swing y se estiliza con FlatLaf + `Theme.java`.
- Nombres tecnicos en ingles; nombres del dominio y textos de UI en espanol.

## Autor

wilberC
