# Arquitectura del Proyecto — Gestión de Perecibles

Este documento es una guía de referencia para entender cómo está organizado el código. La aplicación es un sistema de escritorio construido con **Java Swing** para la interfaz gráfica y **Spring + JPA** para la lógica de negocio y acceso a datos.

---

## Estructura de carpetas

```
src/main/java/pe/plazavea/perecibles/
├── App.java               ← Punto de entrada de la aplicación
├── config/                ← Configuración de Spring y base de datos
├── enums/                 ← Constantes con valores fijos (estados, tipos)
├── mock/                  ← Datos de prueba para desarrollo
├── model/                 ← Entidades JPA (tablas de la base de datos)
├── repository/            ← Acceso a datos (consultas SQL/JPQL)
├── service/               ← Lógica de negocio
├── theme/                 ← Colores y tipografía de la UI
├── ui/
│   ├── MainFrame.java     ← Ventana principal
│   ├── Navigator.java     ← Manejo de navegación entre pantallas
│   ├── component/         ← Componentes visuales reutilizables
│   ├── dialog/            ← Ventanas emergentes (modales)
│   ├── panel/             ← Pantallas principales de la app
│   └── table/             ← Modelos de datos para tablas Swing
└── util/                  ← Utilidades generales
```

---

## Capas de la aplicación

El proyecto sigue una arquitectura en capas. Cada capa tiene una responsabilidad clara y solo habla con la capa inmediatamente inferior.

```
UI  →  Service  →  Repository  →  Base de datos
```

---

## `model/` — Entidades

Representan las tablas de la base de datos. Usan anotaciones JPA (`@Entity`, `@Table`, `@Column`).

**Ejemplo: `Lote.java`**
```java
@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLote;

    private String numeroLote;
    private Double cantidadInicial;
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    private EstadoLote estado;  // DISPONIBLE, PROXIMO_VENCER, VENCIDO, RETIRADO

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private ProductoPerecible producto;
}
```

Cada clase en `model/` equivale a una tabla. Las relaciones entre tablas se expresan con `@ManyToOne`, `@OneToMany`, etc.

---

## `repository/` — Acceso a datos

Interfaces que extienden `JpaRepository`. Spring genera automáticamente las consultas simples a partir del nombre del método. Para consultas complejas se usa `@Query` con JPQL.

**Ejemplo: `LoteRepository.java`**
```java
public interface LoteRepository extends JpaRepository<Lote, Integer> {

    // Spring genera el SQL automáticamente
    List<Lote> findByEstado(EstadoLote estado);

    // Consulta personalizada en JPQL
    @Query("""
        SELECT l FROM Lote l
        WHERE l.fechaVencimiento BETWEEN :inicio AND :fin
          AND l.estado NOT IN (EstadoLote.RETIRADO, EstadoLote.VENCIDO)
        ORDER BY l.fechaVencimiento ASC
        """)
    List<Lote> findProximosAVencer(@Param("inicio") LocalDate inicio,
                                   @Param("fin") LocalDate fin);
}
```

Los repositorios **no contienen lógica de negocio**. Solo saben cómo buscar, guardar y eliminar datos.

---

## `service/` — Lógica de negocio

Clases anotadas con `@Service`. Aquí vive toda la lógica: validaciones, cálculos, orquestación de múltiples repositorios. Son el núcleo de la aplicación.

**Ejemplo: `InventarioServicio.java`** (simplificado)
```java
@Service
public class InventarioServicio {

    private final LoteRepository loteRepository;
    private final MermaRepository mermaRepository;

    // Las dependencias se inyectan por constructor
    public InventarioServicio(LoteRepository loteRepository,
                               MermaRepository mermaRepository) {
        this.loteRepository = loteRepository;
        this.mermaRepository = mermaRepository;
    }

    @Transactional
    public void registrarMerma(Lote lote, double cantidad, String motivo) {
        // 1. Valida que haya stock suficiente
        // 2. Descuenta la cantidad del lote
        // 3. Registra la merma en la tabla correspondiente
        lote.setCantidadActual(lote.getCantidadActual() - cantidad);
        loteRepository.save(lote);

        Merma merma = new Merma(lote, cantidad, motivo);
        mermaRepository.save(merma);
    }
}
```

La anotación `@Transactional` garantiza que si algo falla a mitad del método, todos los cambios se revierten (atomicidad).

---

## `enums/` — Valores fijos

