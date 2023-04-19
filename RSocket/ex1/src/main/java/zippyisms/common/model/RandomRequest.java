package zippyisms.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains a request for random {@link Quote} objects.
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
public class RandomRequest {
    /**
     * The {@link Subscription} associated with this request.
     */
    private Subscription subscription;

    /**
     * The array random quote indices.
     */
    private Integer[] randomIndices;

    /**
     * The constructor initializes the fields.
     */
    public RandomRequest(Subscription subscription, 
                         Integer[] randomIndices) {
        this.subscription = subscription;
        this.randomIndices = randomIndices;
    }
}
