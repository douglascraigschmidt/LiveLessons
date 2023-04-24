package quotes.requester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.responder.corenlpsentiment.CoreNLPSentimentController;
import quotes.responder.quoter.QuotesMessageController;
import reactor.core.publisher.Mono;

import static quotes.common.Constants.CORE_NLP_SENTIMENT_ANALYSIS;
import static quotes.common.Constants.SUBSCRIBE;

/**
 * This class provides a client whose methods can be used to send
 * messages to endpoints provided by the {@link CoreNLPSentimentController}
 * microservice that ...
 * <p>
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class SentimentProxy {
    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.
     *
     * The {@code @Autowired} annotation marks this field to be
     * initialized via Spring's dependency injection facilities, where
     * an object receives other objects that it depends on (in this
     * case, by creating a connected {@link RSocketRequester}).
     */
    @Autowired
    private Mono<RSocketRequester> mQuoteRequester;

    /**
     * This method sends a {@link Quote} to the responder's
     * {@code messagePath} endpoint to use AI to analyze its sentiment.
     *
     * @param messagePath The path of the endpoint to send
     *                    the {@link Quote} to
     * @param quote The {@link Quote} to analyze for sentiment
     * @return A {@link Mono} that emits an update {@link Quote}
     * containing results of the sentiment analysis
     */
    public Mono<Quote> getSentiment(String messagePath,
                                    Quote quote) {
        return mQuoteRequester
            // Initialize the request to send to the server.
            .map(r -> r
                // Set the metadata to indicate the request is for
                // the responder's 'messagePath' endpoint.
                .route(messagePath)

                // Set the data to the quote to analyze.
                .data(quote))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Mono that emits the
            // resulting Quote.
            .flatMap(r -> r
                .retrieveMono(Quote.class));
    }
}
