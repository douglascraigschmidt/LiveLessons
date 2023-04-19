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
@Data
@NoArgsConstructor
public class Subscription {
    /**
     * The unique subscription request id.
     */
    private UUID requestId;

    /**
     * The current status of the subscription.
     */
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    /**
     * The type of the subscription, e.g., ZIPPY vs. HANDEY.
     */
    private SubscriptionType type = SubscriptionType.NONE;

    /**
     * The constructor initializes the field.
     *
     * @param requestId The ID that uniquely indicates the request
     * @param type The type of the subscription
     */
    public Subscription(UUID requestId,
                        SubscriptionType type) {
        this.requestId = requestId;
        this.type = type;
    }
}
