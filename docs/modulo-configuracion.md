# Módulo: Configuración

Panel exclusivo del rol **Supervisor**. Centraliza los parámetros del sistema que hoy están fijos en el código, más la gestión de usuarios.

---

## Secciones

### 1. Umbrales de Alertas

Controla cuándo el sistema genera una alerta según los días restantes al vencimiento.

| Parámetro | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| Días críticos | Días restantes para alerta roja | 1 |
| Días de advertencia | Días restantes para alerta amarilla | 3 |
| Días de aviso anticipado | Días restantes para alerta verde informativa | 7 |

- El Dashboard y el panel de Alertas usan estos valores para colorear chips y calcular urgencia.
- Cambiar un umbral se refleja inmediatamente en toda la sesión activa.

---

### 2. Gestión de Usuarios

Lista de cuentas registradas en el sistema con sus roles.

**Columnas de la tabla:**

| Campo | Descripción |
|-------|-------------|
| Usuario | Nombre de acceso |
| Nombre completo | Nombre real del empleado |
| Rol | Operario / Supervisor |
| Estado | Activo / Inactivo |

**Acciones disponibles:**

- **Nuevo usuario** — abre diálogo con campos: usuario, nombre completo, contraseña, rol.
- **Editar** — permite cambiar nombre completo, rol y estado. No permite cambiar el nombre de usuario.
- **Desactivar / Activar** — no elimina el registro; solo cambia el estado para preservar el historial.

**Restricciones:**

- El Supervisor activo no puede desactivarse a sí mismo.
- Debe existir al menos un Supervisor activo en todo momento.
- El campo usuario no acepta espacios ni caracteres especiales.

---

### 3. Categorías de Productos

Lista de categorías disponibles al registrar un nuevo lote en Inventario.

**Columnas:**

| Campo | Descripción |
|-------|-------------|
| Nombre | Nombre de la categoría (ej. Lácteos, Carnes) |
| Estado | Activa / Inactiva |

**Acciones:**

- **Nueva categoría** — agrega una categoría al listado.
- **Desactivar** — oculta la categoría del formulario de registro sin eliminarla (los lotes existentes la conservan).

**Categorías iniciales sugeridas:** Lácteos, Carnes, Embutidos, Panadería, Frutas y Verduras, Otros.

---

## Navegación y acceso

- Solo visible en el `SidebarPanel` cuando el usuario autenticado tiene rol **Supervisor**.
- El operario no ve la opción en el menú lateral; si intenta acceder directamente, se muestra un mensaje de acceso denegado.
- Atajo de teclado: `Ctrl + ,` (convención estándar de configuración).

---

## Persistencia

- Los umbrales de alertas se guardan en la tabla `configuracion` (una sola fila con clave-valor o columnas fijas).
- Los usuarios se guardan en la tabla `usuarios` con contraseña hasheada (bcrypt).
- Las categorías se guardan en la tabla `categorias`.

---

## Lo que NO incluye este módulo

- Configuración de impresoras o exportación (pertenece a Reportes).
- Parámetros de conexión a base de datos (se configuran fuera de la app, en `application.properties`).
- Auditoría o bitácora de cambios (fuera del alcance del proyecto).
