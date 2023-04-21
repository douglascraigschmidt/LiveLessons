package zippyisms.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * This record is used by clients that subscribe to receive Zippy th'
 * Pinhead quotes.
 */
public record Subscription (
    /*
     * The unique subscription request id.
     */
    UUID requestId,

    /*
     * The current status of the subscription.
     */
    SubscriptionStatus status)
{}
