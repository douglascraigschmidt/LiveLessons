package quotes.responder.chatgptsentiment;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;
import reactor.core.publisher.Mono;

import java.util.List;

import static quotes.utils.SentimentUtils.generatePrompt;

/**
 * This class defines methods that use the ChatGPT web service to
 * analyze the sentiment of a famous {@link Quote} from the Bard.
 * <p>
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class GPTSentimentService {
    /**
     * Debugging tag used by {@link Options}.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Auto-wire the means to access ChatGPT using Spring
     * dependency injection.
     */
    @Autowired
    private OpenAiService mOpenAiService;

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as an {@link Quote} object.
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} object updated to include the
     *         sentiment analysis
     */
    public Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return quoteM
            .map(quote -> {
                    // Create the ChatMessage containing the prompt.
                    List<ChatMessage> messages = makePrompt(quote);

                    // Send an HTTP request to ChatGPT to get the
                    // ChatCompletionResult.
                    var ccRequest = getResult(messages);

                    // Set the sentiment for the Quote.
                    setQuoteSentiment(quote, ccRequest);

                    // Return the updated quote.
                    return quote;
                });
    }

    /**
     * Creates a {@link ChatMessage} containing the prompt.
     *
     * @param quote The {@link Quote} containing information
     *              needed to make the prompt
     * @return A one-element {@link List} containing the prompt
     */
    private List<ChatMessage> makePrompt(Quote quote) {
        return List
            // Create the ChatMessage containing the prompt.
            .of(new ChatMessage
                (ChatMessageRole.SYSTEM.value(),
                 generatePrompt(quote)));
    }

    /**
     * Uses ChatGPT to get a {@link ChatCompletionResult} from the
     * {@link List} of {@link ChatMessage} objects containing
     * the prompt.
     *
     * @param messages The {@link List} of {@link ChatMessage}
     *                 objects containing the prompt
     * @return The {@link ChatCompletionResult} returned from
     *         ChatGPT
     */
    private ChatCompletionResult getResult
        (List<ChatMessage> messages) {
        var ccRequest = ChatCompletionRequest
            // Create the ChatCompletionRequest.Builder.
            .builder()

            // Specify the LLM model to use.
            .model("gpt-3.5-turbo")

            // Provide the prompt.
            .messages(messages)

            // Set the temperature, which controls how
            // deterministic the response is.
            .temperature(0.2)

            // Just return a single response.
            .n(1)

            // Build the ChatCompletionRequest.
            .build();

        return mOpenAiService
            // Use the ChatCompletionRequest to get a
            // single ChatCompletionResult.
            .createChatCompletion(ccRequest);
    }

    /**
     * Sets the sentiment of the given {@link Quote}.
     *
     * @param quote The {@link Quote} whose sentiment is analyzed
     * @param ccResult The {@link ChatCompletionResult} that
     *                 contains the result from ChatGPT
     */
    private void setQuoteSentiment
        (Quote quote,
         ChatCompletionResult ccResult) {
        quote
            // Set the sentiment for the quote.
            .setSentiment(ccResult
                          // Get the first (and only) response.
                          .getChoices().get(0)

                          // Get the sentiment content.
                          .getMessage().getContent());
    }
}
