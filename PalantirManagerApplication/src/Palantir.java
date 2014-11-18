/**
 * @class Palantir
 *
 * @brief Defines an interface for gazing into a Palantir and plays
 *        the role of a "Command" in the Command pattern.
 */
interface Palantir {
    /**
     * Gaze into the Palantir (and go into a trance ;-)).
     */
    public void gaze() throws InterruptedException;

    /**
     * The amount of time the gazing will occur.
     */
    public long gazeDuration();

    /**
     * Return the id of the Palantir.
     */
    public int getId();
}
