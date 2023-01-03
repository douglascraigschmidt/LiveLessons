package edu.vandy.recommender.gateway;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @@ Monte, what is the purpose of this file?
 */
@Profile("eureka")
@Configuration
@EnableDiscoveryClient
public class EurekaConfiguration {
}
