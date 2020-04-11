package tesseract.api.electric;

/**
 * Interface for handling an electric events. (Controller will handle them)
 */
public interface IElectricEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The node position.
     */
    void onNodeOverVoltage(int dim, long pos);

    /**
     * Executes when the cable trying to transport higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     */
    void onCableOverVoltage(int dim, long pos);

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     */
    void onCableOverAmperage(int dim, long pos);
}
