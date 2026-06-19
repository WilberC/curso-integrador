-- Reset operational demo data while preserving all users.
-- Run after the application has created the schema and users.

BEGIN;

DO $$
DECLARE
    supervisor_id integer;
    operario_id integer;
    prod_id integer;
    lot_id integer;
    movimiento_id integer;
    total_lotes integer := 0;
    target_count integer;
    status_name text;
    i integer;
    initial_qty integer;
    current_qty integer;
    ingreso date;
    vencimiento date;
    ubicacion text;
    numero_lote text;
    dias integer;
    alert_state text;
    alert_type text;
    motivo text;
    unidades_merma integer;
    tipo_mov text;
BEGIN
    SELECT id_usuario INTO supervisor_id
    FROM usuarios
    WHERE rol = 'SUPERVISOR' AND activo = true
    ORDER BY id_usuario
    LIMIT 1;

    SELECT id_usuario INTO operario_id
    FROM usuarios
    WHERE rol = 'OPERARIO' AND activo = true
    ORDER BY id_usuario
    LIMIT 1;

    IF supervisor_id IS NULL THEN
        SELECT id_usuario INTO supervisor_id FROM usuarios ORDER BY id_usuario LIMIT 1;
    END IF;

    IF operario_id IS NULL THEN
        operario_id := supervisor_id;
    END IF;

    IF supervisor_id IS NULL THEN
        RAISE EXCEPTION 'No users found. Create/login seed users first, then run this script.';
    END IF;

    TRUNCATE TABLE
        reporte,
        alerta,
        merma,
        movimiento_inventario,
        lote,
        producto_perecible,
        categorias,
        configuracion
    RESTART IDENTITY CASCADE;

    INSERT INTO configuracion (
        dias_criticos,
        dias_advertencia,
        dias_aviso_anticipado,
        activo,
        id_usuario_config
    ) VALUES (1, 3, 7, true, supervisor_id);

    INSERT INTO categorias (nombre, descripcion, activo) VALUES
        ('Lacteos', 'Leches, yogures, quesos y derivados refrigerados', true),
        ('Carnes', 'Cortes frescos de pollo, res y cerdo', true),
        ('Embutidos', 'Jamonadas, chorizos, salchichas y fiambres', true),
        ('Panaderia', 'Panes envasados, masas y productos de vitrina', true),
        ('Frutas y Verduras', 'Productos frescos de alta rotacion', true),
        ('Comidas Preparadas', 'Platos listos, ensaladas y combos refrigerados', true),
        ('Congelados', 'Productos congelados con control de vencimiento', true),
        ('Pescados y Mariscos', 'Pescados, filetes y mariscos refrigerados', true);

    INSERT INTO producto_perecible (nombre, descripcion, unidad_medida, id_categoria, activo)
    SELECT product_name, product_desc, unit_name, c.id_categoria, true
    FROM (
        VALUES
            ('Leche fresca entera 1L', 'Bolsa refrigerada de alta rotacion', 'unidad', 'Lacteos'),
            ('Yogurt bebible fresa 1L', 'Yogurt familiar refrigerado', 'unidad', 'Lacteos'),
            ('Queso fresco 250g', 'Queso fresco empacado', 'unidad', 'Lacteos'),
            ('Mantequilla con sal 200g', 'Barra refrigerada', 'unidad', 'Lacteos'),
            ('Pechuga de pollo fresca', 'Bandeja sellada', 'kg', 'Carnes'),
            ('Pollo entero beneficiado', 'Unidad refrigerada', 'kg', 'Carnes'),
            ('Carne molida premium', 'Bandeja de res molida', 'kg', 'Carnes'),
            ('Chuleta de cerdo familiar', 'Bandeja porcionada', 'kg', 'Carnes'),
            ('Jamon del pais tajado', 'Empaque de fiambre', 'unidad', 'Embutidos'),
            ('Salchicha viena 12 und', 'Paquete refrigerado', 'paquete', 'Embutidos'),
            ('Chorizo parrillero', 'Paquete refrigerado', 'paquete', 'Embutidos'),
            ('Pechuga de pavo tajada', 'Fiambre bajo en grasa', 'unidad', 'Embutidos'),
            ('Pan molde integral', 'Bolsa de pan de molde', 'unidad', 'Panaderia'),
            ('Pan frances embolsado', 'Pack para consumo diario', 'paquete', 'Panaderia'),
            ('Croissant mantequilla', 'Producto de vitrina', 'unidad', 'Panaderia'),
            ('Masa para pizza familiar', 'Masa refrigerada lista para hornear', 'unidad', 'Panaderia'),
            ('Lechuga americana', 'Unidad fresca', 'unidad', 'Frutas y Verduras'),
            ('Fresa seleccionada 500g', 'Bandeja de fruta fresca', 'unidad', 'Frutas y Verduras'),
            ('Palta fuerte malla', 'Malla de paltas', 'kg', 'Frutas y Verduras'),
            ('Tomate italiano malla', 'Malla de tomate fresco', 'kg', 'Frutas y Verduras'),
            ('Ensalada lista cesar', 'Bowl refrigerado', 'unidad', 'Comidas Preparadas'),
            ('Pollo rostizado porcionado', 'Porcion lista para calentar', 'unidad', 'Comidas Preparadas'),
            ('Arroz chaufa familiar', 'Bandeja preparada', 'unidad', 'Comidas Preparadas'),
            ('Lasagna personal', 'Bandeja refrigerada', 'unidad', 'Comidas Preparadas'),
            ('Nuggets de pollo congelados', 'Bolsa congelada', 'paquete', 'Congelados'),
            ('Hamburguesa de res congelada', 'Caja congelada', 'caja', 'Congelados'),
            ('Verduras mixtas congeladas', 'Bolsa congelada', 'paquete', 'Congelados'),
            ('Helado vainilla 1L', 'Pote congelado', 'unidad', 'Congelados'),
            ('Filete de tilapia', 'Bandeja refrigerada', 'kg', 'Pescados y Mariscos'),
            ('Langostino pelado', 'Bolsa refrigerada', 'kg', 'Pescados y Mariscos'),
            ('Trucha entera limpia', 'Unidad refrigerada', 'kg', 'Pescados y Mariscos'),
            ('Mixto para ceviche', 'Bandeja refrigerada', 'kg', 'Pescados y Mariscos')
    ) AS p(product_name, product_desc, unit_name, category_name)
    JOIN categorias c ON c.nombre = p.category_name;

    FOREACH status_name IN ARRAY ARRAY['DISPONIBLE', 'PROXIMO_VENCER', 'VENCIDO', 'RETIRADO']
    LOOP
        target_count := CASE status_name
            WHEN 'DISPONIBLE' THEN 24 + floor(random() * 9)::integer
            WHEN 'PROXIMO_VENCER' THEN 18 + floor(random() * 8)::integer
            WHEN 'VENCIDO' THEN 22 + floor(random() * 8)::integer
            WHEN 'RETIRADO' THEN 21 + floor(random() * 8)::integer
            ELSE 20
        END;

        FOR i IN 1..target_count LOOP
            total_lotes := total_lotes + 1;
            SELECT id_producto INTO prod_id
            FROM producto_perecible
            ORDER BY random()
            LIMIT 1;

            ingreso := CURRENT_DATE - (5 + floor(random() * 56)::integer);
            initial_qty := 18 + floor(random() * 135)::integer;

            IF status_name = 'DISPONIBLE' THEN
                vencimiento := CURRENT_DATE + (16 + floor(random() * 45)::integer);
                current_qty := greatest(5, floor(initial_qty * (0.55 + random() * 0.40))::integer);
            ELSIF status_name = 'PROXIMO_VENCER' THEN
                vencimiento := CURRENT_DATE + floor(random() * 8)::integer;
                current_qty := greatest(3, floor(initial_qty * (0.25 + random() * 0.55))::integer);
            ELSIF status_name = 'VENCIDO' THEN
                vencimiento := CURRENT_DATE - (1 + floor(random() * 45)::integer);
                current_qty := greatest(1, floor(initial_qty * (0.10 + random() * 0.45))::integer);
            ELSE
                vencimiento := CURRENT_DATE - 45 + floor(random() * 96)::integer;
                current_qty := 0;
            END IF;

            ubicacion := (ARRAY[
                'Camara Lacteos A1', 'Camara Carnes C2', 'Gondola Refrigerada B3',
                'Vitrina Embutidos E1', 'Mesa Panaderia P2', 'Camara Frutas F1',
                'Isla Congelados G4', 'Vitrina Pescados M1', 'Backroom Perecibles R2'
            ])[1 + floor(random() * 9)::integer];

            numero_lote := 'PV-' || to_char(CURRENT_DATE, 'YYMM') || '-'
                    || left(status_name, 3) || '-' || lpad(total_lotes::text, 4, '0');

            INSERT INTO lote (
                numero_lote,
                cantidad_inicial,
                cantidad_actual,
                fecha_ingreso,
                fecha_vencimiento,
                ubicacion,
                estado,
                id_producto,
                id_usuario_reg
            ) VALUES (
                numero_lote,
                initial_qty,
                current_qty,
                ingreso,
                vencimiento,
                ubicacion,
                status_name,
                prod_id,
                CASE WHEN random() < 0.75 THEN operario_id ELSE supervisor_id END
            )
            RETURNING id_lote INTO lot_id;

            INSERT INTO movimiento_inventario (
                tipo,
                cantidad,
                fecha_movimiento,
                motivo,
                id_lote,
                id_usuario
            ) VALUES (
                'INGRESO',
                initial_qty,
                ingreso + make_interval(hours => 7 + floor(random() * 10)::integer),
                'Ingreso de lote por abastecimiento regular',
                lot_id,
                operario_id
            );

            IF status_name = 'RETIRADO' THEN
                tipo_mov := (ARRAY['RETIRO', 'REMATE', 'DONACION'])[1 + floor(random() * 3)::integer];
                INSERT INTO movimiento_inventario (
                    tipo,
                    cantidad,
                    fecha_movimiento,
                    motivo,
                    id_lote,
                    id_usuario
                ) VALUES (
                    tipo_mov,
                    initial_qty,
                    greatest(ingreso, CURRENT_DATE - (1 + floor(random() * 40)::integer))
                        + make_interval(hours => 8 + floor(random() * 10)::integer),
                    CASE tipo_mov
                        WHEN 'REMATE' THEN 'Salida por remate preventivo'
                        WHEN 'DONACION' THEN 'Salida por donacion antes de descarte'
                        ELSE 'Retiro operativo de inventario'
                    END,
                    lot_id,
                    supervisor_id
                );
            ELSIF random() < 0.28 THEN
                INSERT INTO movimiento_inventario (
                    tipo,
                    cantidad,
                    fecha_movimiento,
                    motivo,
                    id_lote,
                    id_usuario
                ) VALUES (
                    'AJUSTE',
                    1 + floor(random() * 8)::integer,
                    ingreso + make_interval(days => 1 + floor(random() * 12)::integer, hours => 8),
                    'Ajuste por conteo fisico de tienda',
                    lot_id,
                    CASE WHEN random() < 0.65 THEN operario_id ELSE supervisor_id END
                );
            END IF;

            IF status_name IN ('VENCIDO', 'RETIRADO') AND random() < 0.62 THEN
                unidades_merma := greatest(1, floor(initial_qty * (0.08 + random() * 0.25))::integer);
                motivo := (ARRAY[
                    'Producto vencido detectado en revision diaria',
                    'Envase deteriorado durante manipulacion',
                    'Merma por cadena de frio interrumpida',
                    'Producto retirado por control de calidad',
                    'Diferencia encontrada en conteo de cierre'
                ])[1 + floor(random() * 5)::integer];

                INSERT INTO movimiento_inventario (
                    tipo,
                    cantidad,
                    fecha_movimiento,
                    motivo,
                    id_lote,
                    id_usuario
                ) VALUES (
                    'RETIRO',
                    unidades_merma,
                    CURRENT_DATE - (floor(random() * 45)::integer) + make_interval(hours => 9 + floor(random() * 8)::integer),
                    motivo,
                    lot_id,
                    supervisor_id
                )
                RETURNING id_movimiento INTO movimiento_id;

                INSERT INTO merma (
                    cantidad,
                    fecha_registro,
                    motivo,
                    id_lote,
                    id_movimiento,
                    id_usuario
                ) VALUES (
                    unidades_merma,
                    CURRENT_DATE - (floor(random() * 45)::integer) + make_interval(hours => 10 + floor(random() * 7)::integer),
                    motivo,
                    lot_id,
                    movimiento_id,
                    supervisor_id
                );
            END IF;
        END LOOP;
    END LOOP;

    FOREACH alert_state IN ARRAY ARRAY['PENDIENTE', 'ATENDIDA', 'IGNORADA']
    LOOP
        target_count := CASE alert_state
            WHEN 'PENDIENTE' THEN 20 + floor(random() * 7)::integer
            WHEN 'ATENDIDA' THEN 22 + floor(random() * 8)::integer
            WHEN 'IGNORADA' THEN 18 + floor(random() * 8)::integer
            ELSE 20
        END;

        FOR i IN 1..target_count LOOP
            SELECT l.id_lote,
                   (l.fecha_vencimiento - CURRENT_DATE)
            INTO lot_id, dias
            FROM lote l
            WHERE l.estado IN ('PROXIMO_VENCER', 'VENCIDO', 'DISPONIBLE')
              AND (
                  alert_state <> 'PENDIENTE'
                  OR NOT EXISTS (
                      SELECT 1
                      FROM alerta a
                      WHERE a.id_lote = l.id_lote
                        AND a.estado = 'PENDIENTE'
                  )
              )
            ORDER BY CASE
                         WHEN l.estado = 'VENCIDO' THEN 0
                         WHEN l.estado = 'PROXIMO_VENCER' THEN 1
                         ELSE 2
                     END,
                     random()
            LIMIT 1;

            alert_type := CASE
                WHEN dias < 0 THEN 'VENCIDO'
                WHEN dias <= 1 THEN 'CRITICA'
                WHEN dias <= 7 THEN 'PROXIMO_VENCER'
                ELSE 'AVISO_ANTICIPADO'
            END;

            INSERT INTO alerta (
                tipo_alerta,
                dias_para_vencer,
                fecha_generacion,
                estado,
                id_lote,
                id_usuario_atiende
            ) VALUES (
                alert_type,
                dias,
                CURRENT_DATE - floor(random() * 28)::integer + make_interval(hours => 7 + floor(random() * 11)::integer),
                alert_state,
                lot_id,
                CASE WHEN alert_state = 'PENDIENTE' THEN NULL ELSE supervisor_id END
            );
        END LOOP;
    END LOOP;

    FOR i IN 1..(10 + floor(random() * 5)::integer) LOOP
        INSERT INTO reporte (
            tipo,
            fecha_generacion,
            fecha_inicio,
            fecha_fin,
            id_usuario
        ) VALUES (
            (ARRAY['STOCK', 'VENCIDOS', 'PROXIMOS_VENCER', 'MERMAS'])[1 + floor(random() * 4)::integer],
            CURRENT_DATE - floor(random() * 35)::integer + make_interval(hours => 8 + floor(random() * 9)::integer),
            CURRENT_DATE - (30 + floor(random() * 35)::integer),
            CURRENT_DATE + floor(random() * 45)::integer,
            supervisor_id
        );
    END LOOP;

    WITH ranked AS (
        SELECT id_alerta,
               row_number() OVER (
                   PARTITION BY id_lote, estado
                   ORDER BY fecha_generacion DESC, id_alerta DESC
               ) AS rn
        FROM alerta
        WHERE estado = 'PENDIENTE'
    )
    UPDATE alerta a
    SET estado = 'IGNORADA',
        id_usuario_atiende = supervisor_id
    FROM ranked r
    WHERE a.id_alerta = r.id_alerta
      AND r.rn > 1;

    RAISE NOTICE 'Demo data ready: % lots, % alerts, % mermas, users preserved.',
        (SELECT count(*) FROM lote),
        (SELECT count(*) FROM alerta),
        (SELECT count(*) FROM merma);
END $$;

COMMIT;

SELECT 'lotes' AS tabla, estado AS grupo, count(*) AS total
FROM lote
GROUP BY estado
UNION ALL
SELECT 'alertas', estado, count(*)
FROM alerta
GROUP BY estado
ORDER BY tabla, grupo;
