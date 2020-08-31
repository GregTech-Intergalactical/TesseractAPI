package tesseract.api.fluid;

import tesseract.api.Consumer;
import tesseract.graph.Path;
import tesseract.util.Dir;

/**
 * A class that acts as a container for a fluid consumer.
 */
public class FluidConsumer<T> extends Consumer<IFluidPipe, IFluidNode<T>> {

    private int isProof = 1;
    private int minCapacity = Integer.MAX_VALUE;
    private int minPressure = Integer.MAX_VALUE;
    private int minTemperature = Integer.MAX_VALUE;
    private final Dir input;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     * @param dir The added direction.
     */
    protected FluidConsumer(IFluidNode<T> consumer, Path<IFluidPipe> path, Dir dir) {
        super(consumer, path);
        init();
        this.input = dir;
    }

    /**
     * Adds fluid to the node. Returns amount of fluid that was filled.
     *
     * @param data FluidData attempting to fill the tank.
     * @param simulate If true, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    public int insert(FluidData<T> data, boolean simulate) {
        return node.insert(data, simulate);
    }

    /**
     * @param fluid The Fluid to be queried.
     * @return If the tank can hold the fluid (EVER, not at the time of query).
     */
    public boolean canHold(Object fluid) {
        return node.canInput(fluid, input);
    }

    /**
     * @return Returns the priority of this node as a number.
     */
    public int getPriority() {
        return node.getPriority(input);
    }

    /**
     * @param temperature The current temperature.
     * @param pressure The current pressure.
     * @param proof True if current liquid is in a gas state.
     * @return Checks that the consumer is able to receive fluid.
     */
    public boolean canHandle(int temperature, int pressure, boolean proof) {
        return minTemperature >= temperature && minPressure >= pressure && isProof == (proof ? 1 : 0);
    }

    @Override
    protected void onConnectorCatch(IFluidPipe pipe) {
        isProof = Math.min(isProof, pipe.isGasProof() ? 1 : 0);
        minTemperature = Math.min(minTemperature, pipe.getTemperature());
        minCapacity = Math.min(minCapacity, pipe.getCapacity());
        minPressure = Math.min(minPressure, pipe.getPressure());
    }
}
