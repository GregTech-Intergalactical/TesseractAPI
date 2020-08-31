package tesseract.api.gt;

/**
 * Interface for handling an electric events. (Controller will handle them)
 */
public interface IGTEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The node position.
     * @param voltage The current voltage.
     */
    default void onNodeOverVoltage(int dim, long pos, int voltage) {
        //NOOP
    }

    /**
     * Executes when the cable trying to transport higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     * @param voltage The current voltage.
     */
    default void onCableOverVoltage(int dim, long pos, int voltage) {
        //NOOP
    }

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     * @param amperage The current amperage.
     */
    default void onCableOverAmperage(int dim, long pos, int amperage) {
        //NOOP
    }
}
