package tesseract.api.electric;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for handling an electric events. (Controller will handle them)
 */
@ParametersAreNonnullByDefault
public interface IElectricEvent {

    /**
     * Executes when the node trying to receive higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The node position.
     * @param voltage The current voltage.
     */
    void onNodeOverVoltage(int dim, long pos, int voltage);

    /**
     * Executes when the cable trying to transport higher amount of voltage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     * @param voltage The current voltage.
     */
    void onCableOverVoltage(int dim, long pos, int voltage);

    /**
     * Executes when the cable trying to transport higher amount of amperage than can.
     * @param dim The dimension id.
     * @param pos The cable position.
     * @param amperage The current amperage.
     */
    void onCableOverAmperage(int dim, long pos, int amperage);
}
