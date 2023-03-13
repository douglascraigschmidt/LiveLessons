package edu.vandy.quoteservices.common;

import edu.vandy.quoteservices.common.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource(
    value = "classpath:/application.yml",
    factory = YamlPropertySourceFactory.class)
public class ServerBeans {
    // @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceUnitName("MultiQueryRepo");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter()); // set JPA vendor adapter

        // set packages to scan for JPA entities
        emf.setPackagesToScan("edu.vandy.quoteservices.microservices.zippy");

        return emf;
    }

}