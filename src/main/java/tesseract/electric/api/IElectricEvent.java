package tesseract.electric.api;

/**
 * Interface for handling an electric events. (Producer will handle them)
 */
public interface IElectricEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param node The node ref.
     */
    void onOverVoltage(IElectricNode node);

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param cable The cable ref.
     */
    void onOverAmperage(IElectricCable cable);
}
