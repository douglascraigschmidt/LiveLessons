package edu.vandy.quoteservices.handeymicroservice;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @@ Monte, what is the purpose of this class?
 */
@Profile("eureka")
@Configuration
@EnableDiscoveryClient
public class EurekaConfiguration {

}
