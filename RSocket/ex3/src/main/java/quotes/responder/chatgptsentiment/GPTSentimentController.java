package quotes.responder.chatgptsentiment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import quotes.common.model.Quote;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static quotes.common.Constants.CHAT_GPT_SENTIMENT_ANALYSIS;

/**
 * This class implements a Spring {@code @Controller} that handles
 * the RSocket endpoint that sends requests to the ChatGPT API.
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
public class GPTSentimentController {
    /**
     * The {@link GPTSentimentService} that's associated with this
     * {@link GPTSentimentController} via Spring's dependency
     * injection facilities, where an object receives other objects
     * that it depends on (in this case, the {@link
     * GPTSentimentService}).
     */
    @Autowired
    private GPTSentimentService mService;

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as an update {@link Quote} object
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} updated to include the sentiment
     *         analysis
    */
    @MessageMapping(CHAT_GPT_SENTIMENT_ANALYSIS)
    public Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return mService
            // Forward to the service.
            .analyzeSentiment(quoteM);
    }
}
