package edu.vandy.quoteservices.zippymicroservice;

import edu.vandy.quoteservices.zippymicroservice.model.Quote;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackageClasses = Quote.class)
@EnableJpaRepositories(basePackageClasses = ZippyRepository.class)
public class ZippyApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(ZippyApplication.class, args);
    }
}
