package tesseract.electric.api;

/**
 * Interface for handling an electric events. (Producer will handle them)
 */
public interface IElectricEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param position The consumer position.
     */
    void onOverVoltage(long position);

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param position The cable position.
     */
    void onOverAmperage(long position);
}
