package tesseract.api.fluid;

import tesseract.api.Consumer;
import tesseract.graph.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a fluid consumer.
 */
public class FluidConsumer extends Consumer<IFluidPipe, IFluidNode> {

    private int isProof = 1;
    private int minCapacity = Integer.MAX_VALUE;
    private int minPressure = Integer.MAX_VALUE;
    private int minTemperature = Integer.MAX_VALUE;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     */
    protected FluidConsumer(@Nonnull IFluidNode consumer, @Nullable Path<IFluidPipe> path) {
        super(consumer, path);
    }

    /**
     * Adds fluid to the node. Returns amount of fluid that was filled.
     *
     * @param fluid FluidData attempting to fill the tank.
     * @param simulate If true, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    public int insert(@Nonnull FluidData fluid, boolean simulate) {
        return consumer.insert(fluid, simulate);
    }

    /**
     * @param fluid FluidData holding the Fluid to be queried.
     * @return If the tank can hold the fluid (EVER, not at the time of query).
     */
    public boolean canHold(@Nonnull FluidData fluid) {
        return consumer.canHold(fluid);
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
    protected void onConnectorCatch(@Nonnull IFluidPipe pipe) {
        isProof = Math.min(isProof, pipe.isGasProof() ? 1 : 0);
        minTemperature = Math.min(minTemperature, pipe.getTemperature());
        minCapacity = Math.min(minCapacity, pipe.getCapacity());
        minPressure = Math.min(minPressure, pipe.getPressure());
    }
}
