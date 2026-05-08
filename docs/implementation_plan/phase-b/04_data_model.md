# Fase B — Modelo de Datos (Entidades JPA)

Prerequisito: Phase A completa y aprobada. Solo entonces agregar JPA al proyecto.

---

## Actualizar build.gradle

- [x] Agregar dependencias de Phase B al `build.gradle`:
  ```groovy
  dependencies {
      // ... dependencias de Phase A ...

      // JPA + Hibernate
      implementation 'org.springframework.data:spring-data-jpa:3.2.5'
      implementation 'org.hibernate.orm:hibernate-core:6.4.4.Final'

      // PostgreSQL
      runtimeOnly 'org.postgresql:postgresql:42.7.3'

      // .env reader
      implementation 'io.github.cdimascio:dotenv-java:3.0.0'

      // BCrypt (para contraseñas)
      implementation 'org.mindrot:jbcrypt:0.4'
  }
  ```

---

## Configuración JPA

- [x] Crear `.env.example`:
  ```
  POSTGRES_DB=plazavea_perecibles
  POSTGRES_USER=plazavea
  POSTGRES_PASSWORD=changeme
  DB_URL=jdbc:postgresql://localhost:5432/plazavea_perecibles
  DB_DDL_AUTO=create-drop
  ```

- [x] Crear `config/JpaConfig.java`:
  - Usa `Dotenv.load()` para leer el `.env`
  - Crea `EntityManagerFactory` con propiedades Hibernate:
    - `hibernate.dialect` → `PostgreSQLDialect`
    - `hibernate.hbm2ddl.auto` → valor de `DB_DDL_AUTO` (usar `create-drop` en dev, `validate` en prod)
    - `hibernate.show_sql` → `true` (solo en dev)
  - Crea `JpaTransactionManager`

- [x] Crear `config/SpringContext.java` — `ApplicationContext` estático para inyección manual en controladores JavaFX:
  ```java
  public class SpringContext {
      private static ApplicationContext context;
      public static void init(ApplicationContext ctx) { context = ctx; }
      public static <T> T getBean(Class<T> clazz) { return context.getBean(clazz); }
  }
  ```

---

## Enums (ya existen de Phase A — agregar @Column si hace falta)

Los enums creados en Phase A se usan directamente con `@Enumerated(EnumType.STRING)` en las entidades. No requieren cambios.

---

## Entidades JPA

> Nota: Reemplazar los POJOs de Phase A con estas entidades anotadas. Mantener los mismos nombres de campo para que `MockData` siga compilando durante la transición.

### Usuario

- [x] Crear `model/Usuario.java`:
  ```java
  @Entity @Table(name = "usuario")
  public class Usuario {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Integer idUsuario;

      @Column(nullable = false, length = 100)
      private String nombre;

      @Column(nullable = false, length = 100)
      private String apellido;

      @Column(nullable = false, unique = true, length = 150)
      private String email;

      @Column(nullable = false, length = 255)
      private String contrasena;  // almacenada como hash BCrypt

      @Enumerated(EnumType.STRING)
      @Column(nullable = false, length = 20)
      private RolUsuario rol;

      @Column(nullable = false)
      private boolean activo = true;

      @Column(nullable = false)
      private LocalDate fechaCreacion;

      // Métodos: getNombreCompleto(), esSupervisor()
  }
  ```

### Categoria

- [x] Crear `model/Categoria.java`:
  - `idCategoria` (PK), `nombre` (unique, 80), `descripcion` (255)

### ProductoPerecible

- [x] Crear `model/ProductoPerecible.java`:
  - `idProducto` (PK), `nombre` (150), `descripcion`, `unidadMedida` (30)
  - `@ManyToOne @JoinColumn(name = "id_categoria") Categoria categoria`

### Lote

