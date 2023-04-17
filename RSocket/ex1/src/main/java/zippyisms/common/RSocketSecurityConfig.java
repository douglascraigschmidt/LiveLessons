package zippyisms.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

/**
 * A Spring configuration class that configures security for RSocket
 * connections.
 *
 * This class is annotated with {@code @Configuration} to indicate
 * that it is a Spring configuration class.  It is also annotated with
 * {@code @EnableRSocketSecurity} and
 * {@code @EnableReactiveMethodSecurity} to enable RSocket security
 * and reactive method security, respectively.
 */
@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RSocketSecurityConfig {
    /**
     * Creates an instance of {@link RSocketMessageHandler} and
     * configures it with the provided {@link RSocketStrategies}.
     *
     * @param strategies The {@link RSocketStrategies} to use for
     *                   configuring the message handler
     * @return An instance of {@link RSocketMessageHandler} configured
     *         with the provided {@link RSocketStrategies}
     */
    @Bean
    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler handler =
            new RSocketMessageHandler();

        handler
            // Adds an argument resolver for resolving the
            // authentication principal.
            .getArgumentResolverConfigurer()
            .addCustomResolver(new AuthenticationPrincipalArgumentResolver());

        handler
            // Sets the RSocket strategies to use for the message
            // handler.
            .setRSocketStrategies(strategies);

        return handler;
    }

    /**
     * Returns a new instance of {@link MapReactiveUserDetailsService}
     * that contains two {@link UserDetails} instances for testing and
     * demo purposes only.
     *
     * This method is not intended for production use, as it uses an
     * insecure password encoding scheme and hardcodes the user
     * credentials. It is intended for getting started experience
     * only.
     *
     * @return A new instance of {@link MapReactiveUserDetailsService}
     *         containing two {@link UserDetails} instances for
     *         testing and demo purposes only
     */
    @Bean
    MapReactiveUserDetailsService authentication() {
        // Creates a new UserDetails instance for a regular user.
        UserDetails user = User
            .withDefaultPasswordEncoder()
            .username("d.schmidt@vanderbilt.edu")
            .password("you-shall-not-pass")
            .roles("USER")
            .build();

        // Creates a new UserDetails instance for an admin user.
        UserDetails admin = User
            .withDefaultPasswordEncoder()
            .username("admin")
            .password("pass")
            .roles("NONE")
            .build();

        // Returns a new instance of MapReactiveUserDetailsService
        // that contains the two user details objects
        return new MapReactiveUserDetailsService(user, admin);
    }

    /**
     * Configures the given {@link RSocketSecurity} to require
     * authentication for all exchanges, and returns a new instance of
     * {@link PayloadSocketAcceptorInterceptor} that enforces the
     * configured security policies.
     *
     * @param security The {@link RSocketSecurity} instance to
     *                 configure with authentication and authorization
     *                 policies
     * @return A new instance of {@link
     *         PayloadSocketAcceptorInterceptor} that enforces the
     *         configured security policies
     */
    @Bean
    PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
        security
            // Configures the RSocketSecurity to require
            // authentication for all exchanges.
            .authorizePayload(authorize -> authorize
                              .anyExchange()
                              .authenticated()) 
            // All connections, exchanges.
            .simpleAuthentication(Customizer.withDefaults());

        // Builds and returns a new
        // PayloadSocketAcceptorInterceptor that enforces the
        // configured security policies
        return security.build();
    }
}