Enumeraciones para representar estados o tipos que no cambian. Evitan usar cadenas de texto sueltas en el código.

**Ejemplo: `EstadoLote.java`**
```java
public enum EstadoLote {
    DISPONIBLE,
    PROXIMO_VENCER,
    VENCIDO,
    RETIRADO
}
```

En lugar de escribir `"DISPONIBLE"` como texto, se usa `EstadoLote.DISPONIBLE`. Así el compilador avisa si se escribe mal.

---

## `ui/` — Interfaz gráfica

La UI está construida con Java Swing y se divide en sub-carpetas según el tipo de componente.

### `ui/panel/` — Pantallas principales

Cada panel es una pantalla completa de la aplicación. Se muestran dentro del `MainFrame` según la navegación.

| Panel | Pantalla |
|---|---|
| `LoginPanel` | Inicio de sesión |
| `DashboardPanel` | Resumen general |
| `InventarioPanel` | Gestión de lotes |
| `AlertasPanel` | Alertas activas |
| `ConfiguracionPanel` | Configuración del supervisor |
| `ReportesPanel` | Generación de reportes |

### `ui/component/` — Componentes reutilizables

Piezas visuales pequeñas que se usan en múltiples paneles.

**Ejemplo: `StatusChip.java`** — un pequeño badge de color que muestra el estado de un lote (`DISPONIBLE`, `VENCIDO`, etc.).

### `ui/dialog/` — Ventanas emergentes

Formularios que aparecen sobre la pantalla principal para crear o editar datos.

**Ejemplo: `NuevoLoteDialog.java`** — formulario para registrar un nuevo lote de producto.

### `ui/table/` — Modelos de tabla

En Swing, las tablas (`JTable`) necesitan un modelo de datos separado. Estos modelos conectan la lista de objetos con las columnas de la tabla.

**Ejemplo: `LoteTableModel.java`**
```java
public class LoteTableModel extends AbstractTableModel {

    private final String[] columnas = {"Número", "Producto", "Cantidad", "Vence", "Estado"};
    private List<Lote> lotes;

    @Override
    public Object getValueAt(int fila, int columna) {
        Lote lote = lotes.get(fila);
        return switch (columna) {
            case 0 -> lote.getNumeroLote();
            case 1 -> lote.getProducto().getNombre();
            case 3 -> lote.getFechaVencimiento();
            case 4 -> lote.getEstado();
            default -> null;
        };
    }
}
```

---

## `config/` — Configuración de Spring

Clases que arrancan y configuran el contenedor de Spring.

| Archivo | Qué hace |
|---|---|
| `AppConfig.java` | Activa el escaneo de componentes y los repositorios JPA |
| `JpaConfig.java` | Configura la conexión a la base de datos y Hibernate |
| `SpringContext.java` | Expone el contexto de Spring para que la UI pueda acceder a los servicios |
| `DataSeeder.java` | Carga datos iniciales en la base de datos al arrancar |

---

## `theme/` — Estilos visuales

Centraliza los colores y tipografías para mantener un diseño consistente en toda la app. En lugar de poner colores directamente en cada panel, se referencian desde aquí.

```java
// En lugar de esto:
label.setForeground(new Color(220, 50, 50));

// Se usa esto:
label.setForeground(Theme.COLOR_ALERTA);
```

---

## `util/` — Utilidades

Clases de apoyo sin lógica de negocio.

| Clase | Qué hace |
|---|---|
| `SessionManager` | Guarda el usuario autenticado durante la sesión |
| `DateParser` | Convierte textos a fechas y viceversa |
| `KeyboardHandler` | Maneja atajos de teclado globales |

---

## Flujo de una acción típica

Para entender cómo se conectan todas las capas, este es el recorrido que hace una acción desde que el usuario hace clic hasta que el dato se guarda:

```
1. Usuario hace clic en "Registrar Merma" (InventarioPanel)
        ↓
2. El panel llama a InventarioServicio.registrarMerma(...)
        ↓
3. El servicio valida los datos y llama a LoteRepository y MermaRepository
        ↓
4. Los repositorios ejecutan los INSERT/UPDATE en la base de datos
        ↓
5. El servicio retorna el resultado al panel
        ↓
6. El panel actualiza la tabla en pantalla
```

---

## `mock/` — Datos de prueba

`MockData.java` contiene datos ficticios (productos, lotes, usuarios) que se pueden cargar durante el desarrollo para no depender de una base de datos real poblada.
