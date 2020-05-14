package tesseract.api.fluid;

import tesseract.api.IConnectable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A fluid pipe is the unit of interaction with fluid inventories.
 */
@ParametersAreNonnullByDefault
public interface IFluidPipe extends IConnectable {

    /**
     * Returns the maximum amount of packets that this fluid component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no fluid.
     */
    int getCapacity();

    /**
     * Returns the maximum amount of pressure that this fluid component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum amount, zero or negative indicates that this component accepts no fluid.
     */
    int getPressure();

    /**
     * Returns the maximum temperature that this fluid component will permit to pass through or be received in a single packet.
     * @return A positive integer representing the maximum accepted temp, zero or negative indicates that this component accepts no fluid.
     */
    int getTemperature();

    /**
     * @return Checks that the pipe can handle gases.
     */
    boolean isGasProof();

    /**
     * @param temperature The current temperature.
     * @param pressure The current pressure.
     * @param proof True if current liquid is in a gas state.
     * @return Checks that the pipe is able to handle single packet.
     */
    default FluidStatus getHandler(int temperature, int pressure, boolean proof) {
        if (getTemperature() < temperature) return FluidStatus.FAIL_TEMP;
        else if (getPressure() < pressure) return FluidStatus.FAIL_PRESSURE;
        else if (isGasProof() != proof) return FluidStatus.FAIL_LEAK;
        return FluidStatus.SUCCESS;
    }
}
