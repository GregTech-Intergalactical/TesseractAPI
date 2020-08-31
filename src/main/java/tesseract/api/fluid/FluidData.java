package tesseract.api.fluid;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class FluidData<T> {

    private final T stack;
    private final boolean isGaseous;
    private final int amount, temperature;

    /**
     * Creates instance of the data.
     *
     * @param stack The FluidStack object.
     * @param amount The amount of the fluid.
     * @param temperature The temperature of the fluid.
     * @param isGaseous The state of the fluid.
     */
    public FluidData(T stack, int amount, int temperature, boolean isGaseous) {
        this.stack = stack;
        this.amount = amount;
        this.temperature = temperature;
        this.isGaseous = isGaseous;
    }

    /**
     * @return Gets the FluidStack object.
     */
    public T getStack() {
        return stack;
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