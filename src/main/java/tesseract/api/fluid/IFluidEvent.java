package tesseract.api.fluid;

/**
 * Interface for handling a fluid events. (Controller will handle them)
 */
public interface IFluidEvent {

    /**
     * Executes when the node trying to receive higher amount of pressure than can.
     * @param dim The dimension id.
     * @param pos The node position.
     */
    void onNodeOverPressure(int dim, long pos);

    /**
     * Executes when the cable trying to transport higher amount of temperature than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     */
    void onPipeOverTemp(int dim, long pos);

    /**
     * Executes when the cable trying to transport higher amount of pressure than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     */
    void onPipeOverPressure(int dim, long pos);

    /**
     * Executes when the cable trying to transport higher amount of liquids than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     */
    void onPipeOverCapacity(int dim, long pos);

    /**
     * Executes when the pipe trying to transport gas when can't.
     * @param dim The dimension id.
     * @param pos The pipe position.
     */
    void onPipeGasLeak(int dim, long pos);
}
