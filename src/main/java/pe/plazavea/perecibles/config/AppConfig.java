package pe.plazavea.perecibles.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(JpaConfig.class)
@ComponentScan("pe.plazavea.perecibles")
@EnableJpaRepositories(basePackages = "pe.plazavea.perecibles.repository")
public class AppConfig {
}
