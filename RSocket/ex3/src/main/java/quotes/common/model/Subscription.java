package quotes.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * This class is used by clients that subscribe to receive Zippy th'
 * Pinhead quotes.
 *
 * The {@code @Data} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
// @Data
// @NoArgsConstructor
public record Subscription (
    /*
     * The unique subscription request id.
     */
    UUID requestId,

    /*
     * The current status of the subscription.
     */
    SubscriptionStatus status,

    /*
     * The Shakespeare play, e.g., "Hamlet", "Macbeth", etc.
     */
    String play

    /**
     * The constructor initializes the field.
     *
     * @param requestId The ID that uniquely indicates the request
     * @param play The Shakespeare play
     *
    public Subscription(UUID requestId,
                        String play) {
        this.requestId = requestId;
        this.play = play;
    } */) {}
