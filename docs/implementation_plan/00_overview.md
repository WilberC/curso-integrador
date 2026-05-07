# Plan de Implementación — Visión General

## ¿Qué construimos?

Un sistema de escritorio JavaFX para Plaza Vea que digitaliza el control de productos perecibles: registro de lotes, seguimiento de fechas de vencimiento, alertas automáticas y reportes de mermas. Usuarios: Operarios y Supervisores.

---

## Cómo usar este plan

Cada archivo contiene tareas con checkboxes `- [ ]`. Marca cada tarea `- [x]` al completarla. Los archivos están ordenados — no saltes de fase sin terminar la anterior.

```
docs/implementation_plan/
├── 00_overview.md              ← este archivo
├── phase-a/
│   ├── 01_setup.md             ← estructura del proyecto + Gradle + MockData
│   ├── 02_screens.md           ← 6 pantallas JavaFX con datos simulados
│   └── 03_ux_features.md       ← parser de fechas, teclado, gauges
└── phase-b/
    ├── 04_data_model.md        ← entidades JPA + enums
    ├── 05_repositories.md      ← Spring Data repos
    ├── 06_services.md          ← lógica de negocio
    └── 07_wiring.md            ← reemplazar mocks + Docker + jpackage
```

---

## Fases

| Fase | Nombre | Objetivo principal | Entregable |
|------|--------|--------------------|------------|
| **A** | UI First | Validar UX y flujos antes de tocar la DB | App funcional con datos en memoria |
| **B** | Logic & DB | Persistencia real y automatización | App completa lista para producción |

**Principio:** Los controladores JavaFX no deben cambiar su interfaz entre fases. En Fase A leen de `MockData`; en Fase B leen de servicios reales. El contrato es el mismo.

---

## Criterios de Éxito

### Fase A completa cuando:
- [ ] Las 6 pantallas navegan correctamente entre sí
- [ ] La tabla de Inventario muestra filas con colores por estado (verde/naranja/rojo)
- [ ] El parser de lenguaje natural resuelve `"Hoy + 5"` a la fecha correcta
- [ ] Los atajos `N`, `V`, `R`, `Ctrl+G/I/A/R`, `F5` funcionan
- [ ] El dashboard muestra los 4 gauges con cambio de color por umbral
- [ ] El rol Operario no ve la pantalla de Reportes

### Fase B completa cuando:
- [ ] `docker-compose up` levanta PostgreSQL sin errores
- [ ] Hibernate genera las 9 tablas en el primer arranque
- [ ] El login autentica contra la base de datos real
- [ ] Registrar un nuevo Lote persiste y reaparece tras reiniciar
- [ ] `AlertaServicio` genera Alertas automáticamente para lotes próximos a vencer
- [ ] Los reportes exportan como archivo CSV abrible
- [ ] `./gradlew jpackage` produce un instalador ejecutable

---

## Fuera del Alcance

- Aplicación web o móvil
- Productos no perecibles
- Integración con ERP externo (Saga, SAP)
- Notificaciones por email o SMS
- Múltiples tiendas en red (la app opera standalone por tienda)

---

## Entidades del Dominio (resumen rápido)

| Entidad | Descripción |
|---------|-------------|
| `Usuario` | Operario o Supervisor autenticado |
| `Categoria` | Agrupación de productos (Lácteos, Carnes, etc.) |
| `ProductoPerecible` | Tipo de producto (Leche Gloria 1L) |
| `Lote` | Unidad de inventario con fecha de vencimiento y cantidad |
| `MovimientoInventario` | Auditoría de cada cambio en un lote |
| `Merma` | Pérdida registrada (vencimiento, daño, donación) |
| `Alerta` | Notificación generada cuando un lote se acerca a vencer |
| `Reporte` | Metadata de un reporte generado (Stock / Mermas / Vencidos) |
| `ConfiguracionAlerta` | Umbrales de días para alertas amarilla y roja |

---

## Enums (6)

| Enum | Valores |
|------|---------|
| `RolUsuario` | OPERARIO, SUPERVISOR |
| `EstadoLote` | DISPONIBLE, PROXIMO_VENCER, VENCIDO, RETIRADO |
| `TipoMovimiento` | INGRESO, RETIRO, AJUSTE, REMATE, DONACION |
| `TipoAlerta` | PROXIMO_VENCER, VENCIDO |
| `EstadoAlerta` | PENDIENTE, ATENDIDA, IGNORADA |
| `TipoReporte` | STOCK, VENCIDOS, PROXIMOS_VENCER, MERMAS |
