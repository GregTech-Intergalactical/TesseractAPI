package tesseract.api.fluid;


import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import javax.annotation.Nonnull;

/**
 * A class that acts as holder of the fluid that has passed thought pipes.
 */
public class FluidHolder {

    private int pressure;
    private final int max;
    private final int capacity;
    private final ObjectSet<Object> fluids = new ObjectOpenHashSet<>();

    /**
     * Creates instance of the holder.
     *
     * @param capacity The maximum capacity.
     * @param max The maximum pressure.
     * @param pressure The initial pressure.
     * @param fluid The initial fluid.
     */
    protected FluidHolder(int capacity, int max, int pressure, @Nonnull Object fluid) {
        this.max = max;
        this.pressure = pressure;
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
        this.pressure += pressure;
        fluids.add(fluid);
    }

    /**
     * @return Gets a current pressure.
     */
    public int getPressure() {
        return pressure;
    }

    /**
     * @return Gets a current capacity.
     */
    public int getCapacity() {
        return fluids.size();
    }

    /**
     * @return Checks that the holder is not able to handle pressure.
     */
    public boolean isOverPressure() {
        return max < pressure;
    }

    /**
     * @return Checks that the holder is not able to handle pressure.
     */
    public boolean isOverCapacity() {
        return capacity < fluids.size();
    }
}
