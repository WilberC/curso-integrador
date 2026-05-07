# Fase B — Repositorios (Spring Data JPA)

Un repositorio por entidad. Todos extienden `JpaRepository<T, Integer>`.

---

## UsuarioRepository

- [ ] Crear `repository/UsuarioRepository.java`:
  ```java
  public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
      Optional<Usuario> findByEmail(String email);
      List<Usuario> findByRol(RolUsuario rol);
      List<Usuario> findByRolAndActivo(RolUsuario rol, boolean activo);
  }
  ```

---

## CategoriaRepository

- [ ] Crear `repository/CategoriaRepository.java`:
  ```java
  public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
      Optional<Categoria> findByNombreIgnoreCase(String nombre);
      List<Categoria> findAllByOrderByNombreAsc();
  }
  ```

---

## ProductoPerecibleRepository

- [ ] Crear `repository/ProductoPerecibleRepository.java`:
  ```java
  public interface ProductoPerecibleRepository extends JpaRepository<ProductoPerecible, Integer> {
      List<ProductoPerecible> findByCategoriaId(Integer idCategoria);
      List<ProductoPerecible> findByNombreContainingIgnoreCase(String nombre);
  }
  ```

---

## LoteRepository

- [ ] Crear `repository/LoteRepository.java` — el repositorio con más queries custom:
  ```java
  public interface LoteRepository extends JpaRepository<Lote, Integer> {
      List<Lote> findByEstado(EstadoLote estado);

      List<Lote> findByEstadoNot(EstadoLote estado);  // todos los no-RETIRADO

      // Lotes cuya fecha de vencimiento es anterior a 'fecha' y no están RETIRADOS
      List<Lote> findByFechaVencimientoBeforeAndEstadoNot(LocalDate fecha, EstadoLote estado);

      // Lotes de un producto específico que no están RETIRADOS
      List<Lote> findByProductoIdAndEstadoNot(Integer idProducto, EstadoLote estado);

      // Lotes próximos a vencer: vencimiento entre hoy y hoy+N días, no RETIRADO ni VENCIDO
      @Query("""
          SELECT l FROM Lote l
          WHERE l.fechaVencimiento BETWEEN :inicio AND :fin
            AND l.estado NOT IN ('RETIRADO', 'VENCIDO')
          ORDER BY l.fechaVencimiento ASC
      """)
      List<Lote> findProximosAVencer(
          @Param("inicio") LocalDate inicio,
          @Param("fin") LocalDate fin
      );
  }
  ```

---

## MovimientoInventarioRepository

- [ ] Crear `repository/MovimientoInventarioRepository.java`:
  ```java
  public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {
      List<MovimientoInventario> findByLoteIdLote(Integer idLote);
      List<MovimientoInventario> findByUsuarioIdUsuario(Integer idUsuario);
      List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);
  }
  ```

---

## AlertaRepository

- [ ] Crear `repository/AlertaRepository.java`:
  ```java
  public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
      List<Alerta> findByEstado(EstadoAlerta estado);
      List<Alerta> findByLoteIdLote(Integer idLote);
      Optional<Alerta> findByLoteIdLoteAndEstado(Integer idLote, EstadoAlerta estado);
      long countByEstado(EstadoAlerta estado);
  }
  ```

---

## ReporteRepository

- [ ] Crear `repository/ReporteRepository.java`:
  ```java
  public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
      List<Reporte> findByTipo(TipoReporte tipo);
      List<Reporte> findByFechaGeneracionBetween(LocalDateTime inicio, LocalDateTime fin);
      List<Reporte> findByUsuarioIdUsuario(Integer idUsuario);
  }
  ```

---

## ConfiguracionAlertaRepository

- [ ] Crear `repository/ConfiguracionAlertaRepository.java`:
  ```java
  public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, Integer> {
      Optional<ConfiguracionAlerta> findFirstByActivoTrue();  // configuración global activa
      Optional<ConfiguracionAlerta> findByUsuarioConfigIdUsuario(Integer idUsuario);
  }
  ```

---

## Verificación Phase B — Repositorios

- [ ] `UsuarioRepository.findByEmail("operario@plazavea.com")` devuelve el usuario sembrado
- [ ] `LoteRepository.findByEstado(EstadoLote.VENCIDO)` devuelve solo lotes vencidos
- [ ] `LoteRepository.findProximosAVencer(LocalDate.now(), LocalDate.now().plusDays(7))` devuelve los lotes correctos
- [ ] `AlertaRepository.countByEstado(EstadoAlerta.PENDIENTE)` devuelve el conteo esperado
- [ ] `ConfiguracionAlertaRepository.findFirstByActivoTrue()` devuelve la config sembrada con diasAmarilla=7
