package tesseract.api.electric;

/**
 * Interface for handling an electric events. (Producer will handle them)
 */
public interface IElectricEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param node The node position.
     */
    void onOverVoltage(long node);

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param cable The cable position.
     */
    void onOverAmperage(long cable);
}
