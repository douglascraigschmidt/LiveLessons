package quotes.responder.corenlpsentiment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;

import static quotes.common.Constants.*;

/**
 *
 * Spring enables the integration of RSockets into a controller via
 * the {@code @Controller} annotation, which enables the autodetection
 * of implementation classes via classpath scanning, and the
 * {@code @MessageMapping annotation}, which maps a message onto a
 * message-handling method by matching the declared patterns to a
 * destination extracted from the message.
 *
 * Combining the {@code @Controller} annotation with the
 * {@code @MessageMapping} annotation enables this class to declare
 * service endpoints, which in this case map to RSocket endpoints that
 * each take one {@link Mono} or {@link Flux} parameter and can return
 * a {@link Mono} or {@link Flux} result.  These Project Reactor
 * reactive types enable client and server code to run reactively
 * across a communication channel.
 */
@Controller
public class CoreNLPSentimentController {
    /**
     * The {@link CoreNLPSentimentService} that's associated with this
     * {@link CoreNLPSentimentController} via Spring's dependency
     * injection facilities, where an object receives other objects
     * that it depends on (in this case, the {@link
     * CoreNLPSentimentService}).
     */
    @Autowired
    private CoreNLPSentimentService mService;

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as a {@link String}.
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} updated to include the sentiment analysis
    */
    @MessageMapping(CORE_NLP_SENTIMENT_ANALYSIS)
    Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return mService
            // Forward to the service.
            .analyzeSentiment(quoteM);
    }
}
