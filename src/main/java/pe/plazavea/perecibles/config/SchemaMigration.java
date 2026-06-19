package pe.plazavea.perecibles.config;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class SchemaMigration implements ApplicationListener<ContextRefreshedEvent> {

    private final DataSource dataSource;
    private boolean migrated;

    public SchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (migrated) {
            return;
        }
        migrated = true;
        addProductoActivoColumn();
        fixProductoCategoriaForeignKey();
        fixAlertaTipoAlertaCheckConstraint();
    }

    private void addProductoActivoColumn() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    do $$
                    begin
                        if to_regclass('public.producto_perecible') is not null then
                            alter table producto_perecible
                            add column if not exists activo boolean default true;

                            update producto_perecible
                            set activo = true
                            where activo is null;

                            alter table producto_perecible
                            alter column activo set default true;

                            alter table producto_perecible
                            alter column activo set not null;
                        end if;
                    end $$;
                    """);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo actualizar el esquema de productos", exception);
        }
    }

    private void fixProductoCategoriaForeignKey() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    do $$
                    declare
                        constraint_record record;
                    begin
                        if to_regclass('public.producto_perecible') is not null
                                and to_regclass('public.categorias') is not null then
                            for constraint_record in
                                select constraint_name
                                from information_schema.key_column_usage
                                where table_schema = 'public'
                                  and table_name = 'producto_perecible'
                                  and column_name = 'id_categoria'
                            loop
                                if exists (
                                    select 1
                                    from information_schema.table_constraints
                                    where table_schema = 'public'
                                      and table_name = 'producto_perecible'
                                      and constraint_name = constraint_record.constraint_name
                                      and constraint_type = 'FOREIGN KEY'
                                ) then
                                    execute format(
                                        'alter table producto_perecible drop constraint if exists %I',
                                        constraint_record.constraint_name
                                    );
                                end if;
                            end loop;

                            alter table producto_perecible
                            drop constraint if exists fk_producto_perecible_categoria;

                            alter table producto_perecible
                            add constraint fk_producto_perecible_categoria
                            foreign key (id_categoria)
                            references categorias(id_categoria)
                            not valid;
                        end if;
                    end $$;
                    """);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo actualizar la relacion de productos y categorias", exception);
        }
    }

    private void fixAlertaTipoAlertaCheckConstraint() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    do $$
                    declare
                        constraint_record record;
                    begin
                        if to_regclass('public.alerta') is not null then
                            for constraint_record in
                                select con.conname
                                from pg_constraint con
                                join pg_class rel on rel.oid = con.conrelid
                                join pg_namespace nsp on nsp.oid = rel.relnamespace
                                where nsp.nspname = 'public'
                                  and rel.relname = 'alerta'
                                  and con.contype = 'c'
                                  and pg_get_constraintdef(con.oid) like '%tipo_alerta%'
                            loop
                                execute format(
                                    'alter table alerta drop constraint if exists %I',
                                    constraint_record.conname
                                );
                            end loop;

                            alter table alerta
                            add constraint alerta_tipo_alerta_check
                            check (tipo_alerta in (
                                'AVISO_ANTICIPADO',
                                'CRITICA',
                                'PROXIMO_VENCER',
                                'VENCIDO'
                            ));
                        end if;
                    end $$;
                    """);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo actualizar la restriccion de tipos de alerta", exception);
        }
    }
}
