package edu.vandy.lockmanager.configuration;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

import java.util.concurrent.Executors;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

@Configuration
@Profile("default")
public class VirtualThreadsConfiguration {

	/**
	 * Configure the use of Java virtual threads to handle all incoming HTTP
	 * requests.
	 */
	@Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
	public AsyncTaskExecutor asyncTaskExecutor() {
		return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
	}

	/**
	 * Customize the ProtocolHandler on the TomCat Connector to use Java virtual
	 * threads to handle all incoming HTTP requests.
	 */
	@Bean
	public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
		return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

	}

}
