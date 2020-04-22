package tesseract.api.fluid;

import javax.annotation.Nonnull;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class FluidData {

    private final int amount;
    private final int temperature;
    private final boolean isGaseous;
    private final Object stack;
    private final Object fluid;

    /**
     * Creates instance of the data.
     *
     * @param stack The FluidStack object.
     * @param fluid The fluid inside a FluidStack.
     * @param amount The amount of the fluid.
     * @param temperature The temperature of the fluid.
     * @param isGaseous The state of the fluid.
     */
    public FluidData(@Nonnull Object stack, @Nonnull Object fluid, int amount, int temperature, boolean isGaseous) {
        this.stack = stack;
        this.fluid = fluid;
        this.amount = amount;
        this.temperature = temperature;
        this.isGaseous = isGaseous;
    }

    /**
     * @return Gets the FluidStack object.
     */
    @Nonnull
    public Object getStack() {
        return stack;
    }

    /**
     * @return Gets the fluid inside a FluidStack.
     */
    @Nonnull
    public Object getFluid() {
        return fluid;
    }

    /**
     * @return  Gets the fluid amount inside a FluidStack.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return Gets the temperature.
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * @return Checks the gas state.
     */
    public boolean isGaseous() {
        return isGaseous;
    }
}