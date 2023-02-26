package edu.vandy.pubsub.publisher;

import edu.vandy.pubsub.common.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the Publisher example.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.pubsub")
public class PublisherApplication {
    /**
     * A static main() entry point is needed to run the
     * PublisherApplication app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the PublisherApplication within Spring WebFlux.
            .run(PublisherApplication.class, argv);
    }

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests.
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors
                .newVirtualThreadPerTaskExecutor());
    }

    /**
     * Customize the ProtocolHandler on the TomCat Connector to
     * use Java virtual threads to handle all incoming HTTP requests.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler
                    .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
