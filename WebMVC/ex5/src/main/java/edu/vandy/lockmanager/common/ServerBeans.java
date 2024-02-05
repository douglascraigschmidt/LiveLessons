package edu.vandy.lockmanager.common;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
public class ServerBeans {
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
     * @return A {@link ConcurrentHashMap}
     */
    @Bean
    public Map<LockManager,
               ArrayBlockingQueue<Lock>> getConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
