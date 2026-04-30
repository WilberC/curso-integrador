# Capítulo 1

## 1.1 Descripción del Problema

Supermercados Peruanos S.A. – Plaza Vea gestiona diariamente un elevado volumen de productos perecibles como lácteos, carnes, embutidos y productos de panadería. Actualmente, el control de fechas de vencimiento se realiza de forma manual: el personal de piso recorre los anaqueles y cámaras de frío, revisa físicamente cada producto y anota en planillas los artículos próximos a caducar. Este proceso depende completamente de la disponibilidad y atención del operario, lo que genera omisiones frecuentes.

Como consecuencia, los productos vencidos no se retiran a tiempo de los anaqueles, la rotación de inventario suele ser tardía y el registro de mermas se hace de manera inconsistente o no se registra en absoluto. Esto dificulta que los supervisores conozcan en tiempo real cuántos productos están en riesgo de caducar y en qué sección de la tienda se encuentran.

Esta situación genera pérdidas económicas directas por mermas, expone a la empresa a sanciones sanitarias por comercializar productos vencidos y deteriora la confianza del consumidor. Además, el desperdicio de alimentos resultante representa un impacto ambiental y social evitable que contradice las políticas de sostenibilidad del grupo Intercorp.

## 1.2 Definición de Objetivos

### 1.2.1 Objetivo General

Desarrollar un sistema informático de control de productos perecibles que centralice la gestión del inventario y el seguimiento automatizado de fechas de vencimiento, permitiendo reducir en un 30% las mermas por productos vencidos durante el primer año de implementación en las tiendas piloto de Lima.

### 1.2.2 Objetivos Específicos

- Registrar el ingreso de productos perecibles con sus datos básicos (nombre, categoría, lote, cantidad y fecha de vencimiento).
- Actualizar el estado y la cantidad de los productos perecibles conforme se realicen movimientos en el inventario.
- Consultar el inventario de productos perecibles para monitorear su estado, ubicación y proximidad a la fecha de vencimiento.
- Eliminar o dar de baja los productos vencidos o retirados del inventario, registrando la merma correspondiente.
- Generar reportes y alertas automáticas sobre el estado del inventario y los productos próximos a caducar.

## 1.3 Alcances y Limitaciones

### 1.3.1 Alcances

- Permitir el registro detallado de productos perecibles con información relevante como nombre, categoría, lote, cantidad y fecha de vencimiento.
- Monitorear en tiempo real el estado del inventario de productos perecibles disponibles en almacén y área de ventas.
- Generar alertas automáticas sobre productos próximos a vencer según parámetros configurables de anticipación.
- Facilitar la elaboración de reportes periódicos sobre stock, productos vencidos, productos próximos a vencer y mermas registradas.
- Aplicar control de acceso por roles de usuario (operario y supervisor).
- Operar en las tiendas piloto de Lima como aplicación de escritorio instalada en los equipos de la tienda.
- Cubrir exclusivamente productos perecibles; los no perecibles quedan fuera del alcance inicial.

### 1.3.2 Limitaciones

- No se puede ejecutar en dispositivos móviles.
- No se puede ejecutar por navegador web.

## 1.4 Justificación

La implementación de un sistema de control de productos perecibles para Plaza Vea responde a la necesidad de reemplazar un proceso manual propenso a errores por uno automatizado, confiable y trazable. El proceso actual (basado en revisiones físicas y registros en papel) no escala con el volumen de productos que maneja una tienda de gran superficie, y expone a la empresa a pérdidas económicas evitables y riesgos sanitarios. Automatizar el seguimiento de fechas de vencimiento y el registro de mermas permitirá reducir errores humanos, liberar tiempo del personal operativo y dar a los supervisores visibilidad en tiempo real sobre el estado del inventario.

Desde una perspectiva estratégica, contar con información precisa sobre el inventario de perecibles permite a Plaza Vea mejorar sus márgenes operativos, fortalecer el cumplimiento normativo sanitario y alinear sus operaciones con compromisos de sostenibilidad. Un sistema de este tipo también sienta las bases para escalar la solución a otras sedes a nivel nacional, generando un impacto positivo sostenido en la rentabilidad y en la reducción del desperdicio de alimentos.

## 1.5 Estado del Arte

### 1.5.1 Procesamiento de Lenguaje Natural (Motor Soulver)

- **Referencia de Interfaz:** Se observa un motor de cálculo que interpreta sintaxis humana para resolver operaciones complejas y conversiones de tiempo en tiempo real.
- **Función:** Resolución de lógica temporal y aritmética de inventario mediante procesamiento de lenguaje natural (NLP).
- **Se usará para:** Transformar entradas relativas como "Hoy + 12 días" o "Vence en 3 semanas" en fechas de calendario y cálculos de vigencia automatizados.
- **Impacto esperado:** Optimización del flujo de recepción de mercancía y eliminación del margen de error en cálculos manuales de expiración.

> *Figura 1*

### 1.5.2 Interfaz de Alta Velocidad (Prioridad al Teclado - Godspeed)

- **Referencia de Interfaz:** Se visualiza un sistema de navegación basado en comandos directos, donde cada acción tiene una tecla asignada visible para una ejecución instantánea sin depender de punteros (él mouse).
- **Función:** Control total del sistema mediante atajos de teclado y navegación secuencial.
- **Se usará para:** Operar el inventario en situaciones de movilidad (laptops en carritos o tabletas con teclado) mediante teclas de acceso rápido para acciones críticas como marcar vencimientos (V), remates (R) o nuevas entradas (N).
- **Impacto esperado:** Incremento masivo en la velocidad de captura de datos y reducción de la fatiga operativa al eliminar la precisión requerida por el mouse.

