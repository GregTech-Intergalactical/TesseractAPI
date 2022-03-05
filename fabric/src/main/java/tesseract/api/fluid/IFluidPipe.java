package tesseract.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import tesseract.api.IConnectable;

/**
 * A fluid pipe is the unit of interaction with fluid inventories.
 */
public interface IFluidPipe extends IConnectable {

    /**
     * Returns the maximum amount of packets that this fluid component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no fluid.
     */
    int getCapacity();

    /**
     * Returns the maximum amount of pressure that this fluid component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum amount, zero or negative indicates that this component accepts no fluid.
     */
    int getPressure();

    /**
     * Returns the maximum temperature that this fluid component will permit to pass through or be received in a single packet.
     *
     * @return A positive integer representing the maximum accepted temp, zero or negative indicates that this component accepts no fluid.
     */
    int getTemperature();

    /**
     * @return Checks that the pipe can handle gases.
     */
    boolean isGasProof();

    FluidHolder getHolder();

    /**
     * @param temperature The current temperature.
     * @param pressure    The current pressure.
     * @param proof       True if current liquid is in a gas state.
     * @return Checks that the pipe is able to handle single packet.
     */
    default FluidStatus getHandler(FluidStack stack, int temperature, boolean proof) {
        FluidHolder holder = getHolder();
        if (getTemperature() < temperature) return FluidStatus.FAIL_TEMP;
        else if (!isGasProof() && proof) return FluidStatus.FAIL_LEAK;
        else if (!holder.allowFluid(stack.getFluid())) return FluidStatus.FAIL_CAPACITY;
        return FluidStatus.SUCCESS;
    }
}
