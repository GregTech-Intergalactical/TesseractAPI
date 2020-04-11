package tesseract.api.fluid;

import tesseract.api.IConnectable;
import tesseract.api.electric.ElectricStatus;

/**
 * A fluid pipe is the unit of interaction with fluid inventories.
 */
public interface IFluidPipe extends IConnectable {

    /**
     * Returns the pressure that this fluid component will permit to lost through or be received in a single tick.
     * @return A positive integer representing the loss pressure per block, zero or negative indicates that this component doesn't have a loss.
     */
    int getLoss();

    /**
     * Returns the maximum amount of packets that this fluid component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no energy.
     */
    int getCapacity();

    /**
     * Returns the maximum amount of pressure that this fluid component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum amount, zero or negative indicates that this component accepts no fluid.
     */
    long getPressure();

    /**
     * Returns the maximum temperature that this fluid component will permit to pass through or be received in a single packet.
     * @return A positive integer representing the maximum accepted temp, zero or negative indicates that this component accepts no fluid.
     */
    long getTemp();

    /**
     * @return Checks that the pipe can handle gases.
     */
    boolean isGasProof();

    /**
     * @param pressure The current pressure.
     * @param temp The current temperature.
     * @param proof True if current liquid is in a gas state.
     * @return Checks that the pipe is able to handle single packet.
     */
    default FluidStatus getStatus(long pressure, long temp, boolean proof) {
        if (getPressure() < pressure) return FluidStatus.FAIL_PRESSURE;
        else if (getTemp() < temp) return FluidStatus.FAIL_TEMP;
        else if (isGasProof() != proof) return FluidStatus.FAIL_LEAK;
        return FluidStatus.SUCCESS;
    }
}
