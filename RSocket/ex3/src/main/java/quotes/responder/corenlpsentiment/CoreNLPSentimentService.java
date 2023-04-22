package quotes.responder.corenlpsentiment;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.common.model.SubscriptionStatus;
import quotes.repository.ReactiveQuoteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This class defines methods that uses the
 * {@link StanfordCoreNLP} library to analyze the sentiment of a
 * {@link Quote}.
 *
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class CoreNLPSentimentService {
    /**
     * Debugging tag used by {@link Options}.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link ReactiveQuoteRepository} used to access the quote
     * data.
     */
    @Autowired
    private ReactiveQuoteRepository mQuoteRepository;

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as a {@link String}.
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} updated to include the sentiment analysis
     */
    Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return quoteM
                .map(quote -> {
                    // Create a new Properties object.
                    Properties props = new Properties();

                    // Set the annotators property to tokenize, ssplit
                    // (sentence split), parse, and analyze sentiment.
                    props.setProperty("annotators",
                            "tokenize, ssplit, parse, sentiment");

                    // Create a new pipeline with the specified
                    // properties.
                    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

                    // Create a new CoreDocument object with the given
                    // text.
                    CoreDocument doc =
                            new CoreDocument(quote.getQuote());

                    // Annotate the document by applying the pipeline.
                    pipeline.annotate(doc);

                    // Update the sentiment score of the first
                    // sentence in the document.
                    quote.setSentiment(doc.sentences().get(0).sentiment());

                    // Return the quote.
                    return quote;
                })

                // Subscribe on the BoundedElastic thread pool.
                .subscribeOn(Schedulers.boundedElastic());
    }
}
