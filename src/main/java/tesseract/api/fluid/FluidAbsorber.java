package tesseract.api.fluid;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.api.Absorber;

import javax.annotation.Nonnull;

/**
 * A class that acts as absorber of the fluid that has passed thought pipes.
 */
public final class FluidAbsorber extends Absorber {

    private final int capacity;
    private final ObjectSet<Object> fluids = new ObjectOpenHashSet<>();

    /**
     * Creates instance of the absorber.
     *
     * @param capacity The maximum capacity.
     * @param max The maximum pressure.
     * @param pressure The initial pressure.
     * @param fluid The initial fluid.
     */
    protected FluidAbsorber(int capacity, int max, int pressure, @Nonnull Object fluid) {
        super(max, pressure);
        this.capacity = capacity;
        this.fluids.add(fluid);
    }

    /**
     * Adds a new liquid.
     *
     * @param pressure The added pressure.
     * @param fluid The fluid type.
     */
    public void add(int pressure, @Nonnull Object fluid) {
        super.add(pressure);
        fluids.add(fluid);
    }

    /**
     * @return Checks that the absorber is not able to handle pressure.
     */
    public boolean isOverPressure() {
        return super.isOver();
    }

    /**
     * @return Checks that the absorber is not able to handle pressure.
     */
    public boolean isOverCapacity() {
        return fluids.size() > capacity;
    }

    /**
     * @return Gets a current pressure.
     */
    public int getPressure() {
        return super.get();
    }

    /**
     * @return Gets a current capacity.
     */
    public int getCapacity() {
        return fluids.size();
    }

    @Override
    @Deprecated
    public int get() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean isOver() {
        throw new UnsupportedOperationException();
    }
}