- [x] Crear `model/Lote.java` — entidad central:
  ```java
  @Entity @Table(name = "lote")
  public class Lote {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Integer idLote;

      @Column(name = "numero_lote", nullable = false, length = 50)
      private String numeroLote;

      @Column(name = "cantidad_inicial", nullable = false)
      private Double cantidadInicial;

      @Column(name = "cantidad_actual", nullable = false)
      private Double cantidadActual;

      private LocalDate fechaIngreso;
      private LocalDate fechaVencimiento;

      @Column(length = 100)
      private String ubicacion;

      @Enumerated(EnumType.STRING)
      @Column(nullable = false, length = 20)
      private EstadoLote estado = EstadoLote.DISPONIBLE;

      @ManyToOne @JoinColumn(name = "id_producto", nullable = false)
      private ProductoPerecible producto;

      @ManyToOne @JoinColumn(name = "id_usuario_reg", nullable = false)
      private Usuario usuarioRegistro;

      // Métodos de dominio — no persistidos:
      public long getDiasParaVencer() {
          return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
      }
      public boolean estaVencido() { return getDiasParaVencer() < 0; }
      public boolean estaProximoAVencer(int umbralDias) {
          long dias = getDiasParaVencer();
          return dias >= 0 && dias <= umbralDias;
      }
      public void actualizarEstado(int diasAmarilla, int diasRoja) {
          if (estaVencido()) estado = EstadoLote.VENCIDO;
          else if (estaProximoAVencer(diasAmarilla)) estado = EstadoLote.PROXIMO_VENCER;
          else estado = EstadoLote.DISPONIBLE;
      }
  }
  ```

### MovimientoInventario

- [x] Crear `model/MovimientoInventario.java`:
  - `idMovimiento` (PK), `tipo (TipoMovimiento)`, `cantidad`, `fechaMovimiento (LocalDateTime)`, `motivo`
  - `@ManyToOne Lote lote`
  - `@ManyToOne Usuario usuario`
  - **Sin métodos de modificación** — es insert-only (audit trail)

### Merma

- [x] Crear `model/Merma.java`:
  - `idMerma` (PK), `cantidad`, `fechaRegistro (LocalDateTime)`, `motivo`
  - `@ManyToOne Lote lote` (NOT NULL)
  - `@ManyToOne MovimientoInventario movimiento` (`@JoinColumn(nullable = true)`)
  - `@ManyToOne Usuario usuario`

### Alerta

- [x] Crear `model/Alerta.java`:
  - `idAlerta` (PK), `tipoAlerta (TipoAlerta)`, `diasParaVencer (int)`, `fechaGeneracion (LocalDateTime)`, `estado (EstadoAlerta)` default PENDIENTE
  - `@ManyToOne Lote lote`
  - `@ManyToOne @JoinColumn(nullable = true) Usuario usuarioAtiende`
  - Método `esCritica()`: `tipoAlerta == VENCIDO || diasParaVencer <= 2`

### Reporte

- [x] Crear `model/Reporte.java`:
  - `idReporte` (PK), `tipo (TipoReporte)`, `fechaGeneracion (LocalDateTime)`, `fechaInicio (LocalDate)`, `fechaFin (LocalDate)`
  - `@ManyToOne Usuario usuario`

### ConfiguracionAlerta

- [x] Crear `model/ConfiguracionAlerta.java`:
  - `idConfig` (PK), `diasAlertaAmarilla (int)` default 7, `diasAlertaRoja (int)` default 2, `activo (boolean)` default true
  - `@OneToOne @JoinColumn(name = "id_usuario_config") Usuario usuarioConfig`

---

## Docker + Base de Datos

- [x] Crear `docker-compose.yml`:
  ```yaml
  services:
    db:
      image: postgres:16
      env_file: .env
      environment:
        POSTGRES_DB: ${POSTGRES_DB}
        POSTGRES_USER: ${POSTGRES_USER}
        POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      ports:
        - "5432:5432"
      volumes:
        - pgdata:/var/lib/postgresql/data
  volumes:
    pgdata:
  ```

- [x] Copiar `.env.example` a `.env` y agregar `.env` al `.gitignore`

---

## Verificación Phase B — Data Model

- [x] `docker-compose up -d` levanta PostgreSQL sin errores
- [x] `./gradlew run` con `DB_DDL_AUTO=create-drop` crea las 9 tablas (verificar con `psql` o DBeaver)
- [x] Tablas creadas: `usuario`, `categoria`, `producto_perecible`, `lote`, `movimiento_inventario`, `merma`, `alerta`, `reporte`, `configuracion_alerta`
- [x] `Lote.getDiasParaVencer()` devuelve el valor correcto con fechas reales
- [x] `Lote.actualizarEstado(7, 2)` cambia el estado según los días calculados
