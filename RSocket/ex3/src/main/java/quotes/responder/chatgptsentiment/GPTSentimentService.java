package quotes.responder.chatgptsentiment;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.engine.Engine;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.repository.ReactiveQuoteRepository;
import reactor.core.publisher.Mono;
import quotes.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import static quotes.utils.SentimentUtils.generatePrompt;

/**
 * This class defines methods that use the ChatGPT web service to
 * analyze the sentiment of a famous {@link Quote} from the Bard.
 *
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
     * Auto-wire the field using Spring dependency injection.
     */
    @Autowired
    OpenAiService mOpenAiService;

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as an {@link Quote} object.
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} object updated to include the sentiment
     *         analysis
     */
    Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return quoteM
            .map(quote -> {
                // Create the ChatMessage containing the prompt.
                var messages = List
                    .of(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                        generatePrompt(quote)));

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

                quote
                    // Set the sentiment for the quote.
                    .setSentiment(mOpenAiService
                        // Use the ChatCompletionRequest to get a
                        // single response.
                        .createChatCompletion(ccRequest)

                        // Get the first (and only) response.
                        .getChoices().get(0)

                        // Get the sentiment content.
                        .getMessage().getContent());

                // Return the updated quote.
                return quote;
            });
    }
}