> *Figura 2*

### 1.5.3 Medidores de Riesgo (Gauges & Stat Panels)

- **Referencia de Interfaz:** Se observa un tablero de control con paneles de alto contraste que utilizan códigos de color (semáforo), métricas de gran tamaño y gráficos de tendencia para comunicar estados críticos de un vistazo.
- **Función:** Visualización de indicadores de rendimiento y riesgo mediante elementos gráficos dinámicos y legibilidad de alto impacto.
- **Se usará para:** Monitorear en tiempo real el porcentaje de stock próximo a vencer (menos de 24 horas) y determinar la activación inmediata de protocolos de remate o donación.
- **Impacto esperado:** Toma de decisiones acelerada y reducción de mermas gracias a la identificación visual inmediata de alertas críticas sin procesar tablas de datos.

> *Figura 3*

---

# Capítulo 2: Marco Teórico

## 2.1 Marco Teórico del Sistema

### 2.1.1 Java NetBeans y Base de Datos

#### Java NetBeans

**Definición**

NetBeans IDE ofrece herramientas de primera clase para el desarrollo de aplicaciones web, corporativas, de escritorio y móviles con Java. Siempre es el primer IDE en ofrecer soporte para las últimas versiones de JDK, Java EE y JavaFX. Proporciona descripciones generales inteligentes para ayudarle a comprender y gestionar sus aplicaciones, lo que incluye el soporte inmediato para tecnologías populares, como Maven.

Contiene tecnologías innovadoras listas para usar y es el estándar en el desarrollo de aplicaciones, gracias a sus características integrales para el desarrollo de aplicaciones, las constantes mejoras en el editor de Java y el perfeccionamiento del rendimiento y la velocidad.

**Herencia**

Es una característica de la programación orientada a objetos que permite a las clases definirse a partir de otras, permitiendo reutilizar su funcionalidad. A la clase de la que se hereda se le denomina superclase o clase base, mientras que la clase que hereda recibe el nombre de subclase o clase derivada.

**Polimorfismo**

Es la capacidad de la programación orientada a objetos que permite que un mismo mensaje o método funcione de manera distinta según el objeto sobre el que se aplica. En este concepto, el código no necesita especificar el tipo concreto del objeto con el que trabaja, sino que opera de forma genérica sobre un conjunto de objetos compatibles entre sí.

**Arrays**

Un array en Java es una estructura de datos que permite almacenar una colección de elementos del mismo tipo. Su tamaño se define al momento de su declaración y no puede modificarse en tiempo de ejecución, a diferencia de lo que ocurre en otros lenguajes de programación.

#### Base de Datos

**SQL Server**

Es un sistema de gestión de bases de datos relacionales (RDBMS) desarrollado y respaldado por Microsoft, basado en el lenguaje de consulta estructurado (SQL). Está diseñado para funcionar principalmente en la plataforma Windows, ofreciendo una solución robusta y confiable para el almacenamiento y administración de datos empresariales.

### 2.1.2 Breve Reseña Histórica de la Empresa

Plaza Vea es una cadena de supermercados perteneciente a Supermercados Peruanos S.A., empresa que forma parte del Grupo Intercorp. Fue fundada en 1993 bajo el nombre de Santa Isabel, y en el año 2003 fue adquirida por el Grupo Intercorp, renombrándose como Plaza Vea. Desde entonces, ha experimentado un crecimiento sostenido, consolidándose como una de las cadenas de supermercados más importantes del Perú, con presencia en Lima y diversas ciudades del interior del país. Su modelo de negocio se basa en ofrecer una amplia variedad de productos de consumo masivo, destacando su compromiso con la calidad, la accesibilidad y la satisfacción del cliente.

### 2.1.3 Definición del Sistema

El Sistema de Control de Productos Perecibles para Plaza Vea es una aplicación de escritorio desarrollada en Java con NetBeans IDE y SQL Server como gestor de base de datos. Su propósito es centralizar y automatizar el seguimiento del inventario de productos perecibles, permitiendo registrar ingresos, monitorear fechas de vencimiento, generar alertas automáticas y producir reportes de mermas. El sistema está orientado al uso del personal operativo y supervisores de tienda, con control de acceso por roles, contribuyendo a reducir pérdidas económicas y garantizar el cumplimiento de las normas sanitarias vigentes.

### 2.1.4 Descripción de la Secuencia del Proceso Actual del Sistema

**Paso N° 1**
El personal operativo recorre los anaqueles y cámaras de frío de la tienda para revisar físicamente los productos perecibles disponibles.

**Paso N° 2**
El operario identifica manualmente los productos próximos a vencer, revisando una por una las fechas de vencimiento impresas en cada artículo.

**Paso N° 3**
El encargado anota en una planilla o cuadernillo los productos detectados, registrando su nombre, cantidad y fecha de vencimiento.

**Paso N° 4**
El operario informa al supervisor sobre los productos en riesgo de caducar para que este tome una decisión (remate, donación o retiro).

**Paso N° 5**
El supervisor indica al personal qué productos deben ser retirados de los anaqueles y los da de baja del inventario de forma manual.

**Paso N° 6**
El encargado registra la merma correspondiente en los documentos internos de la tienda, sin un sistema centralizado que valide ni consolide la información.
