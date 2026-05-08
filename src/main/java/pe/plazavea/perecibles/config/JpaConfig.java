package pe.plazavea.perecibles.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pe.plazavea.perecibles.model.Alerta;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Merma;
import pe.plazavea.perecibles.model.MovimientoInventario;
import pe.plazavea.perecibles.model.ProductoPerecible;
import pe.plazavea.perecibles.model.Reporte;
import pe.plazavea.perecibles.model.Usuario;

@Configuration
@EnableTransactionManagement
public class JpaConfig {

    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(env("DB_URL", "jdbc:postgresql://localhost:5432/plazavea_perecibles"));
        dataSource.setUsername(env("POSTGRES_USER", "plazavea"));
        dataSource.setPassword(env("POSTGRES_PASSWORD", "changeme"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setManagedTypes(PersistenceManagedTypes.of(
                Usuario.class.getName(),
                Categoria.class.getName(),
                ProductoPerecible.class.getName(),
                Lote.class.getName(),
                MovimientoInventario.class.getName(),
                Merma.class.getName(),
                Alerta.class.getName(),
                Reporte.class.getName(),
                ConfiguracionAlerta.class.getName()
        ));
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(AvailableSettings.HBM2DDL_AUTO, env("DB_DDL_AUTO", "create-drop"));
        properties.put(
                AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        );
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");
        return properties;
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
