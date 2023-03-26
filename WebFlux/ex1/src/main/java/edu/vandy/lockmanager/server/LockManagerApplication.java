package edu.vandy.lockmanager.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@SpringBootApplication
@ComponentScan("edu.vandy.lockmanager")
public class LockManagerApplication {
    /**
     * The main entry point into the LockManager microservice.
     */
    public static void main(String[] args) {
        SpringApplication.run(LockManagerApplication.class,
                              args);
    }

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests.
     */
    @Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
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





