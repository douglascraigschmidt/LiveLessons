package quotes.responder.chatgptsentiment;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.repository.ReactiveQuoteRepository;
import quotes.utils.WebUtils;
import reactor.core.publisher.Mono;

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
public class ChatGPTSentimentService {
    /**
     * Debugging tag used by {@link Options}.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link ReactiveQuoteRepository} used to access the
     * {@link Quote} data.
     */
    @Autowired
    private ReactiveQuoteRepository mQuoteRepository;

    /**
     * The ChatGPT web service endpoint.
     */
    private static final String sAPI_ENDPOINT =
        "https://api.openai.com/v1/chat/completions";

    /**
     * Put your ChatGPT web service API key here.
     */
    private static final String sAPI_KEY =
        "sk-XXXXXXX";

    /**
     * Create a WebClient instance with the API key and content type
     * as default headers.
     */
    WebClient mWebClient = WebClient
        // Start building a new WebClient instance.
        .builder()

        // Add the API key as a default authorization header.
        .defaultHeader("Authorization",
                       "Bearer " + sAPI_KEY)

        // Set the default content type to JSon.
        .defaultHeader("Content-Type",
                       MediaType.APPLICATION_JSON_VALUE)

        // Build the WebClient instance.
        .build();

    /**
     * Analyzes the sentiment of the given text and return
     * the sentiment as a {@link String}.
     *
     * @param quoteM A {@link Mono} that emits a {@link Quote} whose
     *               sentiment is analyzed
     * @return The {@link Quote} updated to include the sentiment
     *         analysis
     */
    Mono<Quote> analyzeSentiment(Mono<Quote> quoteM) {
        return quoteM
            .flatMap(this::getSentimentFromChatGPT);
    }
    /**
     * Parse the response from the ChatGPT web service to extract just
     * the sentiment analysis.
     *
     * @param response The response from the ChatGPT web service
     * @return A {@link String} containing the sentiment analysis
     */
    public String parseResponse(String response) {
        // Create a new instance of Gson
        Gson gson = new Gson();

        // Parse the response string as a JSonObject using Gson.
        JsonObject responseJson = gson
            .fromJson(response, JsonObject.class);

        // Get the "choices" array from the response JsonObject
        JsonArray choices = responseJson
            .getAsJsonArray("choices");

        // If the "choices" array exists and has at least one element.
        if (choices != null && choices.size() > 0) {
            // Get the first choice from the "choices" array.
            JsonObject choice = choices.get(0).getAsJsonObject();

            // Get the "message" object from the first choice.
            JsonObject message = choice
                .getAsJsonObject("message");

            // Get the "content" property from the "message" object
            // and return it as a string.
            return message.get("content").getAsString();
        } else {
            // If there are no choices, return the original
            // response string.
            return response;
        }
    }

    /**
     * Get the sentiment analysis from ChatGPT for the given {@link
     * Quote}.
     *
     * @param quote The {@link Quote} whose sentiment is analyzed
     * @return The {@link Quote} updated to include the sentiment
     *         analysis
     */
    private Mono<? extends Quote> getSentimentFromChatGPT
        (Quote quote) {
        // Create a ChatGPT prompt from the given Quote.
        String prompt = generatePrompt(quote);

        // Create a JSon-encoded message containing the prompt.
        String requestBody = prompt2JSon(prompt);

        return WebUtils
            // POST the request to the ChatGPT web service.
            .makePostRequestMono(mWebClient,
                                 sAPI_ENDPOINT,
                                 requestBody,
                                 String.class)
            .flatMap(response -> {
                    // Update the Quote with the sentiment analysis.
                    quote.setSentiment(parseResponse(response));

                    // Emit the Quote in a Mono.
                    return Mono.just(quote);
                });
    }

    /**
     * Generate a prompt for the ChatGPT web service.
     *
     * @param quote The {@link Quote}
     * @return The prompt to use in the ChatGPT web service
     */
    private String generatePrompt(Quote quote) {
        return "please analyze the sentiment of this quote '"
            + quote.getQuote()
            + "' from the play "
            + quote.getPlay()
            + "in one paragraph with three sentences";
    }

    /**
     * Create a JSon-encoded message containing the {@code prompt}
     * in a format that ChatGPT understands.
     *
     * @param prompt The prompt to encode in a JSon-encoded message
     * @return The JSon-encoded message
     */
    private String prompt2JSon(String prompt) {
        return "{\"messages\": ["
            + "{\"role\": \"user\", "
            + "\"content\": \"" + prompt + "\"}], "
            + "\"model\": \"gpt-3.5-turbo\", "
            + "\"max_tokens\": 1000, "
            + "\"temperature\": 0.7"
            + "}";
    }
}
