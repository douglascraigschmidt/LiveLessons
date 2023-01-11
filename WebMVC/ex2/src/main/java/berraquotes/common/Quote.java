package berraquotes.common;

/**
 * A Quote "POJO" exchanged between client and microservice.
 */
public record Quote (
    /*
     * The Quote id.
     */
    Integer id,

    /*
     * The Quote contents.
     */
     String quote) {
}
