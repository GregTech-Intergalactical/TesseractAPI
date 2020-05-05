package tesseract.api.fluid;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for handling a fluid events. (Controller will handle them)
 */
@ParametersAreNonnullByDefault
public interface IFluidEvent {

    /**
     * Executes when the cable trying to transport higher amount of pressure than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     * @param pressure The current pressure.
     */
    void onPipeOverPressure(int dim, long pos, int pressure);

    /**
     * Executes when the cable trying to transport higher amount of liquids than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     * @param capacity The current capacity.
     */
    void onPipeOverCapacity(int dim, long pos, int capacity);

    /**
     * Executes when the cable trying to transport higher amount of temperature than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     * @param temperature The current temperature.
     */
    void onPipeOverTemp(int dim, long pos, int temperature);

    /**
     * Executes when the pipe trying to transport gas than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     * @param fluid FluidData holding the Fluid to be queried.
     */
    void onPipeGasLeak(int dim, long pos, FluidData fluid);
}
