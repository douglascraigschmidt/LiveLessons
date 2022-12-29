package edu.vandy.quoteservices.gateway;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @@ Monte, what does this class do?
 */
@Profile("eureka")
@Configuration
@EnableDiscoveryClient
public class EurekaConfiguration {

}
