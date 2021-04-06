package zippyisms.datamodel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * This class is used by clients to subscribe to receive Zippy th' Pinhead quotes.
 */
@Data
@NoArgsConstructor
public class SubscriptionRequest {
    /**
     * The unique request id.
     */
    private UUID requestId;

    /**
     * The current status of the subscription.
     */
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    /**
     * The constructor initializes the field.
     *
     * @param requestId The ID that uniquely indicates the request.
     */
    public SubscriptionRequest(UUID requestId) { this.requestId = requestId;
    }
}
