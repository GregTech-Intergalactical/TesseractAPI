package tesseract.api.fluid;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.api.Producer;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a fluid producer.
 */
public class FluidProducer extends Producer<IFluidNode> {

    private final ObjectSet<?> filter;

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected FluidProducer(@Nonnull IFluidNode producer) {
        super(producer);
        filter = producer.getOutputFilter();
    }

    /**
     * Removes fluid from the node. Returns amount of fluid that was drained.
     * @param tank The tank to extract from.
     * @param amount Maximum amount of fluid to be removed from the container.
     * @param simulate If true, the drain will only be simulated.
     * @return FluidData representing fluid that was removed (or would be, if simulated) from the tank.
     */
    @Nullable
    public FluidData extract(@Nonnull Object tank, int amount, boolean simulate) {
        return producer.extract(tank, amount, simulate);
    }

    /**
     * @return Gets any available tank.
     **/
    @Nullable
    public Object getAvailableTank() {
        return producer.getAvailableTank();
    }

    /**
     * @return Gets the min amount of pressure that can be output.
     */
    public int getOutputAmount() {
        return Math.min(producer.getOutputPressure(), producer.getCapacity());
    }

    /**
     * @return Gets the initial amount of pressure that can be output.
     */
    public int getOutputPressure() {
        return producer.getOutputPressure();
    }

    @Override
    protected boolean canOutput(@Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputPressure() > 0;
    }
}
