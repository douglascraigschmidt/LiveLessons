package edu.vandy.pubsub.common;

import edu.vandy.pubsub.subscriber.PublisherAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;

import static edu.vandy.pubsub.common.Constants.SERVER_BASE_URL;

@Configuration
public class ClientBeans {
    @Bean
    public static PublisherAPI getPublisherAPI() {
        System.out.println("getPublisherAPI()");
        // Create a Retrofit instance with a Reactor adapter and a
        // Jackson converter.
        var result = new Retrofit
                .Builder()
            .baseUrl(SERVER_BASE_URL)

            .addCallAdapterFactory(ReactorCallAdapterFactory.create())

            .addConverterFactory(JacksonConverterFactory.create())

            .build()

            // Create an implementation of the PublisherServiceApi interface.
            .create(PublisherAPI.class);

        System.out.println("result = " + result);
        return result;
    }
}
